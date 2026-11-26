package com.smartnutrient.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationAuditTest {
    @Test
    fun recommendationsDoNotUseDryPantryStaplesAsDefaultMealSuggestions() {
        val targets = NutritionEngine.displayTargets(
            EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30,
        )
        val logs = listOf(
            log("米饭", Meal.BREAKFAST),
            log("番茄炒蛋", Meal.LUNCH),
        )

        val result = RecommendationEngine.recommend(logs, targets, EntryFoodCatalog.items)

        assertTrue(result.joinToString { it.food.name }, result.none {
            it.food.name.contains("干") || it.food.name.contains("粉") || it.food.name == "琼脂"
        })
    }

    @Test
    fun highBurdenScenarioGivesExplicitLightChoiceReason() {
        val targets = NutritionEngine.displayTargets(
            EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30,
        )
        val logs = listOf(
            log("鸡胸肉", Meal.BREAKFAST, 800.0, OilLevel.HIGH, SaltLevel.HIGH),
            log("三文鱼", Meal.LUNCH, 800.0, OilLevel.HIGH, SaltLevel.HIGH),
        )

        val first = RecommendationEngine.recommend(logs, targets, EntryFoodCatalog.items).first()

        assertTrue(first.reason, first.reason.contains("上限") || first.reason.contains("清淡"))
        assertFalse(first.reason, first.reason.contains("优先补充"))
    }

    private fun log(name: String, meal: Meal, grams: Double = 100.0, oil: OilLevel = OilLevel.ZERO, salt: SaltLevel = SaltLevel.ZERO) =
        FoodLog(name.hashCode().toLong(), EntryFoodCatalog.items.first { it.name == name }, grams, meal, oil, salt)
}
