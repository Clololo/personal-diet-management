package com.smartnutrient.app

internal object AdditionalFoodCatalog {
    val items: List<FoodItem> = buildList {
        add(food(90_001, "藜麦鸡胸沙拉", "limaijixiongshala", "lmjx", "菜", "熟重", 132.0, 14.0, 4.0, 0.8, 10.0, 3.5, 2.0, 280.0, 420.0, 35.0, listOf("鸡胸藜麦沙拉"), listOf("藜麦", "鸡胸肉", "生菜"), "拌", "标准配方估算", FoodDataQuality.RECIPE_ESTIMATE, false))
        add(food(90_002, "虾仁西兰花", "xiarenxilanhua", "xrxlh", "菜", "熟重", 96.0, 12.0, 3.0, 0.6, 5.0, 2.6, 1.5, 260.0, 330.0, 95.0, listOf("西兰花炒虾仁"), listOf("虾仁", "西兰花"), "炒", "标准配方估算", FoodDataQuality.RECIPE_ESTIMATE, false))
        add(food(90_003, "藜麦饭", "limaifan", "lmf", "主食", "熟重", 120.0, 4.4, 1.9, 0.2, 21.3, 2.8, 0.9, 7.0, 170.0, 0.0, listOf("藜麦米饭"), listOf("藜麦"), "蒸", "USDA/标准配方估算", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_004, "青稞饭", "qingkefan", "qkf", "主食", "熟重", 132.0, 4.0, 1.0, 0.2, 27.0, 3.8, 0.5, 4.0, 180.0, 0.0, listOf("青稞米饭"), listOf("青稞"), "蒸", "中国食物成分表", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_005, "鹰嘴豆泥", "yingzuidouni", "yz dn", "菜", "熟重", 164.0, 7.9, 8.0, 1.1, 18.0, 6.0, 2.0, 300.0, 292.0, 0.0, listOf("鹰嘴豆酱", "hummus"), listOf("鹰嘴豆", "芝麻酱"), "拌", "USDA/标准配方估算", FoodDataQuality.RECIPE_ESTIMATE, false))
        add(food(90_006, "香煎三文鱼", "xiangjiansanwenyu", "xjswy", "菜", "熟重", 208.0, 22.0, 13.0, 2.5, 0.0, 0.0, 0.0, 150.0, 360.0, 65.0, listOf("三文鱼"), listOf("三文鱼"), "煎", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_007, "鸡胸肉丸", "jixiongrouwan", "jxrw", "菜", "熟重", 165.0, 20.0, 7.0, 2.0, 6.0, 1.0, 1.0, 420.0, 310.0, 55.0, listOf("鸡肉丸"), listOf("鸡胸肉"), "煮", "标准配方估算", FoodDataQuality.RECIPE_ESTIMATE, false))
        add(food(90_008, "低脂牛肉丸", "dizhiniurouwan", "dznrw", "菜", "熟重", 170.0, 18.0, 8.0, 3.0, 7.0, 1.0, 1.0, 450.0, 290.0, 55.0, listOf("牛肉丸"), listOf("牛肉"), "煮", "标准配方估算", FoodDataQuality.RECIPE_ESTIMATE, false))
        add(food(90_009, "蓝莓", "lanmei", "lm", "水果", "可食部", 57.0, 0.7, 0.3, 0.1, 14.5, 2.4, 10.0, 1.0, 77.0, 0.0, listOf("蓝莓鲜果"), listOf("蓝莓"), "鲜食", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_010, "红树莓", "hongshumei", "hsm", "水果", "可食部", 52.0, 1.2, 0.7, 0.1, 11.9, 6.5, 4.4, 1.0, 151.0, 0.0, listOf("树莓", "覆盆子"), listOf("树莓"), "鲜食", "USDA", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_011, "无花果", "wuhuaguo", "whg", "水果", "可食部", 74.0, 0.8, 0.3, 0.1, 19.2, 2.9, 16.3, 1.0, 232.0, 0.0, listOf("鲜无花果"), listOf("无花果"), "鲜食", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_012, "红心火龙果", "hongxinhuolongguo", "hxhlg", "水果", "可食部", 60.0, 1.1, 0.4, 0.1, 13.0, 3.0, 8.0, 2.0, 220.0, 0.0, listOf("红肉火龙果"), listOf("火龙果"), "鲜食", "USDA/同类参考", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_013, "鸡胸肉", "jixiongrou", "jxr", "食材", "每100g", 120.0, 22.0, 2.5, 0.7, 0.0, 0.0, 0.0, 55.0, 340.0, 60.0, listOf("鸡胸", "鸡胸脯肉"), listOf("鸡胸肉"), "生食材", "USDA/中国食物成分表", FoodDataQuality.MEASURED, false))
        add(food(90_014, "三文鱼", "sanwenyu", "swy", "食材", "每100g", 208.0, 20.0, 13.0, 2.5, 0.0, 0.0, 0.0, 59.0, 363.0, 55.0, listOf("鲑鱼"), listOf("三文鱼"), "生食材", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_015, "毛豆", "maodou", "md", "食材", "每100g", 131.0, 13.0, 5.0, 0.7, 10.0, 4.0, 2.0, 6.0, 436.0, 0.0, listOf("枝豆", "青大豆"), listOf("毛豆"), "生食材", "中国食物成分表", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_016, "鹰嘴豆", "yingzuidou", "yzd", "食材", "每100g", 164.0, 8.9, 2.6, 0.3, 27.4, 7.6, 4.8, 7.0, 291.0, 0.0, listOf("鹰嘴豆干"), listOf("鹰嘴豆"), "生食材", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_017, "荞麦", "qiaomai", "qm", "食材", "每100g", 343.0, 13.2, 3.4, 0.7, 71.5, 10.0, 0.0, 1.0, 460.0, 0.0, listOf("荞麦米"), listOf("荞麦"), "生食材", "USDA", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_018, "亚麻籽", "yamazi", "ymz", "食材", "每100g", 534.0, 18.3, 42.2, 3.7, 28.9, 27.3, 1.6, 30.0, 813.0, 0.0, listOf("亚麻籽仁"), listOf("亚麻籽"), "生食材", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_019, "奇亚籽", "qiyazi", "qyz", "食材", "每100g", 486.0, 16.5, 30.7, 3.3, 42.1, 34.4, 0.0, 16.0, 407.0, 0.0, listOf("奇亚籽"), listOf("奇亚籽"), "生食材", "USDA", FoodDataQuality.MEASURED, false))
        add(food(90_020, "无糖燕麦奶", "wutangyanmainai", "wtymn", "饮品", "每100ml", 46.0, 1.0, 1.5, 0.2, 6.7, 0.8, 3.3, 42.0, 120.0, 0.0, listOf("燕麦奶"), listOf("燕麦", "水"), "冲调", "品牌标签典型值", FoodDataQuality.BRAND_LABEL, false))
        add(food(90_021, "无糖杏仁奶", "wutangxingrennai", "wtxrn", "饮品", "每100ml", 17.0, 0.6, 1.2, 0.1, 0.6, 0.3, 0.2, 60.0, 60.0, 0.0, listOf("杏仁奶"), listOf("杏仁", "水"), "冲调", "USDA", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_022, "无糖椰奶", "wutangyenai", "wtyn", "饮品", "每100ml", 73.0, 0.6, 7.3, 6.5, 1.0, 0.0, 0.5, 15.0, 100.0, 0.0, listOf("椰奶饮料"), listOf("椰浆", "水"), "冲调", "USDA", FoodDataQuality.GENERIC_REFERENCE, false))
        add(food(90_023, "低脂高蛋白酸奶", "dizhigaodanbaisuannai", "dzgdsn", "饮品", "每100ml", 57.0, 9.0, 0.5, 0.2, 3.5, 0.0, 3.0, 45.0, 150.0, 5.0, listOf("高蛋白酸奶"), listOf("牛奶", "乳酸菌"), "发酵", "品牌标签典型值", FoodDataQuality.BRAND_LABEL, false))
        add(food(90_024, "零糖电解质饮料", "lingtangdianjiezhiyinliao", "ltdjzyl", "饮品", "每100ml", 2.0, 0.0, 0.0, 0.0, 0.4, 0.0, 0.0, 45.0, 15.0, 0.0, listOf("电解质水", "无糖运动饮料"), listOf("水", "电解质"), "冲调", "品牌标签典型值", FoodDataQuality.BRAND_LABEL, false))
        add(food(90_025, "油炸方便面饼（仅面饼）", "youzhafangbianmianbingjinmianbing", "yzfbmjmb", "主食", "干重", 473.0, 9.5, 21.1, 10.0, 61.0, 2.3, 2.0, 430.0, 134.0, 0.0, listOf("方便面饼", "泡面面饼"), listOf("小麦"), "油炸", "包装标签典型值", FoodDataQuality.BRAND_LABEL, false))
        add(food(90_026, "油炸方便面（含调料包整套）", "youzhafangbianmianhan tiaoliaobaozhengtao", "yzfbmhtlbzt", "主食", "每份", 480.0, 10.0, 22.0, 10.5, 60.0, 2.5, 3.0, 2200.0, 160.0, 0.0, listOf("方便面", "泡面", "每份120g"), listOf("小麦", "调味粉包", "油包"), "冲泡", "包装标签整套参考值", FoodDataQuality.BRAND_LABEL, true))
        add(food(90_027, "非油炸方便面（仅面饼）", "feiyouzhafangbianmianjinmianbing", "fyzfbmjmb", "主食", "干重", 350.0, 9.0, 4.0, 1.0, 67.0, 2.5, 2.0, 250.0, 130.0, 0.0, listOf("非油炸面饼"), listOf("小麦"), "干燥", "包装标签典型值", FoodDataQuality.BRAND_LABEL, false))
        add(food(90_028, "非油炸方便面（含调料包整套）", "feiyouzhafangbianmianhantaoliaobao", "fyzfbmhtlb", "主食", "每份", 360.0, 9.5, 5.0, 1.2, 65.0, 2.2, 3.0, 1900.0, 150.0, 0.0, listOf("非油炸方便面", "每份120g"), listOf("小麦", "调味粉包"), "冲泡", "包装标签整套参考值", FoodDataQuality.BRAND_LABEL, true))
        add(food(90_029, "杯面（含调料）", "beimianhantiaoliao", "bmhtl", "主食", "每份", 450.0, 9.0, 18.0, 8.0, 60.0, 2.0, 3.0, 2100.0, 150.0, 0.0, listOf("杯面", "每份100g"), listOf("小麦", "调味粉包"), "冲泡", "包装标签整套参考值", FoodDataQuality.BRAND_LABEL, true))
        add(food(90_030, "拌面（含酱包）", "banmianhanjiangbao", "bmhjb", "主食", "每份", 470.0, 10.0, 19.0, 7.0, 62.0, 2.0, 4.0, 1900.0, 160.0, 0.0, listOf("干拌面", "每份120g"), listOf("小麦", "酱包"), "拌", "包装标签整套参考值", FoodDataQuality.BRAND_LABEL, true))
    }

    private fun food(
        id: Int, name: String, pinyin: String, initials: String, category: String, basis: String,
        kcal: Double, protein: Double, fat: Double, sat: Double, carbs: Double, fiber: Double, sugar: Double,
        sodium: Double, potassium: Double, cholesterol: Double, aliases: List<String>, ingredients: List<String>,
        method: String, source: String, quality: FoodDataQuality, includesSeasoningPack: Boolean,
    ) = FoodItem(
        id = id,
        name = name,
        pinyin = pinyin.replace(" ", ""),
        initials = initials.replace(" ", ""),
        category = category,
        basis = basis,
        nutrients = FoodNutrients(kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, cholesterol, SugarDataPolicy.addedSugarG(category, name, sugar)),
        aliases = aliases,
        ingredients = ingredients,
        method = method,
        source = source,
        confidence = if (quality == FoodDataQuality.MEASURED) "参考值" else "估算",
        dataQuality = quality,
        includesSeasoningPack = includesSeasoningPack,
    )
}
