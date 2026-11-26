package com.smartnutrient.app

import java.time.LocalDate

data class DailyActivityPlan(
    val manualExerciseKcal: Double = 0.0,
    val steps: Int = 0,
) {
    companion object {
        fun normalizeManualExercise(text: String): Double =
            text.toDoubleOrNull()?.takeIf { it in 0.0..3000.0 } ?: 0.0

        fun normalizeSteps(text: String): Int =
            text.toIntOrNull()?.coerceIn(0, 100_000) ?: 0
    }
}

data class CalendarActivitySummary(
    val manualExerciseKcal: Double,
    val steps: Int,
    val walkingKcal: Double,
    val totalActivityKcal: Double,
    val restingKcal: Double,
    val dailyTargetKcal: Double,
) {
    val manualExerciseText: String get() = "${manualExerciseKcal.toInt()} kcal"
    val stepsText: String get() = "%,d 步".format(steps)
    val walkingText: String get() = "${walkingKcal.toInt()} kcal"
    val totalActivityText: String get() = "${totalActivityKcal.toInt()} kcal"
    val dailyTargetText: String get() = "${kotlin.math.round(dailyTargetKcal).toInt()} kcal"

    companion object {
        fun from(profile: BodyProfile, plan: DailyActivityPlan): CalendarActivitySummary {
            val breakdown = NutritionEngine.energyBreakdown(profile, plan)
            return CalendarActivitySummary(
                manualExerciseKcal = breakdown.manualExerciseKcal,
                steps = plan.steps,
                walkingKcal = breakdown.walkingKcal,
                totalActivityKcal = breakdown.activityKcal,
                restingKcal = breakdown.restingKcal,
                dailyTargetKcal = breakdown.target.centerKcal,
            )
        }
    }
}

object DailyPlans {
    fun applyProfileVersion(
        versions: Map<LocalDate, BodyProfile>,
        fallback: BodyProfile,
        date: LocalDate,
        value: BodyProfile,
    ): Map<LocalDate, BodyProfile> {
        if (versions[date] == value) return versions
        return versions + (date to value)
    }

    fun profileOn(versions: Map<LocalDate, BodyProfile>, fallback: BodyProfile, date: LocalDate): BodyProfile {
        val effective = versions.entries
            .filter { it.key <= date }
            .maxByOrNull { it.key }
            ?.value
        return effective ?: fallback
    }

    fun profileHistory(versions: Map<LocalDate, BodyProfile>, fallback: BodyProfile): List<LocalDate> =
        versions.keys.sortedDescending()

    fun applyActivityPlan(
        plans: Map<LocalDate, DailyActivityPlan>,
        fallback: DailyActivityPlan,
        date: LocalDate,
        value: DailyActivityPlan,
    ): Map<LocalDate, DailyActivityPlan> {
        if (plans[date] == value) return plans
        return plans + (date to value)
    }

    fun activityPlanOn(plans: Map<LocalDate, DailyActivityPlan>, fallback: DailyActivityPlan, date: LocalDate): DailyActivityPlan =
        plans[date] ?: fallback

    fun profileForDate(
        profile: BodyProfile,
        profileVersions: Map<LocalDate, BodyProfile>,
        activityPlans: Map<LocalDate, DailyActivityPlan>,
        date: LocalDate,
    ): BodyProfile {
        return profileOn(profileVersions, profile, date)
    }

    fun updateActivityForDate(
        profile: BodyProfile,
        plan: DailyActivityPlan,
    ): BodyProfile = profile
}
