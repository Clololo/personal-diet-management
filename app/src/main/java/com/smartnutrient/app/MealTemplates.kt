package com.smartnutrient.app

import java.time.LocalDate

data class MealTemplateItem(
    val food: FoodItem,
    val grams: Double,
    val oil: OilLevel = OilLevel.ZERO,
    val salt: SaltLevel = SaltLevel.ZERO,
)

data class MealTemplate(
    val id: Long,
    val name: String,
    val meal: Meal,
    val items: List<MealTemplateItem>,
    val createdAt: LocalDate,
    val updatedAt: LocalDate,
    val usageCount: Int = 0,
) {
    fun isValid(): Boolean = name.isNotBlank() && items.isNotEmpty() && items.all { it.grams > 0.0 }
}

object MealTemplateRules {
    fun toLogs(template: MealTemplate, date: LocalDate, startId: Long): List<FoodLog> =
        template.items.mapIndexed { index, item ->
            FoodLog(startId + index, item.food, item.grams, template.meal, item.oil, item.salt)
        }

    fun upsert(templates: List<MealTemplate>, value: MealTemplate): List<MealTemplate> {
        if (!value.isValid()) return templates
        val existing = templates.indexOfFirst { it.id == value.id }
        return if (existing >= 0) templates.toMutableList().also { it[existing] = value } else templates + value
    }

    fun remove(templates: List<MealTemplate>, id: Long): List<MealTemplate> = templates.filterNot { it.id == id }

    fun markUsed(templates: List<MealTemplate>, id: Long, date: LocalDate): List<MealTemplate> =
        templates.map { if (it.id == id) it.copy(usageCount = it.usageCount + 1, updatedAt = date) else it }
}

data class BodyMeasurement(
    val date: LocalDate,
    val weightKg: Double,
    val waistCm: Double? = null,
    val bodyFatPercent: Double? = null,
    val note: String = "",
)

object BodyMeasurementRules {
    fun upsert(values: List<BodyMeasurement>, value: BodyMeasurement): List<BodyMeasurement> =
        (values.filterNot { it.date == value.date } + value).sortedBy { it.date }

    fun between(values: List<BodyMeasurement>, start: LocalDate, end: LocalDate): List<BodyMeasurement> =
        values.filter { it.date in start..end }.sortedBy { it.date }
}

data class AdaptiveEnergySettings(
    val enabled: Boolean = false,
    val effectiveFrom: LocalDate? = null,
)

data class AdaptiveEnergyEvaluation(
    val centerKcal: Double,
    val usedTrend: Boolean,
    val sampleDays: Int,
    val reason: String,
    val formulaKcal: Double,
    val trendKcal: Double?,
)

object AdaptiveEnergyEngine {
    fun evaluate(
        enabled: Boolean,
        effectiveFrom: LocalDate?,
        date: LocalDate,
        profile: BodyProfile,
        plan: DailyActivityPlan,
        measurements: List<BodyMeasurement>,
        dailyTotals: Map<LocalDate, NutritionSnapshot>,
        windowDays: Int = 28,
    ): AdaptiveEnergyEvaluation {
        val formula = NutritionEngine.energyTarget(profile, plan).centerKcal
        if (!enabled || effectiveFrom == null || date < effectiveFrom) {
            return AdaptiveEnergyEvaluation(formula, false, 0, "趋势目标未启用，使用公式目标", formula, null)
        }
        val start = date.minusDays((windowDays - 1).toLong()).coerceAtLeast(effectiveFrom)
        val windowMeasurements = BodyMeasurementRules.between(measurements, start, date)
        val filledTotals = dailyTotals.filterKeys { it in start..date && it in dailyTotals }
        if (windowMeasurements.size < 4 || filledTotals.size < 7) {
            return AdaptiveEnergyEvaluation(formula, false, minOf(windowMeasurements.size, filledTotals.size), "趋势样本不足，使用公式目标", formula, null)
        }
        val first = windowMeasurements.first()
        val last = windowMeasurements.last()
        val days = java.time.temporal.ChronoUnit.DAYS.between(first.date, last.date).coerceAtLeast(1).toDouble()
        val averageIntake = filledTotals.values.sumOf { it.caloriesKcal } / filledTotals.size
        val weightChange = last.weightKg - first.weightKg
        val energyBalance = weightChange * 7700.0 / days
        val trend = (averageIntake - energyBalance).coerceIn(formula * 0.85, formula * 1.15)
        return AdaptiveEnergyEvaluation(
            centerKcal = trend,
            usedTrend = true,
            sampleDays = filledTotals.size,
            reason = "根据 ${windowMeasurements.size} 次体重测量、${filledTotals.size} 个饮食记录日，估算实际消耗",
            formulaKcal = formula,
            trendKcal = trend,
        )
    }
}

enum class FoodDataQuality(val label: String) {
    MEASURED("实测"),
    BRAND_LABEL("品牌标签"),
    RECIPE_ESTIMATE("标准配方估算"),
    GENERIC_REFERENCE("泛称参考"),
    PENDING_REVIEW("待复核"),
}

