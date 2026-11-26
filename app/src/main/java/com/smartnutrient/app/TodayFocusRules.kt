package com.smartnutrient.app

data class NutritionFocusItem(
    val label: String,
    val detail: String,
    val alert: Boolean,
)

object TodayFocusRules {
    fun items(total: NutritionSnapshot, targets: NutritionTargets, limit: Int = 3): List<NutritionFocusItem> {
        if (limit <= 0) return emptyList()
        val candidates = buildList {
            if (total.sodiumMg > targets.sodium.max) add(NutritionFocusItem("钠偏高", "${total.sodiumMg.toInt()} mg", true))
            if (total.saturatedFatG > targets.saturatedFat.max) add(NutritionFocusItem("饱和脂肪偏高", "%.1f g".format(total.saturatedFatG), true))
            total.addedSugarG?.let { addedSugar ->
                if (addedSugar > targets.sugar.max) {
                    add(NutritionFocusItem("添加糖偏高", "%.1f g".format(addedSugar), true))
                }
            }
            if (total.cholesterolMg > targets.cholesterol.max) add(NutritionFocusItem("胆固醇偏高", "${total.cholesterolMg.toInt()} mg", true))
            if (total.proteinG < targets.protein.min) add(NutritionFocusItem("蛋白质待补", "还差 %.1f g".format(targets.protein.min - total.proteinG), false))
            if (total.fiberG < targets.fiber.min) add(NutritionFocusItem("膳食纤维待补", "还差 %.1f g".format(targets.fiber.min - total.fiberG), false))
            if (total.potassiumMg < targets.potassium.min) add(NutritionFocusItem("钾待补", "还差 ${(targets.potassium.min - total.potassiumMg).toInt()} mg", false))
            if (total.caloriesKcal > targets.calories.max) add(NutritionFocusItem("热量偏高", "${total.caloriesKcal.toInt()} kcal", true))
            if (total.caloriesKcal < targets.calories.min) add(NutritionFocusItem("能量待补", "还差 ${(targets.calories.min - total.caloriesKcal).toInt()} kcal", false))
        }
        return candidates.take(limit)
    }
}
