package com.smartnutrient.app

import org.junit.Test

class RecommendationReportTest {
    @Test
    fun printRepresentativeRecommendations() {
        val targets = NutritionEngine.displayTargets(
            EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30,
        )
        fun food(name: String) = EntryFoodCatalog.items.first { it.name == name }
        fun log(name: String, meal: Meal, grams: Double = 100.0) = FoodLog(name.hashCode().toLong(), food(name), grams, meal, OilLevel.ZERO, SaltLevel.ZERO)
        val scenarios = mapOf(
            "balanced" to listOf(log("米饭", Meal.BREAKFAST), log("番茄炒蛋", Meal.LUNCH)),
            "lowProtein" to listOf(log("米饭", Meal.BREAKFAST), log("生菜", Meal.LUNCH)),
            "highBurden" to listOf(log("鸡胸肉", Meal.BREAKFAST, 800.0), log("三文鱼", Meal.LUNCH, 800.0)),
        )
        scenarios.forEach { (name, logs) ->
            val result = RecommendationEngine.recommend(logs, targets, EntryFoodCatalog.items)
            println("RECOMMENDATION_$name=" + result.joinToString(" | ") { "${it.food.name}:${it.fitScore}:${it.reason}" })
        }
    }
}
