#!/usr/bin/env python3
"""Build an evidence index from the public ChineseCalorie catalogue.

This source is used only as a third-party cross-check. Values are clearly
labelled as public/AI-assisted estimates on the source site, not laboratory
measurements. The saved index makes every comparison reproducible offline.
"""

from __future__ import annotations

import json
import re
import time
import urllib.parse
import urllib.request
from pathlib import Path

from bs4 import BeautifulSoup

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / ".audit_data/chinesecalorie_catalog.json"
DETAIL_OUT = ROOT / ".audit_data/chinesecalorie_details.json"
BASE = "https://chinesecalorie.com"
HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0 Safari/537.36"}


def fetch(url: str) -> str:
    request = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8", "replace")


def main() -> None:
    foods = {}
    for page in range(1, 27):
        url = f"{BASE}/china/foods" if page == 1 else f"{BASE}/china/foods?page={page}"
        soup = BeautifulSoup(fetch(url), "html.parser")
        for card in soup.select("article.cn-food-card"):
            link = card.select_one("h2 a")
            energy = card.select_one(".cn-card-energy")
            if not link or not energy:
                continue
            name = link.get_text(" ", strip=True)
            href = link.get("href")
            if not name or not href:
                continue
            foods[name] = {"name": name, "url": urllib.parse.urljoin(BASE, href), "listing": energy.get_text(" ", strip=True)}
        print(f"page {page}: foods={len(foods)}", flush=True)
        time.sleep(0.4)
    OUT.write_text(json.dumps(list(foods.values()), ensure_ascii=False, indent=2), encoding="utf-8")
    details = {}
    for i, food in enumerate(foods.values(), 1):
        try:
            page = fetch(food["url"])
            soup = BeautifulSoup(page, "html.parser")
            record = dict(food)
            energy = soup.select_one(".cn-dish-energy strong")
            if energy:
                record["energy"] = energy.get_text(strip=True)
            for grid in soup.select(".cn-nutrient-grid div"):
                label = grid.select_one("dt")
                value = grid.select_one("dd")
                if label and value:
                    key = re.split(r"计量|每", label.get_text(" ", strip=True), maxsplit=1)[0].strip()
                    record[key] = value.get_text(" ", strip=True)
            details[food["name"]] = record
        except Exception as exc:
            details[food["name"]] = {**food, "error": str(exc)}
        if i % 25 == 0:
            print(f"details {i}/{len(foods)}", flush=True)
        time.sleep(0.25)
    DETAIL_OUT.write_text(json.dumps(details, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"wrote {OUT} foods={len(foods)}; {DETAIL_OUT} details={len(details)}")


if __name__ == "__main__":
    main()
