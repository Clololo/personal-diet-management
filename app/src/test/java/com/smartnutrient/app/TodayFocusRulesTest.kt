package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class TodayFocusRulesTest {
    private val targets = NutritionEngine.displayTargets(EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30)

    @Test
    fun prioritiesPutExceededLimitsBeforeMissingMinimums() {
        val total = NutritionSnapshot(500.0, 10.0, 60.0, 8.0, 50.0, 3.0, 2.0, 2600.0, 500.0, 20.0)
        val items = TodayFocusRules.items(total, targets)

        assertEquals("钠偏高", items.first().label)
        assertTrue(items.first().alert)
        assertTrue(items.any { it.label == "蛋白质待补" })
        assertFalse(items.first().label == "蛋白质待补")
    }

    @Test
    fun focusListIsBoundedAndStable() {
        val total = NutritionSnapshot(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val items = TodayFocusRules.items(total, targets, limit = 2)
        assertEquals(2, items.size)
        assertEquals(listOf("蛋白质待补", "膳食纤维待补"), items.map { it.label })
    }

    @Test
    fun quickAddMealFollowsTimeOfDay() {
        assertEquals(Meal.BREAKFAST, QuickFoodRules.suggestedMeal(LocalTime.of(8, 0)))
        assertEquals(Meal.LUNCH, QuickFoodRules.suggestedMeal(LocalTime.of(12, 30)))
        assertEquals(Meal.DINNER, QuickFoodRules.suggestedMeal(LocalTime.of(19, 0)))
        assertEquals(Meal.SNACK, QuickFoodRules.suggestedMeal(LocalTime.of(22, 0)))
    }
}
