package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Version047ActivityRulesTest {
    private val profile = BodyProfile(30, Sex.MALE, 175.0, 70.0)

    @Test
    fun walkingStepsUseWeightBasedGrossEnergyMinusRestingShare() {
        assertEquals(38.5, NutritionEngine.estimatedWalkingGrossKcal(70.0, 1_000), 0.01)
        assertEquals(154.0, NutritionEngine.estimatedWalkingGrossKcal(70.0, 4_000), 0.01)
        assertEquals(126.0, NutritionEngine.estimatedWalkingNetKcal(70.0, 4_000), 0.01)
    }

    @Test
    fun energyTargetAddsManualExerciseAndNetWalkingToRestingEnergy() {
        val plan = DailyActivityPlan(manualExerciseKcal = 120.0, steps = 4_000)
        val breakdown = NutritionEngine.energyBreakdown(profile, plan)

        assertEquals(1648.75, breakdown.restingKcal, 0.01)
        assertEquals(120.0, breakdown.manualExerciseKcal, 0.01)
        assertEquals(126.0, breakdown.walkingKcal, 0.01)
        assertEquals(246.0, breakdown.activityKcal, 0.01)
        assertEquals(1894.75, breakdown.target.centerKcal, 0.01)
    }

    @Test
    fun zeroExerciseAndZeroStepsStillUseCompleteRestingEnergy() {
        val breakdown = NutritionEngine.energyBreakdown(profile, DailyActivityPlan(manualExerciseKcal = 0.0, steps = 0))

        assertEquals(1648.75, breakdown.restingKcal, 0.01)
        assertEquals(0.0, breakdown.activityKcal, 0.01)
        assertEquals(1648.75, breakdown.target.centerKcal, 0.01)
    }

    @Test
    fun dailyPlanValidatesInputsAndLegacyActivityMigratesSafely() {
        assertEquals(0.0, DailyActivityPlan.normalizeManualExercise(""), 0.01)
        assertEquals(0.0, DailyActivityPlan.normalizeManualExercise("-20"), 0.01)
        assertEquals(120.0, DailyActivityPlan.normalizeManualExercise("120"), 0.01)
        assertEquals(0, DailyActivityPlan.normalizeSteps("bad"))
        assertEquals(4_000, DailyActivityPlan.normalizeSteps("4000"))

        val legacy = BackupActivityPlan(date = "2026-11-23", activity = ActivityLevel.ACTIVE.name, activityKcal = 620.0)
        val decoded = DataCodec.decode(
            DataCodec.exportState(PersistedState(activityPlans = mapOf(LocalDate.of(2026, 11, 23) to DailyActivityPlan(manualExerciseKcal = 620.0))))
                .replace("\"manualExerciseKcal\":620.0", "\"activityKcal\":620.0")
                .replace("\"steps\":0", "\"steps\":0"),
        )

        assertEquals(620.0, decoded.activityPlans[LocalDate.of(2026, 11, 23)]!!.manualExerciseKcal, 0.01)
        assertEquals(0, decoded.activityPlans[LocalDate.of(2026, 11, 23)]!!.steps)
        assertEquals("ACTIVE", legacy.activity)
    }

    @Test
    fun calendarActivitySummaryExposesManualAndWalkingEnergy() {
        val summary = CalendarActivitySummary.from(
            profile,
            DailyActivityPlan(manualExerciseKcal = 120.0, steps = 4_000),
        )

        assertEquals("120 kcal", summary.manualExerciseText)
        assertEquals("4,000 步", summary.stepsText)
        assertEquals("126 kcal", summary.walkingText)
        assertEquals("246 kcal", summary.totalActivityText)
        assertEquals("1895 kcal", summary.dailyTargetText)
    }
}
