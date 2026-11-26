package com.smartnutrient.app

/**
 * Standalone entries for ingredients named by the dish catalogue but not yet
 * available through the ordinary meal-entry search. Values are per 100 g.
 * Generic terms are explicitly marked as representative estimates.
 */
internal fun expandedIngredientSpecs(): List<CatalogSpec> = buildList {
    addReferenceEntries()
    addMeatEntries()
    addSeafoodEntries()
    addVegetableEntries()
    addPantryEntries()
    addRemainingDishIngredients()
    addLegumeAndWholeGrainEntries()
}

private fun MutableList<CatalogSpec>.addReferenceEntries() {
    add(ingredientRef("猪肉", "zhurou", "zr", 143.0, 20.3, 6.2, 2.2, 1.5, 0.0, 0.0, 57.5, 197.0,
        aliases = listOf("猪肉片", "猪瘦肉"), ingredients = listOf("猪肉"), method = "生食材",
        source = "中国食物成分表：猪肉（里脊）", scope = "泛称按常见瘦部位参考"))
    add(ingredientRef("牛肉", "niurou", "nr", 125.0, 19.9, 4.2, 1.6, 2.0, 0.0, 0.0, 84.2, 216.0,
        aliases = listOf("牛瘦肉", "牛肉（肥瘦）"), ingredients = listOf("牛肉"), method = "生食材",
        source = "中国食物成分表：牛肉（肥瘦均值）", scope = "肥瘦均值"))
    add(ingredientRef("鸡肉", "jirou", "jr", 138.0, 20.3, 5.5, 1.5, 1.5, 0.0, 0.0, 48.0, 300.0,
        ingredients = listOf("鸡肉"), method = "生食材",
        source = "USDA/中国食物成分表：鸡肉（均值）", scope = "带皮与去皮部位会明显不同"))
}

private fun MutableList<CatalogSpec>.addMeatEntries() {
    add(ingredientRef("羊肉", "yangrou", "yr", 203.0, 19.0, 14.1, 5.8, 0.0, 0.0, 0.0, 80.6, 232.0,
        aliases = listOf("羊瘦肉", "羊肉（肥瘦）"), ingredients = listOf("羊肉"), method = "生食材",
        source = "中国食物成分表：羊肉（肥瘦均值）", scope = "肥瘦均值"))
    add(ingredientRef("牛肉末", "niuroumo", "nrm", 250.0, 26.0, 15.0, 5.9, 0.0, 0.0, 0.0, 66.0, 318.0,
        aliases = listOf("牛肉馅", "牛肉糜"), ingredients = listOf("牛肉"), method = "生食材",
        source = "USDA：牛绞肉（80%瘦）", scope = "瘦肉比例影响脂肪与能量"))
    add(ingredientRef("肥牛", "feiniu", "fn", 336.0, 16.8, 30.2, 11.8, 0.0, 0.0, 0.0, 57.0, 182.0,
        aliases = listOf("肥牛肉片", "肥牛肉卷"), ingredients = listOf("牛肉"), method = "生食材",
        source = "中国食物成分表：牛腩", scope = "切片肥瘦比例差异较大"))
    add(ingredientRef("牛尾", "niuwei", "nw", 214.0, 18.6, 15.0, 6.8, 0.0, 0.0, 0.0, 72.0, 200.0,
        ingredients = listOf("牛肉"), method = "生食材", source = "USDA：牛尾", scope = "生重"))
    add(ingredientRef("牛肚", "niudu", "nd", 72.0, 14.5, 1.6, 0.5, 0.0, 0.0, 0.0, 60.0, 67.0,
        aliases = listOf("毛肚", "牛百叶"), ingredients = listOf("牛肉"), method = "生食材",
        source = "USDA：牛肚", scope = "不同部位略有差异"))
    add(ingredientRef("牛杂", "niufu", "nz", 122.0, 16.0, 5.0, 2.0, 1.0, 0.0, 0.0, 140.0, 180.0,
        aliases = listOf("牛杂碎"), ingredients = listOf("牛肉"), method = "生食材",
        source = "综合同类参考估算", scope = "牛肚、牛心等组成的泛称"))
    add(ingredientRef("牛排骨", "niupaigu", "npg", 235.0, 18.6, 17.0, 8.0, 0.0, 0.0, 0.0, 60.0, 250.0,
        ingredients = listOf("牛肉"), method = "生食材", source = "USDA：牛肋排", scope = "生重"))
    add(ingredientRef("猪蹄", "zhuti", "zt", 260.0, 22.6, 18.8, 6.7, 0.0, 0.0, 0.0, 101.0, 54.0,
        aliases = listOf("猪脚", "猪手"), ingredients = listOf("猪肉"), method = "生食材",
        source = "中国食物成分表：猪蹄", scope = "生重"))
    add(ingredientRef("猪肝", "zhugan", "zg", 129.0, 19.3, 3.5, 1.2, 5.0, 0.0, 0.0, 68.0, 235.0,
        ingredients = listOf("猪肉"), method = "生食材", source = "USDA/中国食物成分表：猪肝", scope = "生重"))
    add(ingredientRef("猪腰", "zhuyao", "zy", 96.0, 15.4, 3.2, 1.1, 1.4, 0.0, 0.0, 134.0, 267.0,
        aliases = listOf("猪肾"), ingredients = listOf("猪肉"), method = "生食材",
        source = "中国食物成分表：猪肾", scope = "生重"))
    add(ingredientRef("猪耳", "zhuer", "ze", 190.0, 22.5, 10.0, 3.5, 0.0, 0.0, 0.0, 64.0, 68.0,
        ingredients = listOf("猪肉"), method = "生食材", source = "USDA：猪耳", scope = "生重"))
    add(ingredientRef("猪肚", "zhudu", "zd", 110.0, 15.2, 5.1, 1.7, 0.7, 0.0, 0.0, 75.0, 171.0,
        ingredients = listOf("猪肉"), method = "生食材", source = "USDA：猪肚", scope = "生重"))
    add(ingredientRef("肥肠", "feichang", "fc", 196.0, 6.9, 18.7, 6.7, 0.0, 0.0, 0.0, 56.0, 44.0,
        aliases = listOf("猪大肠", "猪肠"), ingredients = listOf("猪肉"), method = "生食材",
        source = "USDA：猪大肠", scope = "生重"))
    add(ingredientRef("鸭腿", "yatui", "yt3", 217.0, 18.0, 15.7, 5.0, 0.0, 0.0, 0.0, 74.0, 210.0,
        ingredients = listOf("鸭肉"), method = "生食材", source = "USDA：鸭腿肉", scope = "生重"))
    add(ingredientRef("鸡爪", "jizhua", "jz", 215.0, 19.4, 14.6, 4.0, 0.0, 0.0, 0.0, 69.0, 31.0,
        aliases = listOf("凤爪", "鸡脚"), ingredients = listOf("鸡肉"), method = "生食材",
        source = "USDA：鸡爪", scope = "生重"))
    add(ingredientRef("鸭血", "yaxue", "yx", 13.0, 2.5, 0.1, 0.0, 0.8, 0.0, 0.0, 120.0, 150.0,
        ingredients = listOf("鸭血"), method = "生食材", source = "中国食物成分表：鸭血", scope = "生重"))
    add(ingredientRef("皮蛋", "pidan", "pd", 178.0, 14.2, 12.7, 4.0, 4.5, 0.0, 0.0, 542.0, 150.0,
        aliases = listOf("松花蛋", "变蛋"), ingredients = listOf("鸡蛋"), method = "腌制品",
        source = "中国食物成分表：皮蛋", scope = "可食部"))
    add(ingredientRef("咸蛋黄", "xiandanghuang", "xdh", 328.0, 14.5, 27.0, 9.0, 6.0, 0.0, 0.0, 1200.0, 100.0,
        ingredients = listOf("鸡蛋"), method = "腌制品", source = "咸蛋黄营养标签典型范围", scope = "腌制食品"))
    add(ingredientRef("鸡蛋", "jidan", "jd", 144.0, 12.4, 9.8, 3.2, 0.8, 0.0, 0.4, 138.0, 128.0,
        aliases = listOf("全蛋", "鲜鸡蛋"), ingredients = listOf("鸡蛋"), method = "生食材",
        source = "USDA/中国食物成分表", scope = "可食部"))
    add(ingredientRef("火腿", "huotui", "ht3", 145.0, 16.0, 7.0, 2.5, 2.0, 0.0, 1.0, 880.0, 250.0,
        aliases = listOf("火腿片"), ingredients = listOf("猪肉"), method = "腌制品",
        source = "包装火腿营养标签典型范围", scope = "钠随品牌显著变化"))
    add(ingredientRef("腊肉", "larou", "lr", 498.0, 11.8, 48.8, 17.0, 0.0, 0.0, 0.0, 763.0, 173.0,
        ingredients = listOf("猪肉"), method = "腌制品", source = "中国食物成分表：腊肉", scope = "生重"))
    add(ingredientRef("香肠", "xiangchang", "xc2", 508.0, 24.1, 40.7, 14.0, 11.2, 0.0, 3.0, 2309.0, 445.0,
        ingredients = listOf("猪肉"), method = "腌制品", source = "中国食物成分表：香肠", scope = "品牌差异明显"))
    add(ingredientRef("腊肠", "lachang", "lc2", 584.0, 22.0, 48.3, 17.0, 15.0, 0.0, 5.0, 1700.0, 450.0,
        aliases = listOf("广式腊肠"), ingredients = listOf("猪肉"), method = "腌制品",
        source = "腊肠营养标签典型范围", scope = "脂肪和钠随配方变化"))
    add(ingredientRef("腊味", "lawei", "lw", 508.0, 24.1, 40.7, 14.0, 11.2, 0.0, 3.0, 1900.0, 430.0,
        ingredients = listOf("猪肉"), method = "腌制品", source = "综合同类参考估算", scope = "腊肉、腊肠混合泛称"))
    add(ingredientRef("叉烧", "chashao", "cs2", 280.0, 23.0, 15.0, 5.0, 12.0, 0.0, 9.0, 650.0, 300.0,
        ingredients = listOf("猪肉"), method = "烤", source = "粤式叉烧营养标签典型范围", scope = "熟重"))
    add(ingredientRef("烧鸭", "shaoya", "sy3", 298.0, 19.0, 23.0, 6.5, 3.0, 0.0, 1.0, 470.0, 240.0,
        ingredients = listOf("鸭肉"), method = "烤", source = "烧鸭营养标签典型范围", scope = "熟重"))
    add(ingredientRef("烧鹅", "shaoe", "se", 305.0, 18.5, 24.5, 7.0, 2.5, 0.0, 1.0, 500.0, 250.0,
        ingredients = listOf("鹅肉"), method = "烤", source = "烧鹅营养标签典型范围", scope = "熟重"))
    add(ingredientRef("羊杂", "yangza", "yz", 121.0, 16.4, 4.8, 1.9, 0.8, 0.0, 0.0, 132.0, 188.0,
        ingredients = listOf("羊肉"), method = "生食材", source = "USDA 羊杂同类部位估算", scope = "羊肚、羊心等泛称"))
}
private fun MutableList<CatalogSpec>.addSeafoodEntries() {
    add(ingredientRef("鱼", "yu", "y", 105.0, 18.6, 3.0, 0.7, 0.0, 0.0, 0.0, 90.0, 280.0,
        aliases = listOf("鱼类", "鱼肉"), ingredients = listOf("鱼"), method = "生食材",
        source = "中国食物成分表：鲈鱼", scope = "泛称按常见清蒸鱼参考"))
    add(ingredientRef("海米", "haimi", "hm", 200.0, 43.7, 2.6, 0.6, 17.0, 0.0, 0.0, 4891.0, 550.0,
        aliases = listOf("虾米", "干虾仁"), ingredients = listOf("虾"), method = "干食材",
        source = "中国食物成分表：虾米（海米）", scope = "干重；钠很高"))
    add(ingredientRef("虾", "xia", "x", 101.0, 18.9, 1.7, 0.5, 1.5, 0.0, 0.0, 148.0, 259.0,
        ingredients = listOf("虾"), method = "生食材", source = "USDA/中国食物成分表：虾（可食部均值）", scope = "鲜虾去壳可食部"))
    add(ingredientRef("河虾", "hexia", "hx", 87.0, 16.4, 2.4, 0.5, 0.0, 0.0, 0.0, 133.0, 329.0,
        ingredients = listOf("虾"), method = "生食材", source = "中国食物成分表：河虾", scope = "可食部"))
    add(ingredientRef("皮皮虾", "pipixia", "ppx", 101.0, 20.6, 1.7, 0.3, 0.0, 0.0, 0.0, 180.0, 260.0,
        aliases = listOf("虾蛄", "螳螂虾"), ingredients = listOf("虾"), method = "生食材",
        source = "USDA/同类甲壳类参考估算", scope = "可食部"))
    add(ingredientRef("小龙虾", "xiaolongxia", "xlx", 93.0, 18.9, 1.1, 0.2, 0.0, 0.0, 0.0, 98.0, 270.0,
        aliases = listOf("克氏原螯虾"), ingredients = listOf("虾"), method = "生食材",
        source = "USDA：小龙虾（生）", scope = "可食部"))
    add(ingredientRef("螃蟹", "pangxie", "px", 95.0, 17.5, 2.6, 0.5, 0.0, 0.0, 0.0, 194.0, 259.0,
        aliases = listOf("蟹", "河蟹"), ingredients = listOf("螃蟹"), method = "生食材",
        source = "中国食物成分表：河蟹", scope = "可食部"))
    add(ingredientRef("蟹肉", "xierou", "xr2", 97.0, 19.4, 1.5, 0.3, 0.0, 0.0, 0.0, 250.0, 300.0,
        ingredients = listOf("螃蟹"), method = "生食材", source = "USDA：蟹肉", scope = "可食部"))
    add(ingredientRef("扇贝", "shanbei", "sb", 60.0, 11.1, 0.6, 0.1, 2.6, 0.0, 0.0, 140.0, 160.0,
        aliases = listOf("带子", "海扇"), ingredients = listOf("扇贝"), method = "生食材",
        source = "USDA：扇贝", scope = "可食部"))
    add(ingredientRef("蛏子", "chengzi", "cz", 40.0, 7.3, 0.3, 0.1, 2.1, 0.0, 0.0, 120.0, 140.0,
        aliases = listOf("缢蛏", "蛏"), ingredients = listOf("蛏子"), method = "生食材",
        source = "USDA/同类双壳贝参考估算", scope = "可食部"))
    add(ingredientRef("海参", "haishen", "hs6", 56.0, 12.1, 0.2, 0.1, 0.0, 0.0, 0.0, 243.0, 43.0,
        ingredients = listOf("海参"), method = "生食材", source = "USDA：海参（生）", scope = "可食部"))
    add(ingredientRef("海螺", "hailuo", "hl", 119.0, 19.8, 1.0, 0.2, 7.6, 0.0, 0.0, 206.0, 250.0,
        aliases = listOf("螺肉", "海螺肉"), ingredients = listOf("海螺"), method = "生食材",
        source = "USDA：海螺（生）", scope = "可食部"))
    add(ingredientRef("螺蛳", "luosi", "ls2", 90.0, 15.0, 1.4, 0.3, 3.0, 0.0, 0.0, 130.0, 180.0,
        ingredients = listOf("螺蛳"), method = "生食材", source = "USDA/同类淡水螺参考估算", scope = "可食部"))
    add(ingredientRef("金枪鱼", "jinqiangyu", "jqy", 132.0, 28.2, 1.3, 0.4, 0.0, 0.0, 0.0, 45.0, 252.0,
        aliases = listOf("吞拿鱼", "鲔鱼"), ingredients = listOf("金枪鱼"), method = "生食材",
        source = "USDA：金枪鱼（生）", scope = "生重"))
    add(ingredientRef("龙利鱼", "longliyu", "lly", 79.0, 17.2, 0.7, 0.2, 0.0, 0.0, 0.0, 80.0, 300.0,
        aliases = listOf("舌鳎", "踏板鱼"), ingredients = listOf("龙利鱼"), method = "生食材",
        source = "USDA/同科鱼类参考估算", scope = "市售也可能是巴沙鱼"))
    add(ingredientRef("多宝鱼", "duobaoyu", "dby", 101.0, 18.3, 2.7, 0.7, 0.0, 0.0, 0.0, 90.0, 310.0,
        aliases = listOf("大菱鲆"), ingredients = listOf("多宝鱼"), method = "生食材",
        source = "同类比目鱼基础数据估算", scope = "生重"))
    add(ingredientRef("鱼头", "yutou2", "yt2", 97.0, 16.4, 3.6, 0.8, 0.0, 0.0, 0.0, 85.0, 250.0,
        ingredients = listOf("鱼"), method = "生食材", source = "综合鱼类参考估算", scope = "具体鱼种差异明显"))
    add(ingredientRef("鳜鱼", "guiyu", "gy", 117.0, 19.9, 4.2, 0.8, 0.0, 0.0, 0.0, 68.0, 295.0,
        aliases = listOf("桂鱼", "季花鱼"), ingredients = listOf("鳜鱼"), method = "生食材",
        source = "中国食物成分表：鳜鱼", scope = "可食部"))
    add(ingredientRef("瑶柱", "yaozhu", "yz2", 77.0, 17.7, 0.7, 0.2, 1.5, 0.0, 0.0, 340.0, 300.0,
        aliases = listOf("干贝", "扇贝柱"), ingredients = listOf("扇贝"), method = "干食材",
        source = "USDA：干贝", scope = "干重；泡发后按实际重量"))
    add(ingredientRef("鱼豆腐", "yudoufu", "ydf", 163.0, 11.0, 8.0, 2.0, 12.0, 0.0, 2.0, 760.0, 90.0,
        ingredients = listOf("鱼"), method = "加工食品", source = "鱼糜制品营养标签典型范围", scope = "鱼糜比例影响"))
    add(ingredientRef("蟹柳", "xieliu", "xl", 98.0, 8.0, 1.2, 0.3, 14.0, 0.0, 6.0, 710.0, 60.0,
        aliases = listOf("蟹味棒", "鱼糜蟹棒"), ingredients = listOf("鱼"), method = "加工食品",
        source = "鱼糜制品营养标签典型范围", scope = "通常不含真实蟹肉"))
    add(ingredientRef("鱼蛋", "yudan", "yd", 118.0, 12.0, 2.5, 0.7, 12.0, 0.0, 3.0, 650.0, 80.0,
        aliases = listOf("鱼丸", "鱼蛋丸"), ingredients = listOf("鱼"), method = "加工食品",
        source = "鱼丸营养标签典型范围", scope = "品牌与鱼糜比例影响"))
    add(ingredientRef("云吞", "yuntun", "yt4", 201.0, 8.0, 7.0, 2.0, 27.0, 1.2, 2.0, 490.0, 150.0,
        aliases = listOf("馄饨", "抄手"), ingredients = listOf("面粉", "猪肉"), method = "煮",
        source = "标准猪肉馄饨配方估算", scope = "熟重；馅料与汤底影响"))
    add(ingredientRef("海鲜", "haixian", "hx2", 96.0, 17.0, 2.2, 0.5, 1.0, 0.0, 0.0, 180.0, 250.0,
        ingredients = listOf("海鲜"), method = "生食材", source = "综合同类参考估算", scope = "鱼虾贝类泛称"))
    add(ingredientRef("螺蛳汤", "luositang", "lst", 30.0, 3.0, 0.8, 0.2, 2.0, 0.2, 0.5, 380.0, 90.0,
        ingredients = listOf("螺蛳"), method = "汤", source = "螺蛳粉汤底典型范围", scope = "汤底估算"))
}
private fun MutableList<CatalogSpec>.addVegetableEntries() {
    add(ingredientRef("大蒜", "dasuan", "ds", 128.0, 4.5, 0.2, 0.0, 27.6, 1.1, 1.0, 19.6, 302.0,
        aliases = listOf("蒜", "蒜瓣", "蒜头"), ingredients = listOf("大蒜"), method = "生食材",
        source = "中国食物成分表：大蒜（鲜）", scope = "可食部"))
    add(ingredientRef("小葱", "xiaocong", "xc3", 27.0, 1.7, 0.3, 0.0, 5.0, 2.0, 1.5, 16.0, 240.0,
        aliases = listOf("香葱", "葱花"), ingredients = listOf("葱"), method = "生食材",
        source = "USDA/中国食物成分表：小葱", scope = "可食部"))
    add(ingredientRef("大葱", "dacong", "dc", 32.0, 1.7, 0.3, 0.0, 6.5, 2.2, 2.0, 16.0, 220.0,
        aliases = listOf("葱", "京葱"), ingredients = listOf("葱"), method = "生食材",
        source = "中国食物成分表：大葱", scope = "可食部"))
    add(ingredientRef("姜", "jiang", "j", 46.0, 1.3, 0.6, 0.2, 10.3, 2.7, 1.0, 14.0, 295.0,
        aliases = listOf("生姜", "老姜"), ingredients = listOf("姜"), method = "生食材",
        source = "USDA/中国食物成分表：姜", scope = "可食部"))
    add(ingredientRef("辣椒", "lajiao", "lj", 27.0, 1.3, 0.4, 0.1, 5.7, 1.8, 2.4, 7.0, 240.0,
        aliases = listOf("尖椒", "鲜辣椒"), ingredients = listOf("辣椒"), method = "生食材",
        source = "中国食物成分表：辣椒（青尖）", scope = "品种辣度与营养略有差异"))
    add(ingredientRef("杭椒", "hangjiao", "hj2", 32.0, 1.4, 0.4, 0.1, 6.8, 2.0, 2.8, 6.0, 240.0,
        ingredients = listOf("辣椒"), method = "生食材", source = "同类鲜辣椒基础数据估算", scope = "可食部"))
    add(ingredientRef("彩椒", "caijiao", "cj", 31.0, 1.0, 0.3, 0.0, 6.0, 2.1, 4.2, 3.0, 211.0,
        aliases = listOf("甜椒", "灯笼椒"), ingredients = listOf("彩椒"), method = "生食材",
        source = "USDA：甜椒", scope = "红黄彩椒混合参考"))
    add(ingredientRef("泡椒", "paojiao", "pj", 35.0, 1.2, 0.3, 0.1, 7.0, 1.5, 2.0, 1200.0, 190.0,
        ingredients = listOf("辣椒"), method = "腌制品", source = "泡椒营养标签典型范围", scope = "钠随盐水浓度变化"))
    add(ingredientRef("剁椒", "duojiao", "dj2", 57.0, 1.4, 0.5, 0.1, 10.8, 1.7, 5.9, 2443.0, 211.0,
        aliases = listOf("剁辣椒", "辣椒碎"), ingredients = listOf("辣椒"), method = "腌制品",
        source = "中国食物成分表：剁辣椒", scope = "腌制食品，钠高"))
    add(ingredientRef("黑椒", "heijiao", "hj3", 251.0, 10.4, 3.3, 1.4, 64.0, 25.3, 0.6, 20.0, 1329.0,
        aliases = listOf("黑胡椒", "黑胡椒粒"), ingredients = listOf("黑胡椒"), method = "香辛料",
        source = "USDA：黑胡椒", scope = "通常使用量很小"))
    add(ingredientRef("藤椒", "tengjiao", "tj", 282.0, 7.0, 9.0, 2.0, 60.0, 27.0, 2.0, 20.0, 1400.0,
        aliases = listOf("青花椒", "竹叶花椒"), ingredients = listOf("藤椒"), method = "香辛料",
        source = "同类花椒基础数据估算", scope = "干香辛料"))
    add(ingredientRef("花椒", "huajiao", "hj4", 316.0, 6.7, 8.9, 2.0, 66.5, 28.7, 1.0, 47.4, 204.0,
        ingredients = listOf("花椒"), method = "香辛料", source = "中国食物成分表：花椒", scope = "干香辛料"))
    add(ingredientRef("青菜", "qingcai", "qc3", 16.0, 1.5, 0.3, 0.0, 2.7, 1.6, 1.0, 38.0, 220.0,
        aliases = listOf("绿叶菜", "小青菜"), ingredients = listOf("青菜"), method = "生食材",
        source = "中国食物成分表：小白菜类", scope = "绿叶菜泛称"))
    add(ingredientRef("菜心", "caixin", "cx", 20.0, 2.0, 0.3, 0.0, 3.0, 1.8, 1.2, 40.0, 250.0,
        aliases = listOf("广东菜心"), ingredients = listOf("菜心"), method = "生食材",
        source = "中国食物成分表：菜心", scope = "可食部"))
    add(ingredientRef("菜薹", "caitai", "ct", 25.0, 2.5, 0.4, 0.1, 3.8, 2.0, 1.2, 42.0, 260.0,
        aliases = listOf("红菜薹", "油菜薹"), ingredients = listOf("菜薹"), method = "生食材",
        source = "中国食物成分表：菜薹", scope = "可食部"))
    add(ingredientRef("芥菜", "jiecai", "jc3", 24.0, 2.0, 0.4, 0.1, 4.7, 2.0, 1.0, 30.0, 281.0,
        ingredients = listOf("芥菜"), method = "生食材", source = "USDA/中国食物成分表：芥菜", scope = "可食部"))
    add(ingredientRef("雪菜", "xuecai", "xc4", 25.0, 2.0, 0.4, 0.1, 4.0, 2.0, 1.0, 1450.0, 230.0,
        aliases = listOf("雪里蕻", "腌雪菜"), ingredients = listOf("芥菜"), method = "腌制品",
        source = "腌雪菜营养标签典型范围", scope = "盐渍食品，钠随品牌变化"))
    add(ingredientRef("梅菜", "meicai", "mc2", 45.0, 2.5, 0.6, 0.1, 8.0, 3.0, 2.0, 1800.0, 350.0,
        aliases = listOf("梅干菜", "霉干菜"), ingredients = listOf("芥菜"), method = "腌制品",
        source = "梅干菜典型营养范围", scope = "按泡发前重量记录"))
    add(ingredientRef("冬笋", "dongsun", "ds2", 23.0, 2.6, 0.2, 0.0, 3.6, 1.8, 0.5, 2.0, 420.0,
        ingredients = listOf("冬笋"), method = "生食材", source = "中国食物成分表：冬笋", scope = "可食部"))
    add(ingredientRef("笋干", "sungnan", "sg", 275.0, 26.0, 1.4, 0.3, 42.0, 22.0, 3.0, 45.0, 1200.0,
        aliases = listOf("笋干片", "竹笋干"), ingredients = listOf("竹笋"), method = "干食材",
        source = "USDA/中国食物成分表：笋干", scope = "干重；泡发后按实际重量"))
    add(ingredientRef("百合", "baihe", "bh", 166.0, 3.2, 0.1, 0.0, 38.8, 1.7, 2.0, 7.0, 510.0,
        aliases = listOf("鲜百合", "百合瓣"), ingredients = listOf("百合"), method = "生食材",
        source = "中国食物成分表：百合", scope = "可食部"))
    add(ingredientRef("豌豆苗", "wandoumiao", "wdm", 34.0, 4.0, 0.8, 0.2, 4.0, 2.5, 1.5, 18.0, 300.0,
        aliases = listOf("豌豆尖", "龙须豌豆苗"), ingredients = listOf("豌豆苗"), method = "生食材",
        source = "USDA/中国食物成分表", scope = "可食部"))
    add(ingredientRef("芦蒿", "luhao", "lh", 19.0, 3.6, 0.2, 0.0, 2.0, 2.0, 0.5, 25.0, 300.0,
        aliases = listOf("蒌蒿", "藜蒿"), ingredients = listOf("芦蒿"), method = "生食材",
        source = "同类水生蔬菜基础数据估算", scope = "可食部"))
    add(ingredientRef("儿菜", "ercai", "ec", 14.0, 1.5, 0.2, 0.0, 2.2, 1.5, 0.8, 25.0, 220.0,
        aliases = listOf("抱子芥", "娃娃菜头"), ingredients = listOf("儿菜"), method = "生食材",
        source = "同类芥菜基础数据估算", scope = "可食部"))
    add(ingredientRef("鸡毛菜", "jimaocai", "jmc", 15.0, 1.5, 0.3, 0.0, 2.2, 1.2, 0.7, 30.0, 230.0,
        aliases = listOf("鸡毛青菜", "嫩青菜"), ingredients = listOf("青菜"), method = "生食材",
        source = "同类小白菜基础数据估算", scope = "可食部"))
    add(ingredientRef("蒜苗", "suanmiao", "sm", 40.0, 2.1, 0.4, 0.1, 8.0, 2.0, 2.5, 5.0, 220.0,
        aliases = listOf("青蒜", "蒜毫"), ingredients = listOf("蒜苗"), method = "生食材",
        source = "USDA/中国食物成分表", scope = "可食部"))
    add(ingredientRef("豆角", "doujiao", "dj3", 35.0, 2.5, 0.2, 0.0, 6.7, 2.0, 1.5, 29.0, 207.0,
        aliases = listOf("长豆角", "豇豆"), ingredients = listOf("豆角"), method = "生食材",
        source = "USDA/中国食物成分表：豇豆", scope = "可食部"))
    add(ingredientRef("蘑菇", "mogu", "mg", 24.0, 2.7, 0.1, 0.0, 4.1, 0.7, 2.0, 6.0, 312.0,
        aliases = listOf("鲜蘑", "白蘑菇"), ingredients = listOf("蘑菇"), method = "生食材",
        source = "USDA：蘑菇（鲜）", scope = "鲜蘑菇泛称"))
    add(ingredientRef("菌菇", "jungu", "jg", 30.0, 2.6, 0.3, 0.0, 5.5, 2.0, 1.5, 12.0, 300.0,
        aliases = listOf("菌类", "菇类"), ingredients = listOf("菌菇"), method = "生食材",
        source = "综合同类参考估算", scope = "多种食用菌泛称"))
    add(ingredientRef("鸡腿菇", "jituigu", "jtg", 32.0, 3.0, 0.3, 0.0, 5.5, 2.0, 1.0, 10.0, 320.0,
        aliases = listOf("毛头鬼伞", "鸡腿蘑"), ingredients = listOf("鸡腿菇"), method = "生食材",
        source = "同类鲜菌菇基础数据估算", scope = "可食部"))
    add(ingredientRef("竹荪", "zhusun", "zs", 155.0, 17.8, 3.0, 0.5, 26.0, 21.0, 1.0, 60.0, 1100.0,
        aliases = listOf("竹笙", "竹参"), ingredients = listOf("竹荪"), method = "干食材",
        source = "USDA/中国食物成分表：竹荪（干）", scope = "干重；泡发后按实际重量"))
    add(ingredientRef("裙带菜", "qundaicai", "qdc", 45.0, 3.0, 0.6, 0.1, 9.1, 2.0, 0.6, 872.0, 520.0,
        aliases = listOf("海芥菜", "裙带"), ingredients = listOf("裙带菜"), method = "生食材",
        source = "USDA：裙带菜（生）", scope = "可食部"))
    add(ingredientRef("橄榄菜", "ganlancai", "glc", 190.0, 3.0, 15.0, 2.0, 10.0, 4.0, 2.0, 1800.0, 180.0,
        ingredients = listOf("芥菜"), method = "腌制品", source = "橄榄菜营养标签典型范围", scope = "油盐含量高"))
    add(ingredientRef("酸菜", "suancai", "sc2", 22.0, 1.5, 0.3, 0.0, 3.5, 1.5, 0.8, 1300.0, 180.0,
        ingredients = listOf("白菜"), method = "腌制品", source = "酸菜营养标签典型范围", scope = "盐渍食品"))
    add(ingredientRef("酸萝卜", "suanluobo", "slb", 24.0, 1.0, 0.2, 0.0, 4.5, 1.5, 1.0, 1100.0, 160.0,
        ingredients = listOf("白萝卜"), method = "腌制品", source = "酸萝卜营养标签典型范围", scope = "盐渍食品"))
    add(ingredientRef("酸豆角", "suandoujiao", "sdj", 30.0, 1.8, 0.3, 0.0, 6.0, 2.5, 1.0, 1200.0, 180.0,
        ingredients = listOf("豆角"), method = "腌制品", source = "酸豆角营养标签典型范围", scope = "盐渍食品"))
    add(ingredientRef("外婆菜", "waipocai", "wpc", 110.0, 3.0, 7.0, 1.0, 9.0, 4.0, 2.0, 1500.0, 200.0,
        ingredients = listOf("腌菜"), method = "腌制品", source = "综合腌制蔬菜参考估算", scope = "复合腌菜泛称"))
    add(ingredientRef("腌菜", "yancai", "yc3", 25.0, 1.5, 0.3, 0.0, 4.0, 1.6, 1.0, 1400.0, 190.0,
        ingredients = listOf("蔬菜"), method = "腌制品", source = "综合腌制蔬菜参考估算", scope = "盐渍蔬菜泛称"))
    add(ingredientRef("榨菜", "zhacai", "zc2", 29.0, 2.2, 0.3, 0.0, 4.5, 2.1, 1.0, 4252.0, 363.0,
        ingredients = listOf("芥菜"), method = "腌制品", source = "中国食物成分表：榨菜", scope = "钠非常高"))
    add(ingredientRef("蔬菜", "shucai", "sc3", 25.0, 1.8, 0.3, 0.0, 4.5, 2.0, 1.5, 40.0, 250.0,
        ingredients = listOf("蔬菜"), method = "生食材", source = "综合同类参考估算", scope = "多种蔬菜泛称"))
    add(ingredientRef("菜", "cai", "c", 40.0, 2.0, 0.5, 0.1, 6.0, 2.0, 1.5, 80.0, 250.0,
        ingredients = listOf("蔬菜"), method = "生食材", source = "综合同类参考估算", scope = "菜谱遗留的泛称"))
}
private fun MutableList<CatalogSpec>.addPantryEntries() {
    add(ingredientRef("豆腐丝", "doufusi", "dfs", 260.0, 26.0, 14.0, 2.0, 8.0, 2.0, 1.0, 16.0, 800.0,
        aliases = listOf("干豆腐丝", "千张丝"), ingredients = listOf("大豆"), method = "干食材",
        source = "中国食物成分表：豆腐丝", scope = "干重"))
    add(ingredientRef("香干", "xianggan", "xg2", 197.0, 15.8, 10.8, 1.8, 7.0, 1.0, 1.0, 300.0, 150.0,
        aliases = listOf("豆腐干", "五香豆干"), ingredients = listOf("大豆"), method = "加工食品",
        source = "中国食物成分表：豆腐干", scope = "品牌与卤制配方影响"))
    add(ingredientRef("豆豉", "douchi", "dc2", 259.0, 24.0, 3.0, 0.5, 39.0, 5.0, 6.0, 2300.0, 700.0,
        aliases = listOf("发酵豆豉", "阳江豆豉"), ingredients = listOf("黄豆"), method = "发酵",
        source = "中国食物成分表：豆豉", scope = "发酵调味食品，钠高"))
    add(ingredientRef("面饼", "mianbing", "mb", 362.0, 10.4, 1.8, 0.3, 78.7, 2.0, 1.0, 25.0, 120.0,
        aliases = listOf("干面饼", "方便面饼"), ingredients = listOf("小麦粉"), method = "干食材",
        source = "中国食物成分表：面条（均值）", scope = "干重；不同面饼含油差异大"))
    add(ingredientRef("面条", "miantiao", "mt", 137.0, 4.5, 0.7, 0.2, 28.0, 1.2, 0.3, 4.0, 30.0,
        aliases = listOf("熟面条", "白面条"), ingredients = listOf("小麦粉"), method = "煮",
        source = "中国食物成分表：面条（熟）", scope = "熟重"))
    add(ingredientRef("面皮", "mianpi", "mp", 153.0, 5.0, 0.8, 0.2, 31.0, 1.3, 0.3, 10.0, 40.0,
        aliases = listOf("凉皮原料", "熟面皮"), ingredients = listOf("小麦粉"), method = "熟食材",
        source = "中国食物成分表：面皮", scope = "熟重"))
    add(ingredientRef("面片", "mianpian", "mp2", 154.0, 5.4, 0.9, 0.2, 30.0, 1.3, 0.3, 12.0, 45.0,
        aliases = listOf("面片汤原料", "熟面片"), ingredients = listOf("小麦粉"), method = "熟食材",
        source = "中国食物成分表：面制品", scope = "熟重"))
    add(ingredientRef("全麦面包", "quanmaimianbao", "qmmb", 252.0, 12.5, 3.4, 0.7, 43.0, 6.8, 4.0, 400.0, 250.0,
        ingredients = listOf("全麦粉"), method = "烤", source = "USDA：全麦面包", scope = "品牌差异明显"))
    add(ingredientRef("面包", "mianbao", "mb2", 265.0, 9.0, 3.2, 0.7, 49.0, 2.7, 5.0, 490.0, 115.0,
        aliases = listOf("白面包", "吐司"), ingredients = listOf("小麦粉"), method = "烤",
        source = "USDA：白面包", scope = "品牌差异明显"))
    add(ingredientRef("全麦饼", "quanmaibing", "qmb", 280.0, 10.0, 7.0, 1.0, 46.0, 6.0, 3.0, 400.0, 250.0,
        ingredients = listOf("全麦粉"), method = "熟食材", source = "USDA：全麦面饼", scope = "熟重"))
    add(ingredientRef("饼", "bing", "b", 300.0, 8.0, 8.0, 2.0, 49.0, 2.0, 2.0, 400.0, 120.0,
        ingredients = listOf("小麦粉"), method = "熟食材", source = "综合面制品参考估算", scope = "面饼泛称"))
    add(ingredientRef("油条", "youtiao", "yt5", 388.0, 6.9, 17.6, 4.0, 51.0, 1.5, 1.0, 585.0, 90.0,
        ingredients = listOf("小麦粉"), method = "油炸", source = "中国食物成分表：油条", scope = "熟重"))
    add(ingredientRef("糯米圆子", "nuomiyuanzi", "nmyz", 230.0, 4.0, 5.0, 1.0, 42.0, 0.8, 5.0, 50.0, 60.0,
        aliases = listOf("汤圆", "糯米丸子"), ingredients = listOf("糯米粉"), method = "煮",
        source = "标准糯米圆子配方估算", scope = "熟重；馅料影响"))
    add(ingredientRef("米浆", "mijiang", "mj", 60.0, 1.4, 0.3, 0.1, 13.0, 0.3, 0.1, 5.0, 20.0,
        ingredients = listOf("大米"), method = "液体", source = "标准米浆配方估算", scope = "液体"))
    add(ingredientRef("玉米面", "yumimian", "ymm", 365.0, 8.1, 3.6, 0.6, 76.9, 3.6, 0.6, 35.0, 287.0,
        aliases = listOf("玉米粉", "玉米面（黄）"), ingredients = listOf("玉米"), method = "干食材",
        source = "中国食物成分表：玉米面", scope = "干重"))
    add(ingredientRef("木薯淀粉", "mushudianfen", "msdf", 357.0, 0.2, 0.1, 0.0, 88.7, 0.9, 0.0, 1.0, 11.0,
        aliases = listOf("木薯粉", "木薯生粉"), ingredients = listOf("木薯"), method = "干食材",
        source = "USDA：木薯淀粉", scope = "干重"))
    add(ingredientRef("琼脂", "qiongzhi", "qz3", 306.0, 6.2, 0.3, 0.1, 80.9, 9.0, 0.0, 102.0, 112.0,
        aliases = listOf("琼胶", "寒天粉"), ingredients = listOf("海藻"), method = "干食材",
        source = "USDA：琼脂", scope = "干重"))
    add(ingredientRef("冰粉", "bingfen", "bf", 155.0, 0.1, 0.0, 0.0, 38.0, 0.1, 35.0, 10.0, 5.0,
        ingredients = listOf("冰粉籽"), method = "甜品", source = "冰粉成品典型范围", scope = "是否加糖影响能量"))
    add(ingredientRef("仙草", "xiancao", "xc5", 35.0, 0.5, 0.1, 0.0, 8.0, 1.0, 5.0, 20.0, 30.0,
        aliases = listOf("凉粉草", "仙草冻"), ingredients = listOf("仙草"), method = "甜品",
        source = "仙草冻成品典型范围", scope = "熟重"))
    add(ingredientRef("龟苓膏粉", "guilinggaofen", "glgf", 350.0, 1.0, 0.5, 0.1, 86.0, 1.0, 40.0, 50.0, 80.0,
        ingredients = listOf("龟苓膏粉"), method = "干食材", source = "龟苓膏粉营养标签典型范围", scope = "干重"))
    add(ingredientRef("桃胶", "taojiao", "tj2", 175.0, 4.0, 0.2, 0.0, 40.0, 1.0, 2.0, 30.0, 90.0,
        ingredients = listOf("桃胶"), method = "干食材", source = "桃胶营养标签典型范围", scope = "干重"))
    add(ingredientRef("燕窝", "yanwo", "yw", 280.0, 44.0, 0.3, 0.0, 22.0, 0.0, 8.0, 300.0, 100.0,
        ingredients = listOf("燕窝"), method = "干食材", source = "燕窝营养标签典型范围", scope = "干重；品质差异很大"))
    add(ingredientRef("冰", "bingbing", "bb", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        aliases = listOf("冰块", "食用冰"), ingredients = listOf("水"), method = "冷冻",
        source = "基础物质", scope = "纯水冰"))
    add(ingredientRef("茶", "cha", "ch", 1.0, 0.1, 0.0, 0.0, 0.2, 0.0, 0.0, 3.0, 10.0,
        aliases = listOf("茶水", "清茶"), ingredients = listOf("茶"), method = "冲泡",
        source = "中国食物成分表：茶汤", scope = "无糖茶汤"))
    add(ingredientRef("绿茶", "lvcha", "lc3", 1.0, 0.1, 0.0, 0.0, 0.2, 0.0, 0.0, 2.0, 20.0,
        ingredients = listOf("绿茶"), method = "冲泡", source = "中国食物成分表：绿茶茶汤", scope = "无糖茶汤"))
    add(ingredientRef("乌龙茶", "wulongcha", "wlc", 1.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0, 2.0, 12.0,
        ingredients = listOf("乌龙茶"), method = "冲泡", source = "乌龙茶茶汤典型范围", scope = "无糖茶汤"))
    add(ingredientRef("菊花", "juhua", "jh", 242.0, 6.0, 3.3, 0.5, 48.0, 15.0, 2.0, 20.0, 1500.0,
        aliases = listOf("干菊花", "杭白菊"), ingredients = listOf("菊花"), method = "干食材",
        source = "USDA/中国食物成分表：菊花", scope = "干重；泡茶用量很小"))
    add(ingredientRef("抹茶", "mocha", "mc3", 324.0, 29.0, 5.0, 1.0, 40.0, 25.0, 3.0, 10.0, 2500.0,
        aliases = listOf("抹茶粉", "绿茶粉"), ingredients = listOf("绿茶"), method = "干食材",
        source = "USDA：抹茶粉", scope = "干重"))
    add(ingredientRef("咖啡", "kafei", "kf", 2.0, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 49.0,
        aliases = listOf("黑咖啡", "无糖咖啡"), ingredients = listOf("咖啡"), method = "冲泡",
        source = "USDA：咖啡（冲泡）", scope = "无糖无奶"))
    add(ingredientRef("芝麻", "zhima", "zm", 573.0, 17.7, 49.7, 7.0, 23.4, 11.8, 0.3, 11.0, 468.0,
        aliases = listOf("白芝麻", "黑芝麻"), ingredients = listOf("芝麻"), method = "干食材",
        source = "USDA：芝麻", scope = "干重"))
    add(ingredientRef("奇亚籽", "qiyazi", "qyz", 486.0, 16.5, 30.7, 3.3, 42.1, 34.4, 0.0, 16.0, 407.0,
        ingredients = listOf("奇亚籽"), method = "干食材", source = "USDA：奇亚籽", scope = "干重"))
    add(ingredientRef("花生酱", "huashengjiang", "hsj", 588.0, 25.1, 50.4, 10.3, 20.0, 6.0, 9.0, 476.0, 649.0,
        aliases = listOf("花生抹酱", "幼滑花生酱"), ingredients = listOf("花生"), method = "加工食品",
        source = "USDA：花生酱", scope = "品牌含糖与钠差异明显"))
    add(ingredientRef("希腊酸奶", "xilasuannai", "xlsn", 97.0, 9.0, 5.0, 3.2, 3.9, 0.0, 3.0, 36.0, 141.0,
        aliases = listOf("浓缩酸奶", "希腊式酸奶"), ingredients = listOf("牛奶"), method = "发酵",
        source = "USDA：希腊酸奶（全脂）", scope = "脂肪与糖随产品变化"))
    add(ingredientRef("坚果", "jianguo", "jg2", 607.0, 20.0, 54.0, 7.0, 21.0, 7.0, 4.0, 15.0, 600.0,
        aliases = listOf("混合坚果", "果仁"), ingredients = listOf("坚果"), method = "干食材",
        source = "USDA：混合坚果", scope = "不同坚果组成差异明显"))
    add(ingredientRef("水果", "shuiguo", "sg2", 52.0, 0.6, 0.2, 0.0, 13.0, 2.2, 9.0, 3.0, 150.0,
        ingredients = listOf("水果"), method = "生食材", source = "综合同类参考估算", scope = "水果泛称"))
    add(ingredientRef("果干", "guogan", "gg", 320.0, 3.0, 1.0, 0.2, 78.0, 6.0, 60.0, 15.0, 600.0,
        aliases = listOf("混合果干", "葡萄干"), ingredients = listOf("水果"), method = "干食材",
        source = "USDA：混合果干", scope = "干重；糖浓缩"))
    add(ingredientRef("山楂", "shanzha", "sz", 95.0, 0.5, 0.6, 0.1, 25.1, 3.1, 6.0, 5.0, 299.0,
        aliases = listOf("红果", "山楂果"), ingredients = listOf("山楂"), method = "生食材",
        source = "USDA/中国食物成分表", scope = "可食部"))
    add(ingredientRef("乌梅", "wumei", "wm", 219.0, 6.0, 0.7, 0.2, 50.0, 7.0, 20.0, 100.0, 500.0,
        aliases = listOf("乌梅干", "梅干"), ingredients = listOf("梅子"), method = "干食材",
        source = "乌梅营养标签典型范围", scope = "干重"))
    add(ingredientRef("桂花", "guihua", "gh", 238.0, 4.0, 1.0, 0.2, 55.0, 10.0, 20.0, 15.0, 800.0,
        aliases = listOf("桂花干", "糖桂花原料"), ingredients = listOf("桂花"), method = "干食材",
        source = "桂花营养标签典型范围", scope = "干重；糖桂花另含糖"))
    add(ingredientRef("酒酿", "jiuniang", "jn", 100.0, 1.6, 0.2, 0.0, 21.0, 0.2, 12.0, 5.0, 40.0,
        aliases = listOf("醪糟", "甜酒酿"), ingredients = listOf("糯米"), method = "发酵",
        source = "酒酿成品典型范围", scope = "含糖与少量酒精"))
    add(ingredientRef("可乐", "kele", "kl", 42.0, 0.0, 0.0, 0.0, 10.6, 0.0, 10.6, 11.0, 2.0,
        aliases = listOf("汽水", "碳酸饮料"), ingredients = listOf("可乐"), method = "饮料",
        source = "可乐营养标签", scope = "含糖饮料"))
    add(ingredientRef("啤酒", "pijiu", "pj2", 43.0, 0.5, 0.0, 0.0, 3.6, 0.0, 0.0, 4.0, 27.0,
        ingredients = listOf("啤酒"), method = "发酵", source = "USDA：普通啤酒", scope = "酒精供能"))
    add(ingredientRef("米酒", "mijiu", "mj2", 77.0, 0.3, 0.0, 0.0, 12.0, 0.0, 0.0, 5.0, 20.0,
        aliases = listOf("糯米酒", "江米酒"), ingredients = listOf("糯米"), method = "发酵",
        source = "中国食物成分表：米酒", scope = "含酒精与糖"))
    add(ingredientRef("巧克力", "qiaokeli", "qkl", 546.0, 4.9, 31.3, 18.5, 61.2, 3.4, 54.0, 20.0, 372.0,
        ingredients = listOf("可可"), method = "甜品", source = "USDA：黑巧克力", scope = "可可含量影响糖"))
    add(ingredientRef("豆沙", "dousha", "ds3", 240.0, 5.5, 1.0, 0.2, 53.0, 4.0, 35.0, 30.0, 300.0,
        aliases = listOf("红豆沙", "豆沙馅"), ingredients = listOf("红豆"), method = "甜品",
        source = "豆沙成品典型范围", scope = "熟重；糖量差异大"))
    add(ingredientRef("莲蓉", "lianrong", "lr2", 320.0, 4.0, 8.0, 2.0, 58.0, 2.0, 40.0, 50.0, 200.0,
        ingredients = listOf("莲子"), method = "甜品", source = "莲蓉成品典型范围", scope = "熟重；油糖量差异大"))
    add(ingredientRef("乳清蛋白", "ruqingdanbai", "rqdb", 400.0, 80.0, 5.0, 1.5, 8.0, 0.0, 3.0, 300.0, 500.0,
        aliases = listOf("乳清蛋白粉", "蛋白粉"), ingredients = listOf("牛奶"), method = "干食材",
        source = "乳清蛋白粉营养标签典型范围", scope = "干重"))
    add(ingredientRef("调味料", "tiaoweiliao", "twl", 120.0, 4.0, 2.0, 0.4, 20.0, 1.0, 5.0, 2500.0, 200.0,
        aliases = listOf("复合调味料", "调味粉"), ingredients = listOf("调味料"), method = "调料",
        source = "复合调味料营养标签典型范围", scope = "复合泛称；盐含量高"))
    add(ingredientRef("酱料", "jiangliao", "jl2", 130.0, 4.0, 4.0, 0.8, 20.0, 1.0, 8.0, 2400.0, 180.0,
        aliases = listOf("复合酱料", "调味酱"), ingredients = listOf("酱料"), method = "调料",
        source = "复合酱料营养标签典型范围", scope = "盐和糖随配方变化"))
    add(ingredientRef("炸酱", "zhajiang", "zj", 150.0, 8.0, 8.0, 1.5, 12.0, 1.0, 4.0, 1800.0, 250.0,
        ingredients = listOf("黄豆酱"), method = "调料", source = "炸酱成品典型范围", scope = "熟重"))
    add(ingredientRef("辣酱", "lijiang", "lj2", 90.0, 2.0, 2.0, 0.4, 14.0, 1.0, 7.0, 2200.0, 200.0,
        aliases = listOf("辣椒酱", "辣味酱"), ingredients = listOf("辣椒"), method = "调料",
        source = "辣椒酱营养标签典型范围", scope = "钠高"))
    add(ingredientRef("辣油", "layou", "ly3", 884.0, 0.0, 100.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        aliases = listOf("辣椒油", "红油"), ingredients = listOf("油"), method = "调料",
        source = "中国食物成分表：辣椒油", scope = "脂肪供能"))
    add(ingredientRef("卤汁", "luzhi", "lz2", 35.0, 2.0, 1.0, 0.3, 3.0, 0.2, 1.0, 900.0, 100.0,
        aliases = listOf("卤水", "卤汤"), ingredients = listOf("调味料"), method = "汤",
        source = "卤汁典型范围", scope = "喝入量影响钠"))
    add(ingredientRef("椒盐", "jiaoyan", "jy2", 350.0, 8.0, 1.0, 0.2, 70.0, 20.0, 1.0, 30000.0, 200.0,
        ingredients = listOf("花椒", "盐"), method = "调料", source = "椒盐标准配方估算", scope = "钠高"))
}

private fun MutableList<CatalogSpec>.addRemainingDishIngredients() {
    add(ingredientRef("醋", "cu", "c3", 22.0, 0.5, 0.1, 0.0, 4.9, 0.0, 1.0, 262.0, 351.0,
        aliases = listOf("食用醋", "米醋"), ingredients = listOf("醋"), method = "调料",
        source = "中国食物成分表：醋（均值）", scope = "不同醋种酸度和糖分不同"))
    add(ingredientRef("玉米", "yumi", "ym", 112.0, 4.0, 1.2, 0.2, 22.8, 2.9, 6.2, 1.1, 238.0,
        aliases = listOf("甜玉米", "玉米棒"), ingredients = listOf("玉米"), method = "生食材",
        source = "中国食物成分表：玉米（鲜）", scope = "可食部；罐头或糯玉米另计"))
    add(ingredientRef("酸汤", "suanTang", "st2", 18.0, 0.8, 0.3, 0.1, 3.0, 0.5, 1.0, 340.0, 100.0,
        aliases = listOf("酸汤底", "番茄酸汤"), ingredients = listOf("番茄"), method = "汤",
        source = "酸汤底典型配方估算", scope = "汤底泛称；酸味来源含发酵或番茄时会不同"))
    add(ingredientRef("咖喱", "gali", "gl", 338.0, 13.0, 12.2, 2.0, 63.3, 36.9, 3.0, 1700.0, 400.0,
        aliases = listOf("咖喱粉", "咖喱酱"), ingredients = listOf("咖喱"), method = "调料",
        source = "中国食物成分表：咖喱粉", scope = "干香辛料；咖喱酱含水油另计"))
    add(ingredientRef("荷叶", "heye", "hy", 75.0, 4.0, 1.0, 0.2, 13.0, 8.0, 1.0, 10.0, 600.0,
        aliases = listOf("干荷叶", "鲜荷叶"), ingredients = listOf("荷叶"), method = "包裹用",
        source = "中国食物成分表：荷叶", scope = "包裹蒸制通常不按全叶食入；仅作可检索的配方项"))
}

private fun ingredientRef(
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
    aliases: List<String> = emptyList(),
    ingredients: List<String>,
    method: String,
    source: String,
    scope: String,
) = CatalogSpec(
    name = name,
    pinyin = pinyin,
    initials = initials,
    category = "食材",
    basis = "每100g",
    kcal = kcal,
    protein = protein,
    fat = fat,
    sat = saturatedFat,
    carbs = carbs,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    potassium = potassium,
    cholesterol = 0.0,
    aliases = aliases,
    ingredients = ingredients,
    method = method,
    source = "$source；$scope",
    confidence = if (scope.contains("泛称") || source.contains("同类")) "估算" else "参考值",
)

/** Common legumes and whole grains, all explicit per-100g dry or cooked bases. */
private fun MutableList<CatalogSpec>.addLegumeAndWholeGrainEntries() {
    add(ingredientRef("鹰嘴豆（干）", "yingzuidougan", "yzdg", 378.0, 20.5, 6.0, 0.6, 63.0, 12.2, 10.7, 24.0, 875.0,
        aliases = listOf("干鹰嘴豆", "鸡豆（干）"), ingredients = listOf("鹰嘴豆"), method = "干食材",
        source = "USDA：鹰嘴豆（干）", scope = "干重"))
    add(ingredientRef("鹰嘴豆（熟）", "yingzuidoushu", "yzds", 165.0, 8.8, 2.7, 0.3, 27.2, 7.5, 4.9, 8.0, 289.0,
        aliases = listOf("熟鹰嘴豆", "煮鹰嘴豆"), ingredients = listOf("鹰嘴豆"), method = "熟食材",
        source = "USDA：鹰嘴豆（熟）", scope = "熟重"))
    add(ingredientRef("红腰豆（干）", "hongyaodou", "hydg", 333.0, 23.6, 0.8, 0.2, 60.0, 24.9, 2.1, 12.0, 1406.0,
        aliases = listOf("红芸豆", "腰豆（干）"), ingredients = listOf("红腰豆"), method = "干食材",
        source = "USDA：红腰豆（干）", scope = "干重"))
    add(ingredientRef("白芸豆（干）", "baiyundou", "bydg", 333.0, 23.4, 0.8, 0.2, 60.0, 24.9, 3.0, 16.0, 1795.0,
        aliases = listOf("白腰豆", "芸豆（干）"), ingredients = listOf("白芸豆"), method = "干食材",
        source = "USDA：白芸豆（干）", scope = "干重"))
    add(ingredientRef("豌豆（干）", "wandougan", "wdg", 352.0, 24.6, 1.2, 0.2, 63.7, 25.5, 8.0, 15.0, 981.0,
        aliases = listOf("干豌豆", "黄豌豆"), ingredients = listOf("豌豆"), method = "干食材",
        source = "USDA：豌豆（干）", scope = "干重"))
    add(ingredientRef("扁豆（干）", "biandougan", "bdg", 353.0, 25.8, 1.1, 0.2, 60.1, 30.5, 2.0, 6.0, 955.0,
        aliases = listOf("兵豆", "小扁豆"), ingredients = listOf("扁豆"), method = "干食材",
        source = "USDA：扁豆（干）", scope = "干重"))
    add(ingredientRef("花豆（干）", "huadougan", "hdg", 330.0, 24.0, 1.0, 0.2, 59.0, 24.0, 2.0, 10.0, 1400.0,
        aliases = listOf("斑豆", "花芸豆"), ingredients = listOf("花豆"), method = "干食材",
        source = "USDA：花豆（干）", scope = "干重"))
    add(ingredientRef("蚕豆（干）", "candougan", "cdg", 341.0, 26.1, 1.5, 0.3, 58.3, 25.0, 5.7, 13.0, 1062.0,
        aliases = listOf("胡豆", "干蚕豆"), ingredients = listOf("蚕豆"), method = "干食材",
        source = "USDA：蚕豆（干）", scope = "干重"))
    add(ingredientRef("糙米（生）", "caomisheng", "cms", 370.0, 7.9, 2.9, 0.6, 77.2, 3.5, 0.7, 5.0, 250.0,
        aliases = listOf("生糙米", "糙米原料"), ingredients = listOf("糙米"), method = "生食材",
        source = "USDA：糙米（生）", scope = "干重"))
    add(ingredientRef("黑米（生）", "heimisheng", "hms", 341.0, 9.4, 2.5, 0.4, 72.2, 3.9, 0.0, 7.0, 256.0,
        aliases = listOf("生黑米", "黑米原料"), ingredients = listOf("黑米"), method = "生食材",
        source = "USDA：黑米（生）", scope = "干重"))
    add(ingredientRef("藜麦（生）", "limaisheng", "lms", 368.0, 14.1, 6.1, 0.7, 64.0, 7.0, 0.0, 5.0, 563.0,
        aliases = listOf("生藜麦", "藜麦原料"), ingredients = listOf("藜麦"), method = "生食材",
        source = "USDA：藜麦（生）", scope = "干重"))
    add(ingredientRef("青稞（生）", "qingkesheng", "qks", 354.0, 12.5, 2.3, 0.5, 73.5, 17.3, 0.8, 9.0, 452.0,
        aliases = listOf("裸大麦", "元麦"), ingredients = listOf("青稞"), method = "生食材",
        source = "USDA：大麦（生）", scope = "干重"))
    add(ingredientRef("薏米（生）", "yimisheng", "yms", 361.0, 12.8, 3.3, 0.7, 71.0, 2.0, 0.0, 5.0, 250.0,
        aliases = listOf("生薏米", "薏苡仁"), ingredients = listOf("薏米"), method = "生食材",
        source = "中国食物成分表：薏米", scope = "干重"))
    add(ingredientRef("燕麦米（生）", "yanmaimisheng", "ymms", 389.0, 16.9, 6.9, 1.2, 66.3, 10.6, 1.0, 2.0, 429.0,
        aliases = listOf("生燕麦米", "燕麦粒"), ingredients = listOf("燕麦"), method = "生食材",
        source = "USDA：燕麦（生）", scope = "干重"))
}