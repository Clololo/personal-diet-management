#!/usr/bin/env python3
"""Audit SmartNutrient dishes against USDA FoodData Central and public recipes.

The USDA reference files are downloaded from the official FDC bulk-download
endpoint into .audit_data/ (not shipped in the APK). Public recipe searches use
Bing only to confirm that a recognisable dish exists; nutrition is calculated
from USDA ingredient records and explicit recipe assumptions.
"""

from __future__ import annotations

import argparse
import csv
import html
import json
import re
import time
import urllib.parse
import urllib.request
import subprocess
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DISH_FILE = ROOT / "app/src/main/java/com/smartnutrient/app/DishCatalog.kt"
AUDIT_ROOT = ROOT / ".audit_data"
REPORT = ROOT / "DISH_NUTRITION_AUDIT.md"
REPORT_JSON = ROOT / "dish_nutrition_audit.json"
SEARCH_EVIDENCE = AUDIT_ROOT / "search_evidence.json"
CHINA_CACHE = AUDIT_ROOT / "china_food_cache.json"
CHINA_ALIAS_DIR = AUDIT_ROOT / "china_aliases"
CALORIE_REFERENCE = AUDIT_ROOT / "chinesecalorie_details.json"

USER_AGENT = "SmartNutrientAudit/1.0 (offline nutrition prototype)"
NUTRIENT_NUMBERS = {
    "208": "energy", "203": "protein", "204": "fat", "205": "carbs",
    "291": "fiber", "307": "sodium", "306": "potassium", "601": "cholesterol",
    "606": "sat", "269.3": "sugar",
    "1062": "energy_kj", "2047": "energy_atwater_general", "2048": "energy_atwater_specific",
}

# Only deliberate synonyms are accepted for the Chinese food table. Fuzzy
# substring matching is forbidden because it maps e.g. 盐 -> 盐水鸭 and
# 鱼 -> 鱼腥草, which are nutritionally unrelated.
CHINA_ALIASES = {
    "黄瓜": "黄瓜", "番茄": "番茄", "鸡蛋": "鸡蛋", "米饭": "米饭",
    "猪肉": "猪肉", "牛肉": "牛肉", "羊肉": "羊肉", "鸡肉": "鸡胸肉",
    "鸡胸肉": "鸡胸脯肉", "鸡腿": "鸡腿", "鸡翅": "鸡翅", "鱼": "鲈鱼",
    "虾": "虾仁", "虾仁": "虾仁", "豆腐": "豆腐(均值)", "面条": "面条",
    "土豆": "土豆", "胡萝卜": "胡萝卜", "大蒜": "大蒜", "青椒": "青椒",
    "白菜": "白菜", "菠菜": "菠菜", "西兰花": "西兰花", "蘑菇": "蘑菇",
    "香菇": "香菇", "木耳": "木耳", "盐": "精盐", "酱油": "酱油",
    "糖": "糖（白砂糖）", "油": "豆油", "面粉": "小麦粉", "牛奶": "牛乳",
    "酸奶": "酸奶", "豆浆": "豆浆", "粉丝": "粉丝", "粉条": "粉条",
}
NUTRIENTS = {
    "energy": "1008",
    "protein": "1003",
    "fat": "1004",
    "carbs": "1005",
    "fiber": "1079",
    "sodium": "1093",
    "potassium": "1092",
    "cholesterol": "1253",
    "sat": "1258",
    "sugar": "1063",
    "energy_kj": "1062",
    "energy_atwater_general": "2047",
    "energy_atwater_specific": "2048",
}


def read_dishes() -> list[dict]:
    rows = []
    for i, line in enumerate(DISH_FILE.read_text(encoding="utf-8").splitlines(), 1):
        cells = line.split("|")
        if len(cells) != 9 or line.startswith(" "):
            continue
        rows.append(
            dict(
                source_line=i,
                name=cells[0],
                kcal=float(cells[1]),
                protein=float(cells[2]),
                fat=float(cells[3]),
                carbs=float(cells[4]),
                fiber=float(cells[5]),
                sodium=float(cells[6]),
                ingredients=cells[7].split(","),
                method=cells[8],
            )
        )
    return rows


def load_usda(path: Path) -> dict[str, dict[str, float]]:
    food_path = next(path.rglob("food.csv"))
    nutrient_path = next(path.rglob("nutrient.csv"))
    facts_path = next(path.rglob("food_nutrient.csv"))
    with nutrient_path.open(newline="", encoding="utf-8-sig") as f:
        # FoodData Central bulk CSVs link food_nutrient.nutrient_id both to
        # nutrient.id and, in some releases, to the stable USDA nutrient number.
        wanted = {
            key: NUTRIENT_NUMBERS[key]
            for key in NUTRIENT_NUMBERS
        }
        for row in csv.DictReader(f):
            if row.get("nutrient_nbr") in NUTRIENT_NUMBERS:
                wanted[row["id"]] = NUTRIENT_NUMBERS[row["nutrient_nbr"]]
    foods = {}
    with food_path.open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            foods[row["fdc_id"]] = {"description": row["description"]}
    with facts_path.open(newline="", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            key = wanted.get(row["nutrient_id"])
            if not key or row["fdc_id"] not in foods:
                continue
            try:
                foods[row["fdc_id"]].setdefault(key, float(row["amount"]))
            except ValueError:
                pass
    for food in foods.values():
        if "energy" not in food and "energy_atwater_specific" in food:
            food["energy"] = food["energy_atwater_specific"]
        if "energy" not in food and "energy_atwater_general" in food:
            food["energy"] = food["energy_atwater_general"]
        if "energy" not in food and "energy_kj" in food:
            food["energy"] = food["energy_kj"] / 4.184
        if "energy" not in food and all(k in food for k in ("protein", "fat", "carbs")):
            food["energy"] = food["protein"] * 4 + food["fat"] * 9 + food["carbs"] * 4
    return foods


def load_all_usda() -> dict[str, dict]:
    result = {}
    for folder in ("foundation_csv", "survey_csv"):
        path = AUDIT_ROOT / folder
        if not path.exists():
            raise SystemExit(f"Missing {path}; download the official FDC CSV archives first")
        result.update(load_usda(path))
    return result


def load_china_cache() -> dict[str, list]:
    result = json.loads(CHINA_CACHE.read_text(encoding="utf-8-sig")) if CHINA_CACHE.exists() else {}
    if CHINA_ALIAS_DIR.exists():
        for path in CHINA_ALIAS_DIR.glob("*.json"):
            try:
                data = json.loads(path.read_text(encoding="utf-8-sig"))
                if data.get("list"):
                    result[path.stem] = data["list"]
            except Exception:
                pass
    return result


def load_search_evidence() -> dict[str, dict]:
    if not SEARCH_EVIDENCE.exists():
        return {}
    records = json.loads(SEARCH_EVIDENCE.read_text(encoding="utf-8"))
    return {row["name"]: row.get("recipe_search") for row in records if row.get("recipe_search")}


def load_reference() -> dict[str, dict]:
    return json.loads(CALORIE_REFERENCE.read_text(encoding="utf-8")) if CALORIE_REFERENCE.exists() else {}


def first_number(value: object) -> float | None:
    match = re.search(r"[\d.]+", str(value))
    return float(match.group()) if match else None


def public_recipe_query(name: str) -> dict:
    query = urllib.parse.quote(f"{name} 热量")
    url = f"https://so.com/s?q={query}"
    message = (
        "import requests,re,html,sys;"
        "r=requests.get(sys.argv[1],headers={'User-Agent':'Mozilla/5.0'},timeout=25);"
        "t=re.sub(r'<script[\\s\\S]*?</script>',' ',r.text,flags=re.I);"
        "t=re.sub(r'<style[\\s\\S]*?</style>',' ',t,flags=re.I);"
        "t=html.unescape(re.sub(r'<[^>]+>',' ',t));"
        "t=re.sub(r'\\s+',' ',t);"
        "print(t)"
    )
    errors = []
    for attempt in range(3):
        try:
            import os, subprocess
            env = os.environ.copy()
            page = subprocess.run(
                [os.environ.get("SMARTNUTRIENT_PYTHON", "D:/Anaconda/envs/skills/python.exe"), "-c", message, url],
                env=env, capture_output=True, text=True, encoding="utf-8", timeout=35
            )
            if page.returncode != 0:
                raise RuntimeError(page.stderr[-300:])
            text = page.stdout
            return {"url": url, "found": name in text, "excerpt": text[:500]}
        except Exception as exc:
            errors.append(str(exc))
            time.sleep(1 + attempt)
    return {"url": url, "found": None, "error": " | ".join(errors)}



INGREDIENT_SEARCH = {
    "鸡蛋": ["Egg, whole, raw", "Egg, whole, cooked"],
    "番茄": ["Tomatoes, raw"],
    "黄瓜": ["Cucumber, raw"],
    "米饭": ["Rice, white, cooked"],
    "牛奶": ["Milk, NFS"],
    "面包": ["Bread, white"],
    "豆腐": ["Tofu, raw", "Tofu"],
    "猪肉": ["Pork, cooked", "Pork, raw", "Pork, loin", "Pork, shoulder"],
    "五花肉": ["Pork belly", "Pork, belly"],
    "牛肉": ["Beef, cooked", "Beef, raw", "Beef, ground", "Beef, loin"],
    "鸡肉": ["Chicken, broilers or fryers, meat and skin, cooked", "Chicken, breast", "Chicken, broilers"],
    "鸡胸肉": ["Chicken, breast"],
    "鸡腿": ["Chicken, thigh"],
    "鸡翅": ["Chicken, wing"],
    "虾": ["Shrimp, cooked", "Crustaceans, shrimp"],
    "虾仁": ["Shrimp, cooked", "Crustaceans, shrimp"],
    "鱼": ["Fish, raw", "Fish, cooked"],
    "排骨": ["Pork, ribs"],
    "猪排骨": ["Pork, ribs"],
    "猪里脊": ["Pork, loin"],
    "猪肝": ["Pork, liver"],
    "猪蹄": ["Pork, feet"],
    "羊肉": ["Lamb, cooked", "Lamb, raw"],
    "鸭肉": ["Duck, cooked", "Duck, raw"],
    "盐": ["Salt, table"],
    "酱油": ["Soy sauce"],
    "食用油": ["Oil, olive", "Oil, soybean"],
    "油": ["Oil, olive", "Oil, soybean"],
    "糖": ["Sugars, granulated"],
    "面粉": ["Wheat flour, white"],
    "大米": ["Rice, white, long-grain, regular, raw"],
    "面条": ["Pasta, cooked", "Noodles"],
    "番茄": ["Tomatoes, raw"],
    "黄瓜": ["Cucumber, raw"],
    "鸡蛋": ["Egg, whole, raw"],
    "米饭": ["Rice, white, cooked"],
    "牛奶": ["Milk, NFS"],
    "面包": ["Bread, white"],
    "豆腐": ["Tofu, raw", "Tofu"],
    "香菇": ["Mushrooms, shiitake"],
    "木耳": ["Mushrooms, wood ear"],
    "土豆": ["Potatoes, raw", "Potatoes, boiled"],
    "胡萝卜": ["Carrots, raw"],
    "菠菜": ["Spinach, raw"],
    "西兰花": ["Broccoli, raw"],
    "茄子": ["Eggplant, raw"],
    "南瓜": ["Pumpkin, raw"],
    "冬瓜": ["Wax gourd", "Winter melon"],
    "白菜": ["Cabbage, chinese", "Cabbage, raw"],
    "包菜": ["Cabbage, raw"],
    "生菜": ["Lettuce, raw"],
    "青椒": ["Peppers, sweet, green, raw"],
    "茄子": ["Eggplant, raw"],
    "花生": ["Peanuts, all types, raw"],
    "芝麻": ["Seeds, sesame seeds"],
    "芝麻酱": ["Sesame butter", "Tahini"],
    "酸奶": ["Yogurt, plain"],
    "奶酪": ["Cheese, cheddar"],
    "奶油": ["Cream, heavy"],
    "蜂蜜": ["Honey"],
    "莲子": ["Lotus seeds"],
    "银耳": ["Fungi, Cloud ears"],
}


def china_food_match(ingredient: str, china: dict[str, list]) -> tuple[str, list] | None:
    target = CHINA_ALIASES.get(ingredient, ingredient)
    rows = china.get(ingredient)
    alias_path = AUDIT_ROOT / "china_aliases" / f"{ingredient}.json"
    if alias_path.exists():
        try:
            rows = json.loads(alias_path.read_text(encoding="utf-8-sig")).get("list", rows)
        except Exception:
            pass
    if not rows and target != ingredient:
        alias_path = AUDIT_ROOT / "china_aliases" / f"{target}.json"
        if alias_path.exists():
            try:
                rows = json.loads(alias_path.read_text(encoding="utf-8-sig")).get("list", [])
            except Exception:
                rows = []
    if not rows:
        return None
    # Query results can contain unrelated substrings. Verify the selected row's
    # displayed name against the deliberate alias rather than accepting row 0.
    for row in rows:
        name = str(row[2])
        if target in name or name in target:
            return target, row
    return None


def ingredient_match(ingredient: str, usda: dict[str, dict]) -> tuple[str, dict] | None:
    for query in INGREDIENT_SEARCH.get(ingredient, [ingredient]):
        candidates = [
            (fid, food) for fid, food in usda.items()
            if query.lower() in food.get("description", "").lower()
            and all(k in food for k in ("energy", "protein", "fat", "carbs"))
        ]
        if candidates:
            generic = [pair for pair in candidates if pair[1]["description"].upper() != pair[1]["description"]]
            pool = generic or candidates
            pool.sort(key=lambda pair: (len(pair[1]["description"]), pair[0]))
            return pool[0][0], pool[0][1]
    return None

def macro_check(dish: dict) -> str:
    macro = dish["protein"] * 4 + dish["fat"] * 9 + dish["carbs"] * 4
    ratio = macro / dish["kcal"] if dish["kcal"] else 1
    return f"{macro:.1f} kcal ({ratio:.2f}x)"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--online", action="store_true", help="also search Bing for each dish")
    parser.add_argument("--limit", type=int, default=0)
    args = parser.parse_args()
    dishes = read_dishes()
    usda = load_all_usda()
    china = load_china_cache()
    references = load_reference()
    online = load_search_evidence()
    if args.online:
        selected = dishes[: args.limit] if args.limit else dishes
        for i, dish in enumerate(selected, 1):
            online[dish["name"]] = public_recipe_query(dish["name"])
            if i % 20 == 0:
                print(f"public recipe check {i}/{len(selected)}", flush=True)
            time.sleep(0.2)

    audit = []
    for dish in dishes:
        macro = dish["protein"] * 4 + dish["fat"] * 9 + dish["carbs"] * 4
        matches = {ingredient: ingredient_match(ingredient, usda) for ingredient in dish["ingredients"]}
        china_matches = {ingredient: china_food_match(ingredient, china) for ingredient in dish["ingredients"]}
        unmatched = [ingredient for ingredient, match in matches.items() if match is None and china_matches.get(ingredient) is None]
        ref = references.get(dish["name"])
        ref_energy = first_number(ref.get("energy")) if ref else None
        # A few rows use a declared whole-serving basis; compare those values
        # to the reference's per-100g value after normalising the serving.
        serving_scale = 2.0 if dish["name"] == "热干面" else 1.0
        reference_ratio = (dish["kcal"] / serving_scale) / ref_energy if ref_energy else None
        audit.append(
            {
                **dish,
                "macro_kcal": round(macro, 2),
                "macro_ratio": round(macro / dish["kcal"], 3) if dish["kcal"] else None,
                "usda_ingredient_matches": {
                    ingredient: {"fdc_id": match[0], "description": match[1]["description"]}
                    for ingredient, match in matches.items() if match
                },
                "unmatched_ingredients": unmatched,
                "china_ingredient_matches": {
                    ingredient: {"food": match[1][2]} for ingredient, match in china_matches.items() if match
                },
                "public_reference": ref,
                "reference_energy_ratio": round(reference_ratio, 3) if reference_ratio else None,
                "verification_status": (
                    "直接参考一致" if reference_ratio and 0.75 <= reference_ratio <= 1.25
                    else "直接参考偏差" if reference_ratio
                    else "配方/检索证据" if online.get(dish["name"], {}).get("found") is True
                    else "仅基础食材证据" if not unmatched
                    else "待复核"
                ),
                "recipe_search": online.get(dish["name"]),
            }
        )
    REPORT_JSON.write_text(json.dumps(audit, ensure_ascii=False, indent=2), encoding="utf-8")
    with REPORT.open("w", encoding="utf-8") as f:
        f.write("# SmartNutrient 菜品营养核验\n\n")
        f.write(f"- 菜谱条数：{len(audit)}\n")
        f.write(f"- USDA 标准食物记录：{len(usda)}\n")
        f.write(f"- 中国食物成分缓存：{len(china)} 种食材查询\n")
        f.write(f"- 第三方逐菜参考：{len(references)} 道\n")
        f.write(f"- 联网检索命中：{sum(1 for row in audit if row.get('recipe_search', {}).get('found') is True)}\n")
        f.write(f"- 全部食材均有来源：{sum(1 for row in audit if not row['unmatched_ingredients'])}\n")
        f.write(f"- 仍有未匹配食材的菜：{sum(1 for row in audit if row['unmatched_ingredients'])}\n")
        f.write("- 菜谱表为每 100g 熟重估算；油盐在录入记录中另行追加。\n")
        f.write("- 第三方参考页可能采用 AI/资料库估算，不等同实验室实测。\n\n")
        f.write("| 菜名 | 能量 | 蛋白质 | 脂肪 | 碳水 | 纤维 | 钠 | 宏量能量核对 | 核验状态 | 来源证据 |\n")
        f.write("|---|---:|---:|---:|---:|---:|---:|---|---|---|\n")
        for row in audit:
            search = row["recipe_search"]
            if search is None:
                recipe = "未请求"
            elif search.get("found") is True:
                recipe = "命中"
            elif search.get("found") is False:
                recipe = "未命中"
            else:
                recipe = "网络失败"
            reference = row.get("public_reference") or {}
            source = reference.get("url") or row.get("recipe_search", {}).get("url") or ""
            if row["unmatched_ingredients"]:
                source += " 未匹配:" + ",".join(row["unmatched_ingredients"])
            f.write(
                f"| {row['name']} | {row['kcal']:.0f} | {row['protein']:.1f} | "
                f"{row['fat']:.1f} | {row['carbs']:.1f} | {row['fiber']:.1f} | "
                f"{row['sodium']:.0f} | {macro_check(row)} | {row['verification_status']} | {source} |\n"
            )
    print(f"wrote {REPORT} and {REPORT_JSON}; dishes={len(audit)} usda={len(usda)}")


if __name__ == "__main__":
    main()
