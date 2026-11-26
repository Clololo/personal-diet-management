package com.smartnutrient.app

data class FoodItem(
    val id: Int,
    val name: String,
    val pinyin: String,
    val initials: String,
    val category: String,
    val basis: String,
    val nutrients: FoodNutrients,
    val aliases: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val method: String = "",
    val source: String = "标准配方估算",
    val confidence: String = "估算",
    val dataQuality: FoodDataQuality = when (confidence) {
        "参考值" -> FoodDataQuality.GENERIC_REFERENCE
        "品牌标签" -> FoodDataQuality.BRAND_LABEL
        else -> FoodDataQuality.RECIPE_ESTIMATE
    },
    val includesSeasoningPack: Boolean? = null,
)

private data class SearchHit(val score: Int, val food: FoodItem)

object FoodSearch {
    private val seasoningTerms = setOf("低盐", "少盐", "低油", "少油", "中盐", "中油", "高盐", "高油")

    fun search(foods: List<FoodItem>, query: String, favorites: Set<Int> = emptySet(), recent: List<Int> = emptyList()): List<FoodItem> {
        val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val recentRank = recent.withIndex().associate { it.value to it.index }
        val hits = if (terms.isEmpty()) {
            foods.mapIndexed { index, food -> SearchHit(index, food) }
        } else {
            foods.asSequence()
            .mapNotNull { food ->
                val scores = terms.map { score(food, it) }
                if (scores.any { it == null }) return@mapNotNull null
                SearchHit(scores.filterNotNull().sum(), food)
            }
            .toList()
        }
        return hits
            .sortedWith(compareBy<SearchHit> { if (it.food.id in favorites) 0 else if (it.food.id in recentRank) 1 else 2 }
                .thenBy { recentRank[it.food.id] ?: Int.MAX_VALUE }
                .thenBy { it.score }
                .thenBy { it.food.id })
            .take(120)
            .map { it.food }
    }

    private fun score(food: FoodItem, rawTerm: String): Int? {
        val term = rawTerm
        // Seasoning is selected on the log entry, not represented by duplicated dishes.
        // A seasoning word therefore refines the user's intent without excluding the food.
        if (rawTerm in seasoningTerms) return 110
        return when {
            food.name.equals(term, ignoreCase = true) -> 0
            food.aliases.any { it.equals(term, ignoreCase = true) } -> 10
            food.ingredients.any { it.equals(term, ignoreCase = true) } -> 20
            food.name.startsWith(term, ignoreCase = true) -> 30
            food.aliases.any { it.startsWith(term, ignoreCase = true) } -> 40
            food.pinyin.startsWith(term) -> 50
            food.initials.equals(term) -> 45
            food.name.contains(term, ignoreCase = true) -> 70
            food.ingredients.any { it.contains(term, ignoreCase = true) } -> 80
            food.aliases.any { it.contains(term, ignoreCase = true) } -> 90
            food.pinyin.contains(term) || food.initials.contains(term) -> 100
            food.method.contains(term, ignoreCase = true) -> 120
            food.category.contains(term, ignoreCase = true) -> 130
            else -> null
        }
    }
}

