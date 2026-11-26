package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private val targets = NutritionEngine.displayTargets(
        EnergyTarget(centerKcal = 2000.0, minKcal = 1800.0, maxKcal = 2200.0),
        baselineCalories = 2000.0,
        sex = Sex.FEMALE,
        age = 30,
    )

    @Test
    fun recommendationRequiresBreakfastAndLunchEntries() {
        assertFalse(RecommendationEngine.canCalculate(emptyList()))
        assertFalse(RecommendationEngine.canCalculate(listOf(log("鸡蛋", Meal.BREAKFAST))))
        assertTrue(RecommendationEngine.canCalculate(listOf(log("鸡蛋", Meal.BREAKFAST), log("鸡胸肉", Meal.LUNCH))))
    }

    @Test
    fun recommendationsComeOnlyFromIngredientsAndFruit() {
        val result = RecommendationEngine.recommend(
            logs = listOf(
                log("鸡蛋", Meal.BREAKFAST),
                log("鸡胸肉", Meal.LUNCH),
                log("番茄炒蛋", Meal.DINNER),
            ),
            targets = targets,
            catalog = EntryFoodCatalog.items,
        )

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { FoodKind.of(it.food) in setOf(FoodKind.INGREDIENT, FoodKind.FRUIT) })
    }

    @Test
    fun nutrientGapsInfluenceRanking() {
        val lowProtein = RecommendationEngine.recommend(
            logs = listOf(
                log("米饭", Meal.BREAKFAST),
                log("生菜", Meal.LUNCH),
            ),
            targets = targets,
            catalog = EntryFoodCatalog.items,
        )
        val highProtein = RecommendationEngine.recommend(
            logs = listOf(
                log("鸡胸肉", Meal.BREAKFAST),
                log("三文鱼", Meal.LUNCH),
            ),
            targets = targets,
            catalog = EntryFoodCatalog.items,
        )

        assertTrue(lowProtein.first().food.nutrients.proteinG >= highProtein.first().food.nutrients.proteinG)
    }

    @Test
    fun recommendationsAvoidDuplicateNamesAndRemainDeterministic() {
        val first = RecommendationEngine.recommend(logs(), targets, EntryFoodCatalog.items)
        val second = RecommendationEngine.recommend(logs(), targets, EntryFoodCatalog.items)

        assertEquals(first.map { it.food.name }, second.map { it.food.name })
        assertEquals(first.size, first.map { it.food.name }.distinct().size)
    }

    @Test
    fun recommendationsOfferVarietyAcrossFoodGroupsWhenPossible() {
        val result = RecommendationEngine.recommend(logs(), targets, EntryFoodCatalog.items)

        assertTrue(
            result.joinToString { it.food.name },
            result.map { RecommendationEngine.foodGroup(it.food) }.distinct().size >= 2,
        )
    }
    @Test
    fun allTargetsExceededStillProducesLowBurdenFallback() {
        val result = RecommendationEngine.recommend(
            logs = listOf(
                log("鸡胸肉", Meal.BREAKFAST, grams = 800.0, oil = OilLevel.HIGH, salt = SaltLevel.HIGH),
                log("三文鱼", Meal.LUNCH, grams = 800.0, oil = OilLevel.HIGH, salt = SaltLevel.HIGH),
            ),
            targets = targets,
            catalog = EntryFoodCatalog.items,
        )

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.reason.isNotBlank() && it.suggestedGrams in 1.0..300.0 })
        assertTrue(result.first().food.nutrients.sodiumMg < 1_000.0)
    }

    @Test
    fun emptyOrTinyCatalogCannotCrashAndStillReturnsWhenEligibleFoodsExist() {
        assertTrue(RecommendationEngine.recommend(logs(), targets, emptyList()).isEmpty())
        val smallCatalog = listOf(requireNotNull(EntryFoodCatalog.items.firstOrNull { it.name == "黄瓜" }))
        val result = RecommendationEngine.recommend(logs(), targets, smallCatalog)
        assertEquals(1, result.size)
        assertEquals("黄瓜", result.first().food.name)
    }

    private fun logs() = listOf(
        log("米饭", Meal.BREAKFAST),
        log("番茄炒蛋", Meal.LUNCH),
    )

    private fun log(
        name: String,
        meal: Meal,
        grams: Double = 100.0,
        oil: OilLevel = OilLevel.ZERO,
        salt: SaltLevel = SaltLevel.ZERO,
    ): FoodLog = FoodLog(
        id = (name.hashCode().toLong() * 31 + meal.ordinal),
        food = EntryFoodCatalog.items.first { it.name == name },
        grams = grams,
        meal = meal,
        oil = oil,
        salt = salt,
    )
}
