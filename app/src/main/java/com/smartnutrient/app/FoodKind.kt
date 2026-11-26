package com.smartnutrient.app

/** User-facing food type, independent of internal catalogue category names. */
enum class FoodKind(val label: String) {
    DISH("成品菜"),
    INGREDIENT("食材"),
    SEASONING("调料"),
    FRUIT("水果"),
    DRINK("饮品"),
    SUPPLEMENT("补剂");

    companion object {
        fun of(food: FoodItem): FoodKind = when {
            food.category == "补剂" -> SUPPLEMENT
            food.category == "饮品" -> DRINK
            food.category == "水果" -> FRUIT
            food.category == "调料" -> SEASONING
            food.category == "食材" -> INGREDIENT
            food.method in DRINK_METHODS || food.name in DRINK_NAMES ||
                DRINK_NAME_KEYWORDS.any { food.name.contains(it) } -> DRINK
            food.method == "调料" -> SEASONING
            food.method in RAW_METHODS -> INGREDIENT
            else -> DISH
        }

        private val DRINK_METHODS = setOf("饮用", "冲调", "饮料")
        private val RAW_METHODS = setOf("生食材", "干食材", "熟食材")
        private val DRINK_NAMES = setOf("水", "茶", "绿茶", "乌龙茶", "咖啡", "可乐", "啤酒", "米酒", "酒酿", "豆浆", "牛奶", "酸奶")
        private val DRINK_NAME_KEYWORDS = listOf("牛奶", "豆浆", "酸奶", "咖啡", "茶", "可乐", "啤酒", "米酒", "果汁", "奶昔", "饮品")
    }
}
