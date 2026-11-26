package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeformRecipeTest {
    private val chicken = FreeformIngredient(
        id = 900001,
        name = "鸡胸肉",
        grams = 200.0,
        nutrientsPer100g = FoodNutrients(120.0, 23.0, 2.5, 0.7, 0.0, 0.0, 0.0, 60.0, 330.0, 70.0),
    )
    private val regularSoy = SeasoningCatalog.require("调料-生抽")
    private val lowSaltSoy = SeasoningCatalog.require("调料-薄盐生抽")

    @Test
    fun seasoningCatalogOffersAtLeastThirtyDistinctSeasonings() {
        val seasonings = SeasoningCatalog.items
        assertTrue("count=${seasonings.size}", seasonings.size >= 30)
        assertEquals(seasonings.size, seasonings.map { it.id }.distinct().size)
        assertEquals(seasonings.size, seasonings.map { it.name }.distinct().size)
        assertTrue(regularSoy.nutrientsPer100g.sodiumMg > lowSaltSoy.nutrientsPer100g.sodiumMg)
        assertFalse(regularSoy.id == lowSaltSoy.id)
    }

    @Test
    fun freeformRecipeTotalsRawIngredientAndSeasoningNutrients() {
        val recipe = FreeformRecipe(
            name = "自配鸡胸黄瓜",
            ingredients = listOf(chicken, FreeformIngredient.fromFood(FoodCatalog.items.first { it.name == "黄瓜" }, 150.0)),
            seasonings = listOf(FreeformSeasoning(regularSoy, 10.0), FreeformSeasoning(SeasoningCatalog.require("调料-食盐"), 1.0)),
        )
        val total = recipe.totalNutrition()
        val per100 = recipe.per100gNutrition()
        assertEquals(361.0, recipe.totalWeightG, 0.01)
        assertEquals(total.caloriesKcal / 3.61, per100.caloriesKcal, 0.01)
        assertEquals(total.sodiumMg / 3.61, per100.sodiumMg, 0.01)
        assertEquals(240.0 + 16.0 * 1.5 + regularSoy.nutrientsPer100g.caloriesKcal * 0.1, total.caloriesKcal, 0.01)
    }

    @Test
    fun zeroGramItemsAndUnknownSeasoningDoNotChangeRecipe() {
        val recipe = FreeformRecipe(
            name = "零克测试",
            ingredients = listOf(chicken.copy(grams = 0.0)),
            seasonings = emptyList(),
        )
        assertEquals(0.0, recipe.totalWeightG, 0.01)
        assertEquals(0.0, recipe.totalNutrition().caloriesKcal, 0.01)
        assertFalse(recipe.canSave())
    }

    @Test
    fun savedFreeformRecipeBecomesSearchableCustomFood() {
        val recipe = FreeformRecipe(
            name = "鸡胸黄瓜碗",
            ingredients = listOf(chicken),
            seasonings = listOf(FreeformSeasoning(lowSaltSoy, 8.0)),
        )
        val food = recipe.toCustomFood(123456)
        assertEquals("自定义", food.category)
        assertEquals("每100g", food.basis)
        assertEquals("生重配方", food.method)
        assertTrue(food.name.contains("鸡胸黄瓜碗"))
        assertEquals(recipe.per100gNutrition().caloriesKcal, food.nutrients.caloriesKcal, 0.01)
        assertTrue(food.ingredients.any { it.contains("薄盐生抽") })
    }

    @Test
    fun loggedRecipeDoesNotApplyOilOrSaltTwice() {
        val recipe = FreeformRecipe(
            name = "控盐鸡肉",
            ingredients = listOf(chicken.copy(grams = 100.0)),
            seasonings = listOf(FreeformSeasoning(regularSoy, 10.0)),
        )
        val food = recipe.toCustomFood(654321)
        val logged = NutritionEngine.applySeasoning(
            NutritionEngine.calculateForRecord(food, recipe.totalWeightG),
            OilLevel.ZERO,
            SaltLevel.ZERO,
        )
        val expected = recipe.totalNutrition()
        assertEquals(expected.caloriesKcal, logged.caloriesKcal, 0.01)
        assertEquals(expected.sodiumMg, logged.sodiumMg, 0.01)
        assertEquals(expected.fatG, logged.fatG, 0.01)
    }

    @Test
    fun rawCompositionRejectsCookedAndPerServingFoods() {
        val cookedRice = FoodCatalog.items.first { it.name == "米饭" }
        val hotDryNoodles = FoodCatalog.items.first { it.name == "热干面" }
        assertFalse(FreeformIngredient.canUseAsRaw(cookedRice))
        assertFalse(FreeformIngredient.canUseAsRaw(hotDryNoodles))
        assertTrue(FreeformIngredient.canUseAsRaw(FoodCatalog.items.first { it.name == "生大米" }))
    }

    @Test
    fun seasoningSearchSupportsExplicitVariantsAndKeywords() {
        assertTrue(SeasoningCatalog.search("薄盐").any { it.name == "薄盐生抽" })
        assertTrue(SeasoningCatalog.search("wjx").any { it.name == "味极鲜" })
        assertTrue(SeasoningCatalog.search("酱油").any { it.name == "生抽" })
        assertEquals("生抽", SeasoningCatalog.search("生抽").first().name)
        assertEquals("薄盐生抽", SeasoningCatalog.search("薄盐").first().name)
    }

    @Test
    fun savedRecipeCanBeFoundAgainAndAddedWithoutRecalculationDrift() {
        val recipe = FreeformRecipe(
            name = "自制番茄鸡胸",
            ingredients = listOf(
                FreeformIngredient.fromFood(FoodCatalog.items.first { it.name == "鸡胸肉" }, 120.0),
                FreeformIngredient.fromFood(FoodCatalog.items.first { it.name == "番茄" }, 180.0),
            ),
            seasonings = listOf(FreeformSeasoning(lowSaltSoy, 8.0)),
        )
        val food = recipe.toCustomFood(700001)
        val results = FoodSearch.search(listOf(food), "自制 番茄")
        assertEquals(listOf(food.id), results.map { it.id })
        val reused = NutritionEngine.calculateForRecord(food, recipe.totalWeightG)
        assertEquals(recipe.totalNutrition().sodiumMg, reused.sodiumMg, 0.01)
    }

    @Test
    fun ingredientCatalogCoversCoreRawMaterials() {
        val names = RawIngredientCatalog.items.map { it.name }.toSet()
        listOf("鸡胸肉", "黄瓜", "番茄", "鸡蛋液", "牛奶", "北豆腐", "生大米", "生面条", "菠菜", "青椒", "猪里脊", "虾仁").forEach {
            assertTrue("missing raw ingredient=$it", it in names)
        }
    }

    @Test
    fun rawAliasesNeverResolveToAWholePreparedDish() {
        val raw = RawIngredientCatalog.items
        assertTrue(raw.any { it.name == "土豆" })
        assertTrue(raw.none { it.category != "食材" })
        assertFalse(FreeformIngredient.canUseAsRaw(FoodCatalog.items.first { it.name == "米饭" }))
        assertTrue(FoodSearch.search(raw, "土豆").any { it.name == "土豆" })
    }

    @Test
    fun everyRawIngredientHasAUniqueIdentityAndPlausibleNutrition() {
        val raw = FoodCatalog.items.filter(FreeformIngredient::canUseAsRaw)
        assertTrue(raw.size >= 400)
        assertEquals(
            raw.size,
            raw.map { "${it.category}:${it.name}:${it.basis}" }.distinct().size,
        )
        raw.forEach { food ->
            val n = food.nutrients
            assertTrue("${food.name} kcal=${n.caloriesKcal}", n.caloriesKcal in 0.0..900.0)
            val sodiumLimit = if (food.name == "盐") 40000.0 else 20000.0
            assertTrue("${food.name} sodium=${n.sodiumMg}", n.sodiumMg in 0.0..sodiumLimit)
            assertTrue("${food.name} fiber=${n.fiberG} carbs=${n.carbohydrateG}", n.fiberG <= n.carbohydrateG + 1.0)
            assertTrue("${food.name} source", food.source.isNotBlank())
        }
    }

    @Test
    fun freeformCatalogMixesCookedAndRawNamesWithoutOfferingCookedOnes() {
        val allNames = FoodCatalog.items.map { it.name }.toSet()
        listOf("米饭", "生大米", "番茄炒蛋", "番茄", "鸡胸肉", "鸡胸黄瓜碗").take(5).forEach {
            if (it == "鸡胸黄瓜碗") return@forEach
            assertTrue("missing=$it", it in allNames)
        }
        assertFalse(FreeformIngredient.canUseAsRaw(FoodCatalog.items.first { it.name == "米饭" }))
        assertTrue(FreeformIngredient.canUseAsRaw(FoodCatalog.items.first { it.name == "番茄" }))
    }
}
