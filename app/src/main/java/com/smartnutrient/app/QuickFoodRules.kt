package com.smartnutrient.app

import java.time.LocalTime

data class QuickFoodSuggestion(
    val food: FoodItem,
    val label: String,
)

object QuickFoodRules {
    fun suggestedMeal(time: LocalTime): Meal = when {
        time.hour < 10 -> Meal.BREAKFAST
        time.hour < 15 -> Meal.LUNCH
        time.hour < 21 -> Meal.DINNER
        else -> Meal.SNACK
    }

    fun suggestions(
        foods: List<FoodItem>,
        recentIds: List<Int>,
        allEntries: List<FoodLog>,
        favorites: Set<Int>,
        limit: Int = 6,
    ): List<QuickFoodSuggestion> {
        if (limit <= 0) return emptyList()
        val byId = foods.distinctBy { it.id }.associateBy { it.id }
        val historyCounts = allEntries.groupingBy { it.food.id }.eachCount()
        val result = linkedMapOf<Int, QuickFoodSuggestion>()

        recentIds.forEach { id ->
            byId[id]?.let { result.putIfAbsent(id, QuickFoodSuggestion(it, "最近")) }
        }
        foods.filter { it.id in favorites }
            .sortedBy { it.name }
            .forEach { result.putIfAbsent(it.id, QuickFoodSuggestion(it, "收藏")) }
        historyCounts.entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            .forEach { (id, count) ->
                byId[id]?.let {
                    result.putIfAbsent(id, QuickFoodSuggestion(it, "常吃 ${count} 次"))
                }
            }

        return result.values.take(limit)
    }
}

object ServingPresetRules {
    fun forFood(food: FoodItem): List<Double> = when {
        FoodKind.of(food) == FoodKind.SEASONING -> listOf(1.0, 5.0, 10.0, 15.0)
        FoodKind.of(food) == FoodKind.SUPPLEMENT -> listOf(3.0, 5.0, 10.0, 15.0, 20.0, 30.0)
        FoodKind.of(food) == FoodKind.DRINK -> listOf(100.0, 200.0, 250.0, 330.0, 500.0)
        FoodKind.of(food) == FoodKind.FRUIT -> listOf(100.0, 150.0, 200.0)
        food.basis == "每份" -> listOf(100.0, 150.0, 200.0)
        food.basis == "干重" -> listOf(20.0, 30.0, 50.0)
        else -> listOf(100.0, 150.0, 200.0)
    }
}
