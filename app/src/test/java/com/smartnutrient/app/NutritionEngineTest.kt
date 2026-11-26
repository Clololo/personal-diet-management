package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionEngineTest {
    @Test
    fun calculatesNutrientsForWeightAndSource() {
        val rice = FoodNutrients(130.0, 2.6, 0.3, 0.1, 28.0, 0.4, 0.1, 1.0, 30.0, 0.0)
        val result = NutritionEngine.calculate(rice, 200.0)

        assertEquals(260.0, result.caloriesKcal, 0.01)
        assertEquals(5.2, result.proteinG, 0.01)
        assertEquals(56.0, result.carbohydrateG, 0.01)
    }

    @Test
    fun addsOilAndSaltToTheSharedServing() {
        val dish = FoodNutrients(100.0, 5.0, 5.0, 1.0, 10.0, 1.0, 1.0, 100.0, 200.0, 5.0)
        val base = NutritionEngine.calculate(dish, 500.0)
        val adjusted = NutritionEngine.applySeasoning(base, ServingSeasoning.Medium)

        assertEquals(500.0, base.caloriesKcal, 0.01)
        assertEquals(572.0, adjusted.caloriesKcal, 0.01)
        assertEquals(1100.0, adjusted.sodiumMg, 0.01)
    }

    @Test
    fun sumsMultipleFoodsIntoDailyTotals() {
        val rice = NutritionEngine.calculate(FoodNutrients(116.0, 2.6, 0.3, 0.1, 25.9, 0.3, 0.0, 1.0, 30.0, 0.0), 200.0)
        val egg = NutritionEngine.calculate(FoodNutrients(144.0, 13.3, 8.8, 2.6, 2.8, 0.0, 1.2, 131.0, 154.0, 0.0), 50.0)

        val total = DailyNutrition.total(listOf(rice, egg))

        assertEquals(304.0, total.caloriesKcal, 0.01)
        assertEquals(11.85, total.proteinG, 0.01)
        assertEquals(67.5, total.sodiumMg, 0.1)
    }

    @Test
    fun estimatesAdultEnergyFromBodyMeasurements() {
        val profile = BodyProfile(age = 30, sex = Sex.MALE, heightCm = 175.0, weightKg = 70.0, activity = ActivityLevel.MODERATE, goal = WeightGoal.MAINTAIN)
        val breakdown = NutritionEngine.energyBreakdown(profile, DailyActivityPlan(manualExerciseKcal = 0.0, steps = 0))
        assertEquals(1648.75, breakdown.restingKcal, 0.01)
        assertEquals(0.0, breakdown.activityKcal, 0.01)
        assertEquals(1648.75, breakdown.target.centerKcal, 0.01)
    }

    @Test
    fun dailyDisplayTargetsContainAllTenNutrientsInDisplayOrder() {
        val targets = NutritionEngine.displayTargets(EnergyTarget(2400.0, 2160.0, 2640.0), 2400.0, Sex.MALE, 30)
        assertEquals(listOf("热量", "蛋白质", "脂肪", "饱和脂肪", "碳水化合物", "膳食纤维", "添加糖", "钠", "钾", "胆固醇"), targets.all.map { it.label })
        assertEquals(10, targets.all.size)
    }

    fun customActivityCaloriesAddToCompleteRestingEnergy() {
        val profile = BodyProfile(age = 30, sex = Sex.MALE, heightCm = 175.0, weightKg = 70.0)
        val breakdown = NutritionEngine.energyBreakdown(profile, DailyActivityPlan(manualExerciseKcal = 700.0, steps = 0))
        assertEquals(700.0, breakdown.activityKcal, 0.01)
        assertEquals(2348.75, breakdown.target.centerKcal, 0.01)
    }
}


