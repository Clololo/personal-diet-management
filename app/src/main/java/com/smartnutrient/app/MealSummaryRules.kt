package com.smartnutrient.app

data class WeeklySummary(
    val loggedDays: Int,
    val averageCalories: Double,
    val averageProteinG: Double,
    val averageFatG: Double = 0.0,
    val averageSaturatedFatG: Double = 0.0,
    val averageCarbohydrateG: Double = 0.0,
    val averageFiberG: Double,
    val averageSugarG: Double = 0.0,
    val averageSodiumMg: Double,
    val averagePotassiumMg: Double = 0.0,
    val averageCholesterolMg: Double = 0.0,
    val averageAddedSugarG: Double? = null,
    val entriesByDay: List<List<FoodLog>>
) {
    val average: NutritionSnapshot get() = NutritionSnapshot(
        averageCalories,
        averageProteinG,
        averageFatG,
        averageSaturatedFatG,
        averageCarbohydrateG,
        averageFiberG,
        averageSugarG,
        averageSodiumMg,
        averagePotassiumMg,
        averageCholesterolMg,
        averageAddedSugarG,
    )
}

object MealRules {
    fun copyMeal(source: List<FoodLog>, from: Meal, to: Meal, startId: Long): List<FoodLog> {
        if (from == to) return emptyList()
        return source.filter { it.meal == from }.mapIndexed { index, log ->
            log.copy(id = startId + index, meal = to)
        }
    }

    fun copyDay(source: List<FoodLog>, startId: Long): List<FoodLog> =
        source.mapIndexed { index, log -> log.copy(id = startId + index) }
}

object SummaryRules {
    fun weekly(loggedDays: Int, entriesByDay: List<List<FoodLog>>): WeeklySummary {
        val nonEmpty = entriesByDay.filter { it.isNotEmpty() }
        val totals = nonEmpty.map { logs ->
            val snapshots = logs.map { log -> NutritionEngine.recordSnapshot(log.food, log.grams, log.oil, log.salt) }
            DailyNutrition.total(snapshots)
        }
        val divisor = nonEmpty.size.coerceAtLeast(1)
        val average = if (totals.isEmpty()) {
            NutritionSnapshot(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        } else {
            averageSnapshot(DailyNutrition.total(totals), divisor)
        }
        return WeeklySummary(
            loggedDays = loggedDays,
            averageCalories = average.caloriesKcal,
            averageProteinG = average.proteinG,
            averageFatG = average.fatG,
            averageSaturatedFatG = average.saturatedFatG,
            averageCarbohydrateG = average.carbohydrateG,
            averageFiberG = average.fiberG,
            averageSugarG = average.sugarG,
            averageSodiumMg = average.sodiumMg,
            averagePotassiumMg = average.potassiumMg,
            averageCholesterolMg = average.cholesterolMg,
            averageAddedSugarG = average.addedSugarG,
            entriesByDay = entriesByDay,
        )
    }

    private fun averageSnapshot(total: NutritionSnapshot, divisor: Int) = NutritionSnapshot(
        total.caloriesKcal / divisor,
        total.proteinG / divisor,
        total.fatG / divisor,
        total.saturatedFatG / divisor,
        total.carbohydrateG / divisor,
        total.fiberG / divisor,
        total.sugarG / divisor,
        total.sodiumMg / divisor,
        total.potassiumMg / divisor,
        total.cholesterolMg / divisor,
        total.addedSugarG?.div(divisor),
    )
}
