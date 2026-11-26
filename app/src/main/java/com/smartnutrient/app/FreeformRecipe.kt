package com.smartnutrient.app

data class FreeformIngredient(
    val id: Int,
    val name: String,
    val grams: Double,
    val nutrientsPer100g: FoodNutrients,
    val source: String = "食材目录",
    val confidence: String = "估算",
) {
    init {
        require(grams >= 0.0) { "Ingredient weight cannot be negative" }
    }

    companion object {
        private val rawBases = setOf("每100g", "可食部", "生重", "干重")

        fun canUseAsRaw(food: FoodItem): Boolean = food.basis in rawBases

        fun fromFood(food: FoodItem, grams: Double): FreeformIngredient {
            require(canUseAsRaw(food)) {
                "${food.name} 的基数是 ${food.basis}，不能当作生重食材"
            }
            return FreeformIngredient(
                id = food.id,
                name = food.name,
                grams = grams,
                nutrientsPer100g = food.nutrients,
                source = food.source,
                confidence = food.confidence,
            )
        }
    }
}

data class FreeformSeasoning(
    val seasoning: SeasoningItem,
    val grams: Double,
) {
    init {
        require(grams >= 0.0) { "Seasoning weight cannot be negative" }
    }
}

data class SeasoningSpoon(val label: String, val grams: Double)

object SeasoningPortionRules {
    fun commonSpoons(seasoning: SeasoningItem): List<SeasoningSpoon> {
        val grams = when (seasoning.category) {
            "油" -> listOf(SeasoningSpoon("1 茶匙", 5.0), SeasoningSpoon("1 汤匙", 15.0))
            "酱油", "醋", "酒", "酱汁" -> listOf(SeasoningSpoon("1 茶匙", 5.0), SeasoningSpoon("1 汤匙", 15.0))
            "盐" -> listOf(SeasoningSpoon("1 平茶匙", 5.0))
            "糖" -> listOf(SeasoningSpoon("1 平茶匙", 4.0), SeasoningSpoon("1 汤匙", 12.0))
            "酱" -> listOf(SeasoningSpoon("1 茶匙", 5.0), SeasoningSpoon("1 汤匙", 15.0))
            "鲜味", "香辛料" -> listOf(SeasoningSpoon("1 平茶匙", 3.0))
            else -> listOf(SeasoningSpoon("1 茶匙", 5.0))
        }
        return listOf(SeasoningSpoon("1 g", 1.0)) + grams
    }
}

data class FreeformRecipe(
    val name: String,
    val ingredients: List<FreeformIngredient>,
    val seasonings: List<FreeformSeasoning>,
) {
    val totalWeightG: Double
        get() = ingredients.sumOf { it.grams } + seasonings.sumOf { it.grams }

    fun totalNutrition(): FoodNutrients {
        var total = zeroNutrients()
        ingredients.forEach { total = total + NutritionEngine.calculate(it.nutrientsPer100g, it.grams).asNutrients() }
        seasonings.forEach { total = total + NutritionEngine.calculate(it.seasoning.nutrientsPer100g, it.grams).asNutrients() }
        return total
    }

    fun per100gNutrition(): FoodNutrients = if (totalWeightG <= 0.0) zeroNutrients() else {
        totalNutrition().scaled(100.0 / totalWeightG)
    }

    fun canSave(): Boolean = name.isNotBlank() && ingredients.any { it.grams > 0.0 } && totalWeightG > 0.0

    fun toCustomFood(id: Int): FoodItem {
        require(canSave()) { "Recipe must have a name and at least one non-zero ingredient" }
        val ingredientLines = ingredients.filter { it.grams > 0.0 }.map { "${it.name} ${formatGrams(it.grams)} g" } +
            seasonings.filter { it.grams > 0.0 }.map { "${it.seasoning.name} ${formatGrams(it.grams)} g" }
        return FoodItem(
            id = id,
            name = name.trim(),
            pinyin = "",
            initials = "",
            category = "自定义",
            basis = "每100g",
            nutrients = per100gNutrition(),
            aliases = listOf("自由配方", "自制菜"),
            ingredients = ingredientLines,
            method = "生重配方",
            source = "用户自由配方",
            confidence = "配方估算",
        )
    }

    private fun formatGrams(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}

internal fun zeroNutrients(): FoodNutrients =
    FoodNutrients(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

private fun NutritionSnapshot.asNutrients() = FoodNutrients(
    caloriesKcal, proteinG, fatG, saturatedFatG, carbohydrateG,
    fiberG, sugarG, sodiumMg, potassiumMg, cholesterolMg, addedSugarG,
)

private operator fun FoodNutrients.plus(other: FoodNutrients) = FoodNutrients(
    caloriesKcal + other.caloriesKcal,
    proteinG + other.proteinG,
    fatG + other.fatG,
    saturatedFatG + other.saturatedFatG,
    carbohydrateG + other.carbohydrateG,
    fiberG + other.fiberG,
    sugarG + other.sugarG,
    sodiumMg + other.sodiumMg,
    potassiumMg + other.potassiumMg,
    cholesterolMg + other.cholesterolMg,
    SugarDataPolicy.sumKnown(listOf(addedSugarG, other.addedSugarG)),
)

private fun FoodNutrients.scaled(scale: Double) = FoodNutrients(
    caloriesKcal * scale,
    proteinG * scale,
    fatG * scale,
    saturatedFatG * scale,
    carbohydrateG * scale,
    fiberG * scale,
    sugarG * scale,
    sodiumMg * scale,
    potassiumMg * scale,
    cholesterolMg * scale,
    addedSugarG?.times(scale),
)
