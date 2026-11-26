package com.smartnutrient.app

/**
 * A nutrition-aware suggestion for the next meal. Recommendations are intentionally
 * limited to ingredients and fruit; prepared dishes are never used as an ingredient.
 */
data class NutritionRecommendation(
    val food: FoodItem,
    val suggestedGrams: Double,
    val estimated: NutritionSnapshot,
    val fitScore: Double,
    val reason: String,
)

object RecommendationEngine {
    private val recommendableKinds = setOf(FoodKind.INGREDIENT, FoodKind.FRUIT)
    private val excludedRecommendations = setOf("乳清蛋白")

    fun canCalculate(logs: List<FoodLog>): Boolean =
        logs.any { it.meal == Meal.BREAKFAST } && logs.any { it.meal == Meal.LUNCH }

    /**
     * Ranks standalone ingredients and fruit against the remaining daily nutrition gap.
     *
     * The score deliberately combines two ideas:
     * - unused minimums (protein, fiber, potassium and carbohydrate) are useful gaps to fill;
     * - maximums (fat, saturated fat, sugar, sodium and cholesterol) are burdens, not goals.
     *
     * When several maximums are already exceeded, low-burden terms lead the ranking and
     * the list still has a deterministic fallback rather than becoming empty.
     */
    fun recommend(
        logs: List<FoodLog>,
        targets: NutritionTargets,
        catalog: List<FoodItem>,
        limit: Int = 3,
        excludedNames: Set<String> = emptySet(),
    ): List<NutritionRecommendation> {
        if (!canCalculate(logs) || limit <= 0) return emptyList()

        val eligible = catalog.asSequence()
            .filter { FoodKind.of(it) in recommendableKinds }
            .filter { it.name.isNotBlank() && it.nutrients.caloriesKcal >= 0.0 }
            .filter { isEverydayChoice(it) }
            .filter { it.name !in excludedRecommendations }
            .filter { it.name !in excludedNames }
            .distinctBy { it.name }
            .toList()
        if (eligible.isEmpty()) return emptyList()

        val current = currentNutrition(logs)
        val gapNames = preferredGaps(current, targets)
        val manyLimitsExceeded = highBurdenCount(current, targets) >= 3

        val ranked = eligible
            .map { food ->
                val grams = recommendedGrams(food)
                val estimated = NutritionEngine.calculateForRecord(food, grams)
                NutritionRecommendation(
                    food = food,
                    suggestedGrams = grams,
                    estimated = estimated,
                    fitScore = score(estimated, current, targets),
                    reason = reasonFor(food, grams, gapNames, manyLimitsExceeded),
                )
            }
            .sortedWith(
                compareByDescending<NutritionRecommendation> { it.fitScore }
                    .thenBy { it.food.id }
                    .thenBy { it.food.name },
            )

        return diversify(ranked, limit)
    }

    private fun currentNutrition(logs: List<FoodLog>): NutritionSnapshot =
        DailyNutrition.total(logs.map { log -> NutritionEngine.recordSnapshot(log.food, log.grams, log.oil, log.salt) })

    private fun score(
        estimate: NutritionSnapshot,
        current: NutritionSnapshot,
        targets: NutritionTargets,
    ): Double {
        var score = 0.0

        // Reward foods that make progress on minimums. Cap each contribution so one
        // unusually dense food cannot dominate every recommendation.
        score += gapReward(current.proteinG, targets.protein, estimate.proteinG / 20.0) * 5.0
        score += gapReward(current.fiberG, targets.fiber, estimate.fiberG / 5.0) * 4.0
        score += gapReward(current.potassiumMg, targets.potassium, estimate.potassiumMg / 400.0) * 3.0
        score += gapReward(current.carbohydrateG, targets.carbohydrate, estimate.carbohydrateG / 30.0) * 1.2
        score += gapReward(current.caloriesKcal, targets.calories, estimate.caloriesKcal / 250.0) * 1.0

        // Prefer low-burden foods for upper limits. The penalty becomes stronger after
        // the corresponding daily maximum is exceeded.
        score += burdenReward(estimate.fatG, current.fatG, targets.fat.max, 18.0) * 0.9
        score += burdenReward(estimate.saturatedFatG, current.saturatedFatG, targets.saturatedFat.max, 6.0) * 1.2
        val estimatedAddedSugar = estimate.addedSugarG
        val currentAddedSugar = current.addedSugarG
        if (estimatedAddedSugar != null && currentAddedSugar != null) {
            score += burdenReward(estimatedAddedSugar, currentAddedSugar, targets.sugar.max, 18.0) * 0.8
        }
        score += burdenReward(estimate.sodiumMg, current.sodiumMg, targets.sodium.max, 500.0) * 1.2
        score += burdenReward(estimate.cholesterolMg, current.cholesterolMg, targets.cholesterol.max, 100.0) * 0.8

        return score
    }

    private fun gapReward(current: Double, target: NutrientTarget, density: Double): Double {
        if (target.min <= 0.0 || current >= target.min) return 0.0
        val gap = ((target.min - current) / target.min).coerceIn(0.0, 1.0)
        return gap * density.coerceIn(0.0, 1.5)
    }

    private fun burdenReward(estimate: Double, current: Double, max: Double, reference: Double): Double {
        if (reference <= 0.0) return 0.0
        val load = (estimate / reference).coerceIn(0.0, 1.5)
        val exceeded = if (max > 0.0 && current >= max) 1.0 else 0.0
        return (1.0 - load) * (0.35 + exceeded * 0.65)
    }

    /**
     * Keep the strongest options while avoiding three near-identical food groups in a row.
     * If there are fewer groups than slots, the remaining best options fill the list.
     */
    private fun diversify(ranked: List<NutritionRecommendation>, limit: Int): List<NutritionRecommendation> {
        val selected = mutableListOf<NutritionRecommendation>()
        val groups = mutableSetOf<String>()
        ranked.forEach { candidate ->
            if (selected.size >= limit) return@forEach
            if (groups.add(foodGroup(candidate.food))) selected += candidate
        }
        if (selected.size < limit) {
            ranked.forEach { candidate ->
                if (selected.size >= limit) return@forEach
                if (candidate !in selected) selected += candidate
            }
        }
        return selected
    }

    internal fun foodGroup(food: FoodItem): String {
        val name = food.name
        return when {
            FoodKind.of(food) == FoodKind.FRUIT -> "水果"
            listOf("豆", "豌豆", "扁豆", "鹰嘴豆", "花生", "腐竹", "豆皮").any { name.contains(it) } -> "豆类"
            listOf("鸡", "牛", "猪", "羊", "鱼", "虾", "蟹", "蛋", "贝", "鱿").any { name.contains(it) } -> "蛋白"
            listOf("米", "麦", "玉米", "薯", "土豆", "山药", "芋", "莲藕", "荸荠", "燕麦").any { name.contains(it) } -> "谷薯"
            listOf("菜", "菠菜", "生菜", "西兰花", "黄瓜", "番茄", "胡萝卜", "洋葱", "青椒", "茄子", "西葫芦", "南瓜", "蘑菇", "香菇", "木耳").any { name.contains(it) } -> "蔬菜"
            else -> "其他食材"
        }
    }

    private fun recommendedGrams(food: FoodItem): Double = when {
        FoodKind.of(food) == FoodKind.FRUIT -> 150.0
        food.basis == "干重" -> 30.0
        else -> 100.0
    }

    private fun highBurdenCount(current: NutritionSnapshot, targets: NutritionTargets): Int = listOf(
        current.caloriesKcal > targets.calories.max,
        current.fatG > targets.fat.max,
        current.saturatedFatG > targets.saturatedFat.max,
        current.addedSugarG?.let { it > targets.sugar.max } == true,
        current.sodiumMg > targets.sodium.max,
        current.cholesterolMg > targets.cholesterol.max,
    ).count { it }

    private fun isEverydayChoice(food: FoodItem): Boolean {
        val dryMarkers = listOf("干", "粉", "琼脂", "脱水", "腌", "酱")
        return dryMarkers.none { marker -> food.name.contains(marker) } && food.basis != "干重"
    }
    private fun preferredGaps(current: NutritionSnapshot, targets: NutritionTargets): List<String> = buildList {
        if (current.proteinG < targets.protein.min) add("蛋白质")
        if (current.fiberG < targets.fiber.min) add("膳食纤维")
        if (current.potassiumMg < targets.potassium.min) add("钾")
        if (current.carbohydrateG < targets.carbohydrate.min) add("碳水")
    }

    private fun reasonFor(
        food: FoodItem,
        grams: Double,
        gaps: List<String>,
        allLimitsExceeded: Boolean,
    ): String = when {
        allLimitsExceeded -> "今日多项上限已较高，优先选择清淡低负担的${FoodKind.of(food).label}，建议 ${grams.toInt()}g"
        gaps.isNotEmpty() -> "优先补充${gaps.take(2).joinToString("、")}，建议 ${grams.toInt()}g"
        else -> "今日营养较均衡，选择${FoodKind.of(food).label}作为轻量补充，建议 ${grams.toInt()}g"
    }
}
