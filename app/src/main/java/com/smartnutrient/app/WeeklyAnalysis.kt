package com.smartnutrient.app

import java.time.LocalDate

data class NutrientDeviation(
    val label: String,
    val unit: String,
    val actual: Double,
    val recommendedMin: Double,
    val recommendedMax: Double,
    val shortfall: Double,
    val excess: Double,
) {
    val hasDifference: Boolean get() = shortfall > 0.0 || excess > 0.0
    val actualText: String get() = "${WeeklyDisplayFormat.value(actual)} $unit"
    val recommendedText: String
        get() = when {
            recommendedMin > 0.0 && recommendedMax > 0.0 -> "${WeeklyDisplayFormat.value(recommendedMin)}–${WeeklyDisplayFormat.value(recommendedMax)} $unit"
            recommendedMin > 0.0 -> "至少 ${WeeklyDisplayFormat.value(recommendedMin)} $unit"
            recommendedMax > 0.0 -> "不超过 ${WeeklyDisplayFormat.value(recommendedMax)} $unit"
            else -> "无限制"
        }
    val deviationText: String
        get() = when {
            shortfall > 0.0 -> "低于推荐下限 ${WeeklyDisplayFormat.value(shortfall)} $unit"
            excess > 0.0 -> "高于推荐上限 ${WeeklyDisplayFormat.value(excess)} $unit"
            else -> "位于推荐区间内"
        }
}

internal object WeeklyDisplayFormat {
    fun value(value: Double): String =
        if (value >= 100.0) value.toInt().toString() else "%.1f".format(value)
}

data class WeeklyDeviationSummary(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val filledDays: Int,
    val totalDays: Int,
    val addedSugarKnown: Boolean,
    val metrics: List<NutrientDeviation>,
)

object WeeklyAnalysis {
    fun previousSevenDays(selectedDate: LocalDate): List<LocalDate> =
        (1..7).map { selectedDate.minusDays(it.toLong()) }

    fun deviationSummary(
        days: List<LocalDate>,
        totals: Map<LocalDate, NutritionSnapshot>,
        targetForDate: (LocalDate) -> NutritionTargets,
    ): WeeklyDeviationSummary {
        val periodStart = days.min() ?: throw IllegalArgumentException("At least one analysis date is required")
        val periodEnd = days.max() ?: periodStart
        val filledDates = days.filter { totals[it] != null }
        val addedSugarKnown = filledDates.isNotEmpty() && filledDates.all { totals[it]?.addedSugarG != null }
        val metrics = buildList {
            add(accumulate("热量", "kcal", filledDates, totals, targetForDate, { it.calories }) { total, _ -> total.caloriesKcal })
            add(accumulate("蛋白质", "g", filledDates, totals, targetForDate, { it.protein }) { total, _ -> total.proteinG })
            add(accumulate("脂肪", "g", filledDates, totals, targetForDate, { it.fat }) { total, _ -> total.fatG })
            add(accumulate("饱和脂肪", "g", filledDates, totals, targetForDate, { it.saturatedFat }) { total, _ -> total.saturatedFatG })
            add(accumulate("碳水化合物", "g", filledDates, totals, targetForDate, { it.carbohydrate }) { total, _ -> total.carbohydrateG })
            add(accumulate("膳食纤维", "g", filledDates, totals, targetForDate, { it.fiber }) { total, _ -> total.fiberG })
            add(accumulate("钠", "mg", filledDates, totals, targetForDate, { it.sodium }) { total, _ -> total.sodiumMg })
            add(accumulate("钾", "mg", filledDates, totals, targetForDate, { it.potassium }) { total, _ -> total.potassiumMg })
            add(accumulate("胆固醇", "mg", filledDates, totals, targetForDate, { it.cholesterol }) { total, _ -> total.cholesterolMg })
            if (addedSugarKnown) {
                add(accumulate("添加糖", "g", filledDates, totals, targetForDate, { it.sugar }) { total, _ -> total.addedSugarG ?: 0.0 })
            }
        }
        return WeeklyDeviationSummary(
            periodStart = periodStart,
            periodEnd = periodEnd,
            filledDays = filledDates.size,
            totalDays = days.size,
            addedSugarKnown = addedSugarKnown,
            metrics = metrics,
        )
    }

    private fun accumulate(
        label: String,
        unit: String,
        dates: List<LocalDate>,
        totals: Map<LocalDate, NutritionSnapshot>,
        targetForDate: (LocalDate) -> NutritionTargets,
        targetSelector: (NutritionTargets) -> NutrientTarget,
        valueFor: (NutritionSnapshot, NutrientTarget) -> Double,
    ): NutrientDeviation {
        var actual = 0.0
        var recommendedMin = 0.0
        var recommendedMax = 0.0
        dates.forEach { date ->
            val total = totals[date] ?: return@forEach
            val target = targetSelector(targetForDate(date))
            actual += valueFor(total, target)
            recommendedMin += target.min
            recommendedMax += target.max
        }
        return deviation(label, unit, actual, recommendedMin, recommendedMax)
    }

    private fun deviation(
        label: String,
        unit: String,
        actual: Double,
        recommendedMin: Double,
        recommendedMax: Double,
    ) = NutrientDeviation(
        label = label,
        unit = unit,
        actual = actual,
        recommendedMin = recommendedMin,
        recommendedMax = recommendedMax,
        shortfall = if (recommendedMin > 0.0 && actual < recommendedMin) recommendedMin - actual else 0.0,
        excess = if (recommendedMax > 0.0 && actual > recommendedMax) actual - recommendedMax else 0.0,
    )
}

