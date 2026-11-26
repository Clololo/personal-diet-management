package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PortionScalingRegressionTest {
    private val food = FoodItem(
        id = 1,
        name = "测试食材",
        pinyin = "",
        initials = "",
        category = "食材",
        basis = "每100g",
        nutrients = FoodNutrients(
            caloriesKcal = 200.0,
            proteinG = 20.0,
            fatG = 10.0,
            saturatedFatG = 4.0,
            carbohydrateG = 30.0,
            fiberG = 8.0,
            sugarG = 6.0,
            sodiumMg = 800.0,
            potassiumMg = 400.0,
            cholesterolMg = 40.0,
            addedSugarG = 2.0,
        ),
    )

    @Test
    fun changingPortionFromHundredToTwentyFiveGramsScalesSaltAndEveryNutrient() {
        val hundredGrams = NutritionEngine.recordSnapshot(food, 100.0, OilLevel.ZERO, SaltLevel.MEDIUM)
        val twentyFiveGrams = NutritionEngine.recordSnapshot(food, 25.0, OilLevel.ZERO, SaltLevel.MEDIUM)

        assertEquals(hundredGrams.caloriesKcal * 0.25, twentyFiveGrams.caloriesKcal, 0.01)
        assertEquals(hundredGrams.proteinG * 0.25, twentyFiveGrams.proteinG, 0.01)
        assertEquals(hundredGrams.fatG * 0.25, twentyFiveGrams.fatG, 0.01)
        assertEquals(hundredGrams.saturatedFatG * 0.25, twentyFiveGrams.saturatedFatG, 0.01)
        assertEquals(hundredGrams.carbohydrateG * 0.25, twentyFiveGrams.carbohydrateG, 0.01)
        assertEquals(hundredGrams.fiberG * 0.25, twentyFiveGrams.fiberG, 0.01)
        assertEquals(hundredGrams.sugarG * 0.25, twentyFiveGrams.sugarG, 0.01)
        assertEquals(hundredGrams.sodiumMg * 0.25, twentyFiveGrams.sodiumMg, 0.01)
        assertEquals(hundredGrams.potassiumMg * 0.25, twentyFiveGrams.potassiumMg, 0.01)
        assertEquals(hundredGrams.cholesterolMg * 0.25, twentyFiveGrams.cholesterolMg, 0.01)
        assertEquals(hundredGrams.addedSugarG!! * 0.25, twentyFiveGrams.addedSugarG!!, 0.01)
    }

    @Test
    fun changingPortionScalesOilAndSaltTogether() {
        val hundredGrams = NutritionEngine.recordSnapshot(food, 100.0, OilLevel.MEDIUM, SaltLevel.HIGH)
        val twentyFiveGrams = NutritionEngine.recordSnapshot(food, 25.0, OilLevel.MEDIUM, SaltLevel.HIGH)

        assertEquals(272.0, hundredGrams.caloriesKcal, 0.01)
        assertEquals(68.0, twentyFiveGrams.caloriesKcal, 0.01)
        assertEquals(2000.0, hundredGrams.sodiumMg, 0.01)
        assertEquals(500.0, twentyFiveGrams.sodiumMg, 0.01)
    }

    @Test
    fun perServingFoodScalesItsSeasoningByDeclaredServingWeight() {
        val servingFood = food.copy(
            id = 3,
            name = "整份测试面",
            basis = "每份",
            aliases = listOf("每份200g"),
        )

        val fullServing = NutritionEngine.recordSnapshot(servingFood, 200.0, OilLevel.ZERO, SaltLevel.LOW)
        val quarterServing = NutritionEngine.recordSnapshot(servingFood, 50.0, OilLevel.ZERO, SaltLevel.LOW)

        assertEquals(fullServing.caloriesKcal * 0.25, quarterServing.caloriesKcal, 0.01)
        assertEquals(fullServing.sodiumMg * 0.25, quarterServing.sodiumMg, 0.01)
    }

    @Test
    fun changingPortionScalesTheRenderedSodiumValue() {
        val hundredGrams = NutritionEngine.recordSnapshot(food, 100.0, OilLevel.ZERO, SaltLevel.MEDIUM)
        val twentyFiveGrams = NutritionEngine.recordSnapshot(food, 25.0, OilLevel.ZERO, SaltLevel.MEDIUM)
        val hundredSodium = NutritionValues.rows(hundredGrams).first { it.label == "钠" }.value
        val twentyFiveSodium = NutritionValues.rows(twentyFiveGrams).first { it.label == "钠" }.value

        assertEquals("1400 mg", hundredSodium)
        assertEquals("350 mg", twentyFiveSodium)
    }

    @Test
    fun smallPortionsKeepMeaningfulDecimalsInsteadOfLookingUnchangedWhenRounded() {
        val tinyFood = food.copy(
            id = 4,
            nutrients = food.nutrients.copy(sodiumMg = 150.0),
        )

        val hundredGrams = NutritionEngine.recordSnapshot(tinyFood, 100.0, OilLevel.ZERO, SaltLevel.ZERO)
        val twentyFiveGrams = NutritionEngine.recordSnapshot(tinyFood, 25.0, OilLevel.ZERO, SaltLevel.ZERO)

        assertEquals("150 mg", NutritionValues.rows(hundredGrams).first { it.label == "钠" }.value)
        assertEquals("37.5 mg", NutritionValues.rows(twentyFiveGrams).first { it.label == "钠" }.value)
    }
}
