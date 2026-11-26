package com.smartnutrient.app

import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientCoverageTest {
    @Test
    fun everyIngredientNamedByAnyDishHasAnIndependentEntry() {
        val standaloneCategories = setOf("食材", "调料", "基础食物", "水果", "主食")
        val dishIngredients = buildDishes()
            .flatMap { it.ingredients }
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

        val missing = dishIngredients.filter { ingredient ->
            FoodSearch.search(EntryFoodCatalog.items, ingredient).none { food ->
                food.category in standaloneCategories &&
                    (food.name == ingredient || ingredient in food.aliases)
            }
        }

        assertTrue("dish ingredients without independent food entries (${missing.size}): $missing", missing.isEmpty())
    }

    @Test
    fun distinctNamesDoNotSilentlyDuplicateAllTenNutrients() {
        val standalone = EntryFoodCatalog.items.filter { it.category == "食材" }
        val duplicates = standalone.groupBy { food ->
            val n = food.nutrients
            listOf(
                n.caloriesKcal, n.proteinG, n.fatG, n.saturatedFatG, n.carbohydrateG,
                n.fiberG, n.sugarG, n.sodiumMg, n.potassiumMg, n.cholesterolMg,
            )
        }.values.filter { group -> group.size > 1 }

        assertTrue("食材条目完全共享十项营养值: ${duplicates.map { group -> group.map { it.name } }}", duplicates.isEmpty())
    }
}