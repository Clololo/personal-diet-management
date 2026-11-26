package com.smartnutrient.app

/**
 * Total sugar and added sugar are different dietary measures:
 * - fruit and plain milk contain naturally occurring sugar, but no added sugar;
 * - sugar seasonings are entirely added sugar;
 * - processed foods and mixed dishes stay unknown unless their formula supplies
 *   a trustworthy added-sugar value.
 *
 * Returning null is intentional. Missing data must never be treated as zero.
 */
object SugarDataPolicy {
    private val zeroAddedSugarNames = setOf(
        "全脂牛奶",
        "低脂牛奶",
        "脱脂牛奶",
        "牛奶",
        "鸡蛋",
        "鸡蛋清",
        "鸡蛋黄",
        "鸡蛋液",
        "无糖豆浆",
        "无糖酸奶",
        "水",
        "茶",
        "绿茶",
        "乌龙茶",
        "咖啡",
        "黑咖啡",
        "冰",
    )

    private val addedSugarAsTotalNames = setOf(
        "糖",
        "白糖",
        "白砂糖",
        "冰糖",
        "红糖",
        "蜂蜜",
    )

    fun addedSugarG(category: String, name: String, totalSugarG: Double): Double? = when {
        category == "水果" -> 0.0
        (category == "饮品" && name.endsWith("牛奶")) -> 0.0
        name in zeroAddedSugarNames -> 0.0
        name in addedSugarAsTotalNames || category == "糖" -> totalSugarG
        else -> null
    }

    fun sumKnown(values: List<Double?>): Double? =
        if (values.isNotEmpty() && values.all { it != null }) values.sumOf { it ?: 0.0 } else null
}
