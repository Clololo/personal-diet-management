package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Test

class TodayLayoutRulesTest {
    @Test
    fun activityCardSitsAfterSnackAndBeforeRecommendation() {
        val sections = TodayLayoutRules.sections
        val snack = sections.indexOf(TodaySection.SNACK)
        val activity = sections.indexOf(TodaySection.ACTIVITY)
        val recommendation = sections.indexOf(TodaySection.RECOMMENDATION)

        assertEquals(snack + 1, activity)
        assertEquals(activity + 1, recommendation)
    }

    @Test
    fun manualExerciseAndStepsRejectInvalidInputs() {
        assertEquals(0.0, DailyActivityPlan.normalizeManualExercise(""), 0.01)
        assertEquals(0.0, DailyActivityPlan.normalizeManualExercise("not-a-number"), 0.01)
        assertEquals(420.0, DailyActivityPlan.normalizeManualExercise("420"), 0.01)
        assertEquals(0, DailyActivityPlan.normalizeSteps("bad"))
    }
}
