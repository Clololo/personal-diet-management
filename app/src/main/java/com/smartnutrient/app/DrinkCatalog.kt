package com.smartnutrient.app

/**
 * Popular ready-to-drink beverages. Nutrition is declared per 100 ml;
 * the entry UI displays millilitres while persistence keeps grams, where 1 ml = 1 g.
 */
object DrinkCatalog {
    private var nextDrinkId = 70_000

    val items: List<FoodItem> = buildList {
        add(drink("全脂牛奶", "quan zhi niu nai", "qznn", 64.0, 3.3, 3.6, 2.1, 4.9, 0.0, 5.0, 43.0, 150.0, 14.0, listOf("纯牛奶"), 0.0, "USDA/中国食物成分表", "参考值"))
        add(drink("低脂牛奶", "di zhi niu nai", "dznn", 43.0, 3.4, 1.3, 0.8, 4.9, 0.0, 5.0, 44.0, 150.0, 9.0, listOf("低脂纯牛奶"), 0.0, "USDA", "参考值"))
        add(drink("脱脂牛奶", "tuo zhi niu nai", "tznn", 35.0, 3.4, 0.1, 0.1, 4.9, 0.0, 5.0, 44.0, 150.0, 2.0, listOf("脱脂纯牛奶"), 0.0, "USDA", "参考值"))
        add(drink("无糖豆浆", "wu tang dou jiang", "wtdj", 31.0, 3.0, 1.6, 0.2, 1.2, 0.6, 0.5, 4.0, 118.0, 0.0, listOf("原味豆浆", "豆浆"), 0.0, "USDA/标准配方估算", "参考值"))
        add(drink("甜豆浆", "tian dou jiang", "tdj", 49.0, 2.8, 1.5, 0.2, 5.5, 0.6, 4.5, 5.0, 115.0, 0.0, listOf("加糖豆浆"), 4.5, "标准配方估算", "估算"))
        add(drink("无糖酸奶", "wu tang suan nai", "wtsn", 63.0, 5.3, 3.3, 2.1, 4.7, 0.0, 4.7, 46.0, 234.0, 13.0, listOf("原味酸奶"), 0.0, "USDA", "参考值"))
        add(drink("甜酸奶", "tian suan nai", "tsn", 89.0, 3.0, 2.4, 1.5, 14.0, 0.0, 13.0, 40.0, 140.0, 10.0, listOf("风味酸奶"), 13.0, "标准配方估算", "估算"))
        add(drink("原味乳酸菌饮品", "yuan wei ru suan jun yin pin", "ywlssjyp", 70.0, 1.2, 0.0, 0.0, 16.0, 0.0, 12.0, 30.0, 110.0, 2.0, listOf("乳酸菌饮料", "原味养乐多"), 12.0, "预包装乳酸菌饮品标签典型值", "标签估算"))
        add(drink("低糖乳酸菌饮品", "di tang ru suan jun yin pin", "dtlssjyp", 50.0, 1.1, 0.0, 0.0, 11.5, 0.0, 6.5, 30.0, 110.0, 2.0, listOf("低糖养乐多"), 6.5, "预包装低糖乳酸菌饮品标签典型值", "标签估算"))
        add(drink("无糖养乐多", "wu tang yang le duo", "wtyld", 28.0, 1.1, 0.0, 0.0, 6.0, 0.0, 0.2, 30.0, 110.0, 2.0, emptyList(), 0.2, "预包装无糖乳酸菌饮品标签典型值", "标签估算"))
        add(drink("冰红茶", "bing hong cha", "bhc", 40.0, 0.0, 0.0, 0.0, 10.0, 0.0, 10.0, 25.0, 5.0, 0.0, listOf("红茶饮料"), 10.0, "预包装冰红茶标签典型值", "标签估算"))
        add(drink("无糖冰红茶", "wu tang bing hong cha", "wtbhc", 4.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.2, 25.0, 5.0, 0.0, emptyList(), 0.2, "预包装无糖冰红茶标签典型值", "标签估算"))
        add(drink("柠檬茶", "ning meng cha", "nmc", 44.0, 0.0, 0.0, 0.0, 11.0, 0.0, 10.5, 30.0, 10.0, 0.0, listOf("柠檬红茶"), 10.5, "预包装柠檬茶标签典型值", "标签估算"))
        add(drink("无糖柠檬茶", "wu tang ning meng cha", "wtnmc", 5.0, 0.0, 0.0, 0.0, 1.2, 0.0, 0.3, 30.0, 10.0, 0.0, emptyList(), 0.3, "预包装无糖柠檬茶标签典型值", "标签估算"))
        add(drink("茉莉蜜茶", "mo li mi cha", "mlmc", 43.0, 0.0, 0.0, 0.0, 10.8, 0.0, 10.5, 25.0, 5.0, 0.0, emptyList(), 10.5, "预包装茉莉蜜茶标签典型值", "标签估算"))
        add(drink("无糖茉莉花茶", "wu tang mo li hua cha", "wtmlhc", 2.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.1, 20.0, 5.0, 0.0, emptyList(), 0.1, "预包装无糖茉莉花茶标签典型值", "标签估算"))
        add(drink("乌龙茶饮料", "wu long cha yin liao", "wlcyl", 22.0, 0.0, 0.0, 0.0, 5.5, 0.0, 5.5, 35.0, 5.0, 0.0, listOf("低糖乌龙茶"), 5.5, "预包装低糖乌龙茶标签典型值", "标签估算"))
        add(drink("无糖乌龙茶", "wu tang wu long cha", "wtwlc", 2.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.1, 25.0, 5.0, 0.0, emptyList(), 0.1, "预包装无糖乌龙茶标签典型值", "标签估算"))
        add(drink("青梅绿茶", "qing mei lv cha", "qmlc", 36.0, 0.0, 0.0, 0.0, 9.0, 0.0, 8.8, 25.0, 5.0, 0.0, emptyList(), 8.8, "预包装青梅绿茶标签典型值", "标签估算"))
        add(drink("无糖绿茶", "wu tang lv cha", "wtlc", 2.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.1, 20.0, 10.0, 0.0, emptyList(), 0.1, "预包装无糖绿茶标签典型值", "标签估算"))
        add(drink("珍珠奶茶", "zhen zhu nai cha", "zznc", 80.0, 1.0, 2.5, 1.6, 13.5, 0.2, 10.0, 40.0, 60.0, 5.0, listOf("奶茶"), 8.0, "现制奶茶与珍珠典型配方估算", "估算"))
        add(drink("三分糖珍珠奶茶", "san fen tang zhen zhu nai cha", "sftzznc", 62.0, 1.0, 2.5, 1.6, 10.0, 0.2, 6.0, 40.0, 60.0, 5.0, emptyList(), 4.0, "现制三分糖奶茶典型配方估算", "估算"))
        add(drink("无糖奶茶", "wu tang nai cha", "wtnc", 45.0, 1.0, 2.5, 1.6, 5.0, 0.2, 1.0, 40.0, 60.0, 5.0, emptyList(), 1.0, "现制无糖奶茶典型配方估算", "估算"))
        add(drink("原味奶茶粉冲调", "yuan wei nai cha fen chong tiao", "ywnfcct", 68.0, 1.2, 3.0, 2.0, 11.0, 0.2, 8.0, 55.0, 80.0, 5.0, emptyList(), 6.0, "奶茶粉冲调标签典型值", "标签估算"))
        add(drink("生椰拿铁", "sheng ye na tie", "synt", 58.0, 1.2, 2.5, 1.6, 7.5, 0.1, 5.0, 35.0, 90.0, 4.0, emptyList(), 5.0, "现制生椰拿铁典型配方估算", "估算"))
        add(drink("拿铁咖啡", "na tie ka fei", "ntkf", 43.0, 1.8, 1.7, 1.0, 5.0, 0.0, 4.5, 40.0, 120.0, 6.0, listOf("牛奶咖啡"), 4.5, "现制拿铁典型配方估算", "估算"))
        add(drink("美式咖啡", "mei shi ka fei", "mskf", 2.0, 0.1, 0.0, 0.0, 0.4, 0.0, 0.1, 5.0, 45.0, 0.0, listOf("黑咖啡"), 0.0, "USDA：黑咖啡", "参考值"))
        add(drink("咖啡", "ka fei", "kf", 2.0, 0.1, 0.0, 0.0, 0.4, 0.0, 0.1, 5.0, 45.0, 0.0, listOf("黑咖啡饮品"), 0.0, "USDA：黑咖啡", "参考值"))
        add(drink("摩卡咖啡", "mo ka ka fei", "mkkf", 58.0, 1.5, 2.2, 1.4, 8.5, 0.1, 6.5, 45.0, 110.0, 6.0, emptyList(), 6.0, "现制摩卡典型配方估算", "估算"))
        add(drink("可乐", "ke le", "kl", 43.0, 0.0, 0.0, 0.0, 10.6, 0.0, 10.6, 11.0, 2.0, 0.0, listOf("汽水"), 10.6, "可乐营养标签", "参考值"))
        add(drink("零度可乐", "ling du ke le", "ldkl", 0.4, 0.0, 0.0, 0.0, 0.1, 0.0, 0.05, 15.0, 2.0, 0.0, listOf("无糖可乐"), 0.0, "零度可乐营养标签", "参考值"))
        add(drink("雪碧", "xue bi", "xb", 45.0, 0.0, 0.0, 0.0, 10.8, 0.0, 10.8, 12.0, 2.0, 0.0, emptyList(), 10.8, "雪碧营养标签典型值", "标签估算"))
        add(drink("无糖雪碧", "wu tang xue bi", "wtxb", 0.4, 0.0, 0.0, 0.0, 0.1, 0.0, 0.05, 12.0, 2.0, 0.0, emptyList(), 0.0, "无糖雪碧营养标签典型值", "标签估算"))
        add(drink("橙味汽水", "cheng wei qi shui", "cwqs", 45.0, 0.0, 0.0, 0.0, 11.0, 0.0, 11.0, 15.0, 2.0, 0.0, emptyList(), 11.0, "橙味汽水标签典型值", "标签估算"))
        add(drink("盐汽水", "yan qi shui", "yqs", 18.0, 0.0, 0.0, 0.0, 4.5, 0.0, 4.0, 90.0, 5.0, 0.0, emptyList(), 4.0, "盐汽水标签典型值", "标签估算"))
        add(drink("橙汁饮料", "cheng zhi yin liao", "czyl", 45.0, 0.3, 0.0, 0.0, 11.0, 0.1, 10.0, 15.0, 40.0, 0.0, emptyList(), 7.0, "预包装橙汁饮料标签典型值", "标签估算"))
        add(drink("100%橙汁", "bai fen zhi bai cheng zhi", "bfbzcz", 45.0, 0.7, 0.2, 0.0, 10.4, 0.2, 8.0, 5.0, 180.0, 0.0, listOf("纯橙汁"), 0.0, "USDA：橙汁", "参考值"))
        add(drink("100%苹果汁", "bai fen zhi bai ping guo zhi", "bfbzpgz", 46.0, 0.1, 0.1, 0.0, 11.3, 0.2, 9.5, 5.0, 100.0, 0.0, listOf("纯苹果汁"), 0.0, "USDA：苹果汁", "参考值"))
        add(drink("葡萄汁饮料", "putao zhi yin liao", "ptzyl", 49.0, 0.2, 0.0, 0.0, 12.0, 0.1, 10.5, 15.0, 40.0, 0.0, emptyList(), 8.0, "预包装葡萄汁饮料标签典型值", "标签估算"))
        add(drink("椰子水", "ye zi shui", "yzs", 20.0, 0.7, 0.2, 0.2, 3.7, 1.1, 2.6, 105.0, 250.0, 0.0, listOf("椰青水"), 0.0, "USDA：椰子水", "参考值"))
        add(drink("椰汁饮料", "ye zhi yin liao", "yzyl", 50.0, 0.5, 2.0, 1.6, 7.5, 0.2, 5.5, 40.0, 60.0, 0.0, listOf("椰奶饮料"), 5.0, "预包装椰汁饮料标签典型值", "标签估算"))
        add(drink("酸梅汤", "suan mei tang", "smt", 35.0, 0.2, 0.0, 0.0, 8.5, 0.2, 7.0, 35.0, 70.0, 0.0, emptyList(), 7.0, "现制/预包装酸梅汤典型范围", "估算"))
        add(drink("王老吉", "wang lao ji", "wlj", 35.0, 0.0, 0.0, 0.0, 8.8, 0.0, 8.5, 40.0, 5.0, 0.0, listOf("凉茶"), 8.5, "预包装凉茶标签典型值", "标签估算"))
        add(drink("加多宝", "jia duo bao", "jdb", 35.0, 0.0, 0.0, 0.0, 8.8, 0.0, 8.5, 40.0, 5.0, 0.0, listOf("凉茶饮料"), 8.5, "预包装凉茶标签典型值", "标签估算"))
        add(drink("无糖凉茶", "wu tang liang cha", "wtlc2", 4.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.2, 45.0, 5.0, 0.0, emptyList(), 0.2, "预包装无糖凉茶标签典型值", "标签估算"))
        add(drink("运动饮料", "yun dong yin liao", "ydyl", 26.0, 0.0, 0.0, 0.0, 6.5, 0.0, 5.5, 45.0, 15.0, 0.0, listOf("电解质饮料"), 5.0, "运动饮料标签典型值", "标签估算"))
        add(drink("无糖运动饮料", "wu tang yun dong yin liao", "wtydyl", 4.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.2, 55.0, 15.0, 0.0, emptyList(), 0.0, "无糖运动饮料标签典型值", "标签估算"))
        add(drink("功能饮料", "gong neng yin liao", "gnyl", 48.0, 0.0, 0.0, 0.0, 12.0, 0.0, 11.0, 45.0, 10.0, 0.0, listOf("能量饮料"), 11.0, "功能饮料标签典型值", "标签估算"))
        add(drink("无糖功能饮料", "wu tang gong neng yin liao", "wtgnyl", 4.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.2, 55.0, 10.0, 0.0, emptyList(), 0.0, "无糖功能饮料标签典型值", "标签估算"))
        add(drink("无糖气泡水", "wu tang qi pao shui", "wtqps", 1.0, 0.0, 0.0, 0.0, 0.2, 0.0, 0.05, 15.0, 5.0, 0.0, listOf("气泡水"), 0.0, "预包装无糖气泡水标签典型值", "标签估算"))
        add(drink("青柠气泡水", "qing ning qi pao shui", "qnqps", 20.0, 0.0, 0.0, 0.0, 5.0, 0.0, 4.8, 15.0, 5.0, 0.0, emptyList(), 4.8, "预包装低糖气泡水标签典型值", "标签估算"))
        add(drink("苏打水", "su da shui", "sds", 2.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.1, 25.0, 5.0, 0.0, emptyList(), 0.1, "无糖苏打水典型值", "标签估算"))
        add(drink("米酒", "mi jiu", "mj", 77.0, 0.3, 0.0, 0.0, 12.0, 0.0, 0.5, 5.0, 20.0, 0.0, listOf("糯米酒"), 0.5, "中国食物成分表：米酒", "参考值"))
        add(drink("啤酒", "pi jiu", "pj", 43.0, 0.5, 0.0, 0.0, 3.6, 0.0, 0.0, 4.0, 27.0, 0.0, listOf("普通啤酒"), 0.0, "USDA：普通啤酒", "参考值"))
        add(drink("果味啤酒", "guo wei pi jiu", "gwpj", 55.0, 0.3, 0.0, 0.0, 7.0, 0.0, 5.5, 10.0, 25.0, 0.0, emptyList(), 5.5, "果味啤酒标签典型值", "标签估算"))
        add(drink("蜂蜜柚子茶饮品", "feng mi you zi cha yin pin", "fmyzcyp", 45.0, 0.1, 0.0, 0.0, 11.2, 0.1, 10.5, 25.0, 20.0, 0.0, listOf("柚子茶饮料"), 10.0, "预包装蜂蜜柚子茶饮料标签典型值", "标签估算"))
        add(drink("绿豆汤", "lv dou tang", "ldt", 40.0, 1.2, 0.3, 0.1, 8.0, 0.8, 4.0, 25.0, 120.0, 0.0, emptyList(), 4.0, "现制绿豆汤典型配方估算", "估算"))
    }

    private fun drink(
        name: String,
        pinyin: String,
        initials: String,
        kcal: Double,
        protein: Double,
        fat: Double,
        saturatedFat: Double,
        carbs: Double,
        fiber: Double,
        sugar: Double,
        sodium: Double,
        potassium: Double,
        cholesterol: Double,
        aliases: List<String>,
        addedSugar: Double,
        source: String,
        confidence: String,
    ) = FoodItem(
        id = nextDrinkId++,
        name = name,
        pinyin = pinyin.replace(" ", ""),
        initials = initials,
        category = "饮品",
        basis = "每100ml",
        nutrients = FoodNutrients(kcal, protein, fat, saturatedFat, carbs, fiber, sugar, sodium, potassium, cholesterol, addedSugar),
        aliases = aliases,
        ingredients = emptyList(),
        method = "饮用",
        source = source,
        confidence = confidence,
    )
}
