package com.smartnutrient.app

object CatalogAudit {
    data class Issue(val name: String, val reason: String)

    private fun macroKcal(n: FoodNutrients) = n.proteinG * 4.0 + n.carbohydrateG * 4.0 + n.fatG * 9.0

    /** Alcohol energy is not included in the three macronutrients. */
    private val alcoholicDrinks = setOf("米酒", "啤酒", "果味啤酒")

    /** Salt and dried sea products legitimately exceed the generic sodium guard. */
    private fun sodiumCeiling(food: FoodItem): Double = when {
        food.name in setOf("盐", "食盐", "精盐") -> 40000.0
        food.name.contains("虾皮") || food.name.contains("海米") || food.name.contains("瑶柱") -> 15000.0
        else -> 5000.0
    }

    fun invalidServingBasis(foods: List<FoodItem>): List<String> = foods.mapNotNull { food ->
        when {
            food.basis == "每份" && food.aliases.none { it.startsWith("每份") && Regex("""[0-9]+(?:\\.[0-9]+)?""").containsMatchIn(it) } -> food.name
            food.basis == "干重" && food.category != "主食" -> food.name
            food.basis !in setOf("每100g", "每100ml", "可食部", "熟重", "每份", "干重", "生重") -> "${food.name}:${food.basis}"
            else -> null
        }
    }

    fun issues(foods: List<FoodItem>): List<Issue> = foods.mapNotNull { food ->
        val n = food.nutrients
        val minEnergyRatio = if (food.category == "水果") 0.60 else 0.75
        when {
            n.caloriesKcal !in 0.0..900.0 -> Issue(food.name, "energy=${n.caloriesKcal}")
            n.proteinG !in 0.0..60.0 -> Issue(food.name, "protein=${n.proteinG}")
            n.fatG !in 0.0..100.0 -> Issue(food.name, "fat=${n.fatG}")
            n.saturatedFatG !in 0.0..n.fatG -> Issue(food.name, "satFat=${n.saturatedFatG} fat=${n.fatG}")
            n.carbohydrateG !in 0.0..100.0 -> Issue(food.name, "carbs=${n.carbohydrateG}")
            n.fiberG !in 0.0..n.carbohydrateG + 1.0 -> Issue(food.name, "fiber=${n.fiberG} carbs=${n.carbohydrateG}")
            n.sugarG !in 0.0..n.carbohydrateG + 1.0 -> Issue(food.name, "sugar=${n.sugarG} carbs=${n.carbohydrateG}")
            n.sodiumMg !in 0.0..sodiumCeiling(food) -> Issue(food.name, "sodium=${n.sodiumMg}")
            n.potassiumMg !in 0.0..5000.0 -> Issue(food.name, "potassium=${n.potassiumMg}")
            n.cholesterolMg !in 0.0..3000.0 -> Issue(food.name, "cholesterol=${n.cholesterolMg}")
            n.caloriesKcal >= 30.0 && macroKcal(n) > 0.0 && !alcoholicDrinks.contains(food.name) &&
                (n.caloriesKcal / macroKcal(n)) !in minEnergyRatio..1.30 -> Issue(food.name, "energy=${n.caloriesKcal} macroEnergy=${macroKcal(n)}")
            else -> null
        }
    }

    fun report(foods: List<FoodItem>): String {
        val invalid = issues(foods)
        val highSodium = foods.filter { it.nutrients.sodiumMg >= 1000 }.sortedByDescending { it.nutrients.sodiumMg }
        val highEnergy = foods.filter { it.nutrients.caloriesKcal >= 350 }.sortedByDescending { it.nutrients.caloriesKcal }
        val suspiciousFat = foods.filter { it.nutrients.fatG >= 35 }.sortedByDescending { it.nutrients.fatG }
        return buildString {
            appendLine("invalid=${invalid.size}")
            appendLine("highSodium=${highSodium.size}")
            appendLine("highEnergy=${highEnergy.size}")
            appendLine("suspiciousFat=${suspiciousFat.size}")
            appendLine("invalidSample=" + invalid.take(20))
            appendLine("highSodiumSample=" + highSodium.take(20).map { "${it.name}:${it.nutrients.sodiumMg}" })
            appendLine("highEnergySample=" + highEnergy.take(20).map { "${it.name}:${it.nutrients.caloriesKcal}" })
            appendLine("suspiciousFatSample=" + suspiciousFat.take(20).map { "${it.name}:${it.nutrients.fatG}" })
        }
    }
}
