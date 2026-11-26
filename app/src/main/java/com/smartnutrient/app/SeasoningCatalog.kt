package com.smartnutrient.app

data class SeasoningItem(
    val id: String,
    val name: String,
    val pinyin: String,
    val initials: String,
    val category: String,
    val nutrientsPer100g: FoodNutrients,
    val aliases: List<String> = emptyList(),
    val source: String,
    val confidence: String,
)

/**
 * Seasonings stay separate from [FoodCatalog]: their usual amounts are 1-20 g,
 * and soy sauce/salt products have a sodium density far above prepared dishes.
 */
object SeasoningCatalog {
    private fun item(
        name: String,
        pinyin: String,
        initials: String,
        category: String,
        kcal: Double,
        protein: Double,
        fat: Double,
        saturatedFat: Double,
        carbs: Double,
        fiber: Double,
        sugar: Double,
        sodium: Double,
        potassium: Double,
        cholesterol: Double = 0.0,
        aliases: List<String> = emptyList(),
        source: String = "中国食物成分表/USDA；按 100g 统一",
        confidence: String = "估算",
    ) = SeasoningItem(
        id = "调料-$name",
        name = name,
        pinyin = pinyin,
        initials = initials,
        category = category,
        nutrientsPer100g = FoodNutrients(
            kcal, protein, fat, saturatedFat, carbs, fiber, sugar,
            sodium, potassium, cholesterol,
            SugarDataPolicy.addedSugarG(category, name, sugar),
        ),
        aliases = aliases,
        source = source,
        confidence = confidence,
    )

    val items: List<SeasoningItem> = listOf(
        item("生抽", "shengchou", "sc", "酱油", 64.0, 5.6, 0.1, 0.0, 10.1, 0.0, 1.0, 5757.0, 337.0,
            aliases = listOf("酱油", "酿造酱油"), source = "中国食物成分表：酱油均值", confidence = "参考值"),
        item("薄盐生抽", "baoyanshengchou", "bysc", "酱油", 59.0, 5.2, 0.1, 0.0, 9.7, 0.0, 1.0, 3920.0, 310.0,
            aliases = listOf("减盐生抽", "低盐酱油"), source = "预包装薄盐生抽标签典型范围", confidence = "标签估算"),
        item("味极鲜", "weijixian", "wjx", "酱油", 77.0, 7.4, 0.1, 0.0, 10.8, 0.0, 3.0, 5900.0, 350.0,
            aliases = listOf("味极鲜酱油"), source = "预包装味极鲜标签典型值", confidence = "标签估算"),
        item("零添加生抽", "lingtianjiashengchou", "ltjsc", "酱油", 61.0, 6.0, 0.1, 0.0, 8.3, 0.0, 1.0, 5300.0, 320.0,
            source = "零添加酿造酱油标签典型值"),
        item("老抽", "laochou", "lc", "酱油", 70.0, 5.4, 0.1, 0.0, 12.5, 0.0, 5.0, 6500.0, 280.0,
            source = "老抽营养标签典型范围"),
        item("蒸鱼豉油", "zhengyuchiyou", "zycy", "酱油", 68.0, 5.0, 0.1, 0.0, 12.0, 0.0, 4.0, 4900.0, 260.0,
            source = "蒸鱼豉油营养标签典型范围"),
        item("蚝油", "haoyou", "hy", "酱汁", 114.0, 5.5, 0.4, 0.1, 22.0, 0.3, 12.0, 4400.0, 100.0,
            source = "蚝油营养标签典型范围"),
        item("陈醋", "chencu", "cc", "醋", 32.0, 0.0, 0.0, 0.0, 3.0, 0.0, 1.5, 836.0, 715.0,
            source = "中国食物成分表：陈醋", confidence = "参考值"),
        item("香醋", "xiangcu", "xc", "醋", 67.0, 0.7, 0.1, 0.0, 14.0, 0.0, 5.0, 600.0, 120.0),
        item("米醋", "micu", "mc", "醋", 22.0, 0.5, 0.1, 0.0, 5.0, 0.0, 1.0, 250.0, 80.0),
        item("白醋", "baicu", "bc", "醋", 6.0, 0.1, 0.0, 0.0, 0.7, 0.0, 0.1, 226.0, 12.0,
            source = "中国食物成分表：白醋", confidence = "参考值"),
        item("料酒", "liaojiu", "lj", "酒", 31.0, 0.3, 0.0, 0.0, 7.0, 0.0, 1.2, 1200.0, 20.0,
            source = "料酒营养标签典型范围；碳水含少量糖/酒精"),
        item("黄酒", "huangjiu", "hj", "酒", 66.0, 0.1, 0.0, 0.0, 16.1, 0.0, 8.0, 1.0, 9.0,
            source = "中国食物成分表：江米酒", confidence = "参考值"),
        item("食盐", "shiyan", "sy", "盐", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 39311.0, 2.0,
            aliases = listOf("盐", "精盐", "细盐"), source = "中国食物成分表：精盐", confidence = "参考值"),
        item("低钠盐", "dinaayan", "dny", "盐", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24000.0, 12000.0,
            source = "低钠盐标签典型值：氯化钾部分替代氯化钠", confidence = "标签估算"),
        item("白糖", "baitang", "bt", "糖", 400.0, 0.0, 0.0, 0.0, 99.9, 0.0, 99.9, 1.0, 2.0,
            aliases = listOf("白砂糖", "砂糖"), source = "中国食物成分表：糖（白砂糖）", confidence = "参考值"),
        item("冰糖", "bingtang", "bt2", "糖", 400.0, 0.0, 0.0, 0.0, 99.3, 0.0, 95.0, 2.7, 1.0,
            source = "中国食物成分表：糖（冰糖）", confidence = "参考值"),
        item("红糖", "hongtang", "ht", "糖", 389.0, 0.7, 0.1, 0.0, 96.6, 0.0, 90.0, 18.3, 240.0,
            source = "中国食物成分表：糖（红糖）", confidence = "参考值"),
        item("蜂蜜", "fengmi", "fm", "糖", 326.0, 0.4, 0.1, 0.0, 75.6, 0.0, 72.0, 0.3, 28.0,
            source = "中国食物成分表：蜂蜜", confidence = "参考值"),
        item("食用油", "shiyongyou", "syy", "油", 899.0, 0.0, 99.9, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            aliases = listOf("植物油", "烹调油", "色拉油")),
        item("橄榄油", "ganlanyou", "gly", "油", 884.0, 0.0, 100.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        item("花生油", "huashengyou", "hsy", "油", 899.0, 0.0, 99.9, 17.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        item("菜籽油", "caiziyou", "czy", "油", 884.0, 0.0, 100.0, 7.4, 0.0, 0.0, 0.0, 0.0, 0.0),
        item("芝麻油", "zhimayou", "zmy", "油", 884.0, 0.0, 100.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            aliases = listOf("香油")),
        item("辣椒油", "lajiaoyou", "ljy", "油", 884.0, 0.0, 100.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            source = "中国食物成分表：油（辣椒油）", confidence = "参考值"),
        item("花椒油", "huajiaoyou", "hjy", "油", 884.0, 0.0, 100.0, 14.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        item("豆瓣酱", "doubanjiang", "dbj", "酱", 107.0, 4.0, 1.0, 0.2, 22.0, 2.0, 3.0, 5658.0, 585.0,
            aliases = listOf("郫县豆瓣酱", "辣豆瓣酱"), source = "中国食物成分表：郫县辣酱", confidence = "参考值"),
        item("甜面酱", "tianmianjiang", "tmj", "酱", 140.0, 5.5, 0.6, 0.1, 28.5, 1.0, 15.0, 2097.0, 189.0,
            source = "中国食物成分表：甜面酱", confidence = "参考值"),
        item("黄豆酱", "huangdoujiang", "hdj", "酱", 136.0, 12.5, 5.7, 0.9, 15.0, 2.0, 5.0, 5000.0, 300.0),
        item("番茄酱", "fanqiejiang", "fqj", "酱", 86.0, 4.9, 0.2, 0.0, 16.9, 2.2, 12.0, 989.0, 37.0,
            source = "中国食物成分表：番茄酱", confidence = "参考值"),
        item("芝麻酱", "zhimajiang", "zmj", "酱", 623.0, 19.2, 52.7, 7.4, 22.7, 5.1, 5.0, 38.5, 342.0,
            source = "中国食物成分表：芝麻酱", confidence = "参考值"),
        item("腐乳", "furu", "fr", "酱", 134.0, 10.9, 8.2, 1.5, 7.8, 0.0, 2.0, 2460.0, 84.0,
            aliases = listOf("豆腐乳", "酱豆腐"), source = "中国食物成分表：腐乳（白）", confidence = "参考值"),
        item("鸡精", "jijing", "jj", "鲜味", 195.0, 11.0, 1.0, 0.2, 35.0, 0.0, 5.0, 20000.0, 100.0),
        item("味精", "weijing", "wj", "鲜味", 268.0, 40.0, 0.0, 0.0, 25.0, 0.0, 0.0, 12300.0, 4.0,
            source = "谷氨酸钠分子量折算：钠约 12.3%"),
        item("黑胡椒", "heihujiao", "hhj", "香辛料", 303.0, 11.6, 4.0, 1.5, 63.0, 25.0, 0.6, 20.0, 1329.0),
        item("白胡椒", "baihujiao", "bhj", "香辛料", 315.0, 10.4, 2.1, 0.9, 66.0, 26.0, 1.0, 5.0, 73.0),
        item("孜然粉", "ziranfen", "zrf", "香辛料", 375.0, 17.8, 22.3, 1.5, 44.2, 10.5, 2.3, 168.0, 1788.0,
            aliases = listOf("孜然")),
        item("五香粉", "wuxiangfen", "wxf", "香辛料", 348.0, 9.1, 8.5, 2.0, 63.0, 20.0, 2.0, 80.0, 1100.0),
        item("咖喱粉", "galifen", "glf", "香辛料", 338.0, 13.0, 12.2, 2.0, 63.3, 36.9, 3.0, 1700.0, 400.0,
            source = "中国食物成分表：咖喱粉；能量按宏量折算", confidence = "参考值"),
        item("辣椒粉", "lajiaofen", "ljf", "香辛料", 282.0, 13.5, 13.0, 2.0, 45.0, 27.0, 10.0, 30.0, 1900.0),
    )

    private val byId = items.associateBy { it.id }

    val foods: List<FoodItem> = items.mapIndexed { index, seasoning ->
        val n = seasoning.nutrientsPer100g
        FoodItem(
            id = 60_000 + index,
            name = seasoning.name,
            pinyin = seasoning.pinyin,
            initials = seasoning.initials,
            category = "调料",
            basis = "每100g",
            nutrients = n,
            aliases = seasoning.aliases,
            ingredients = emptyList(),
            method = "调料",
            source = seasoning.source,
            confidence = seasoning.confidence,
        )
    }

    fun require(id: String): SeasoningItem =
        requireNotNull(byId[id]) { "Unknown seasoning id: $id" }

    fun search(query: String): List<SeasoningItem> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (terms.isEmpty()) return items
        return items
            .mapNotNull { seasoning ->
                val scores = terms.map { term -> seasoningSearchScore(seasoning, term) }
                if (scores.any { it == null }) return@mapNotNull null
                seasoning to scores.filterNotNull().sum()
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun seasoningSearchScore(seasoning: SeasoningItem, term: String): Int? = when {
        seasoning.name.equals(term, ignoreCase = true) -> 0
        seasoning.aliases.any { it.equals(term, ignoreCase = true) } -> 10
        seasoning.name.startsWith(term, ignoreCase = true) -> 20
        seasoning.aliases.any { it.startsWith(term, ignoreCase = true) } -> 30
        seasoning.initials.equals(term, ignoreCase = true) -> 40
        seasoning.pinyin.startsWith(term, ignoreCase = true) -> 50
        seasoning.name.contains(term, ignoreCase = true) -> 60
        seasoning.aliases.any { it.contains(term, ignoreCase = true) } -> 70
        seasoning.pinyin.contains(term, ignoreCase = true) || seasoning.initials.contains(term, ignoreCase = true) -> 80
        seasoning.category.contains(term, ignoreCase = true) -> 90
        else -> null
    }
}
