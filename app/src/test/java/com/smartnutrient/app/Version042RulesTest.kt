package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Version042RulesTest {
    private val profile = BodyProfile(30, Sex.MALE, 175.0, 70.0)

    @Test
    fun weeklyWindowUsesSevenCalendarDaysBeforeSelectedDate() {
        assertEquals(
            listOf(18, 17, 16, 15, 14, 13, 12).map { LocalDate.of(2026, 11, it) },
            WeeklyAnalysis.previousSevenDays(LocalDate.of(2026, 11, 19)),
        )
    }

    @Test
    fun weeklyDeviationComparesAggregateIntakeWithAggregateRange() {
        val dates = listOf(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 19))
        val totals = mapOf(
            dates[0] to NutritionSnapshot(1200.0, 50.0, 30.0, 8.0, 140.0, 5.0, 12.0, 1800.0, 2500.0, 180.0, 20.0),
            dates[1] to NutritionSnapshot(2500.0, 90.0, 80.0, 30.0, 260.0, 4.0, 30.0, 2600.0, 2000.0, 420.0, 60.0),
        )
        val targets = mapOf(
            dates[0] to targets(1800.0, 2200.0, 50.0, 80.0, 44.0, 67.0, 23.0, 225.0, 325.0, 30.0, 50.0, 2000.0, 3000.0, 300.0),
            dates[1] to targets(1900.0, 2200.0, 55.0, 85.0, 42.0, 64.0, 22.0, 214.0, 309.0, 28.0, 50.0, 2000.0, 3000.0, 300.0),
        )

        val result = WeeklyAnalysis.deviationSummary(dates, totals) { targets[it]!! }

        assertEquals(2, result.filledDays)
        assertEquals(2, result.totalDays)
        val calories = result.metrics.first { it.label == "热量" }
        assertEquals(0.0, calories.shortfall, 0.01)
        assertEquals(0.0, calories.excess, 0.01)
        assertEquals("位于推荐区间内", calories.deviationText)
        assertEquals(3700.0, result.metrics.first { it.label == "热量" }.actual, 0.01)
        assertEquals(3700.0, result.metrics.first { it.label == "热量" }.recommendedMin, 0.01)
        assertEquals(4400.0, result.metrics.first { it.label == "热量" }.recommendedMax, 0.01)
        assertEquals(400.0, result.metrics.first { it.label == "钠" }.excess, 0.01)
        assertEquals(49.0, result.metrics.first { it.label == "膳食纤维" }.shortfall, 0.01)
        assertTrue(result.metrics.none { it.shortfall > 0.0 && it.excess > 0.0 })
    }

    @Test
    fun weeklyMetricFormatsUnitsAndExplicitDeviationText() {
        val dates = listOf(LocalDate.of(2026, 11, 18))
        val totals = mapOf(
            dates[0] to NutritionSnapshot(1200.0, 50.0, 30.0, 8.0, 140.0, 5.0, 12.0, 1800.0, 2500.0, 180.0, 20.0),
        )

        val metric = WeeklyAnalysis.deviationSummary(dates, totals) {
            targets(1800.0, 2200.0, 50.0, 80.0, 44.0, 67.0, 23.0, 225.0, 325.0, 30.0, 50.0, 2000.0, 3000.0, 300.0)
        }.metrics.first { it.label == "热量" }

        assertEquals("1200 kcal", metric.actualText)
        assertEquals("1800–2200 kcal", metric.recommendedText)
        assertEquals("低于推荐下限 600 kcal", metric.deviationText)
    }

    @Test
    fun deviationSummaryWorksWithProductionNutritionTargets() {
        val dates = listOf(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 19))
        val totals = dates.associateWith {
            NutritionSnapshot(1200.0, 50.0, 30.0, 8.0, 140.0, 5.0, 12.0, 1800.0, 2500.0, 180.0, 20.0)
        }
        val productionTargets = NutritionEngine.displayTargets(
            NutritionEngine.energyTarget(profile),
            NutritionEngine.energyTarget(profile).centerKcal,
            profile.sex,
            profile.age,
        )

        val result = WeeklyAnalysis.deviationSummary(dates, totals) { productionTargets }

        assertEquals(10, result.metrics.size)
        assertTrue(result.metrics.any { it.label == "碳水化合物" })
    }

    @Test
    fun profileVersionsUseLatestVersionOnOrBeforeDate() {
        val newer = profile.copy(age = 40)
        val newerStill = profile.copy(age = 41)
        val versions = mapOf(
            LocalDate.of(2026, 11, 18) to newer,
            LocalDate.of(2026, 11, 21) to newerStill,
        )

        assertEquals(profile, DailyPlans.profileOn(versions, profile, LocalDate.of(2026, 11, 17)))
        assertEquals(newer, DailyPlans.profileOn(versions, profile, LocalDate.of(2026, 11, 20)))
        assertEquals(newerStill, DailyPlans.profileOn(versions, profile, LocalDate.of(2026, 11, 21)))
    }

    @Test
    fun activityPlansInheritMostRecentSetting() {
        val plan = DailyActivityPlan(manualExerciseKcal = 620.0, steps = 4_000)
        val plans = mapOf(LocalDate.of(2026, 11, 19) to plan)

        assertEquals(DailyActivityPlan(), DailyPlans.activityPlanOn(plans, DailyActivityPlan(), LocalDate.of(2026, 11, 18)))
        assertEquals(DailyActivityPlan(), DailyPlans.activityPlanOn(plans, DailyActivityPlan(), LocalDate.of(2026, 11, 20)))
        assertEquals(plan, DailyPlans.activityPlanOn(plans, DailyActivityPlan(), LocalDate.of(2026, 11, 19)))
    }

    @Test
    fun backupSchemaFiveRoundTripsHistoricalProfileAndActivity() {
        val state = PersistedState(
            profile = profile,
            profileVersions = mapOf(LocalDate.of(2026, 11, 18) to profile.copy(age = 39)),
            activityPlans = mapOf(LocalDate.of(2026, 11, 19) to DailyActivityPlan(manualExerciseKcal = 420.0, steps = 3_000)),
        )
        val json = DataCodec.exportState(state)

        assertTrue(DataCodec.isValidBackup(json))
        val decoded = DataCodec.decode(json)
        assertEquals(39, decoded.profileVersions[LocalDate.of(2026, 11, 18)]!!.age)
        assertEquals(420.0, decoded.activityPlans[LocalDate.of(2026, 11, 19)]!!.manualExerciseKcal, 0.01)
        assertEquals(3_000, decoded.activityPlans[LocalDate.of(2026, 11, 19)]!!.steps)
    }

    @Test
    fun legacySchemaFourBackupStillRestores() {
        val state = PersistedState(profile = profile, customFoods = listOf(EntryFoodCatalog.items.first { it.name == "橘子" }))
        val json = DataCodec.exportState(state).replace("\"schemaVersion\":5", "\"schemaVersion\":4")

        assertTrue(DataCodec.isValidBackup(json))
        val decoded = DataCodec.decode(json)
        assertEquals(profile, decoded.profile)
        assertTrue(decoded.profileVersions.isEmpty())
        assertTrue(decoded.activityPlans.isEmpty())
    }

    @Test
    fun drinksAreIncludedAndUseMillilitrePresets() {
        val milkTea = EntryFoodCatalog.items.first { it.id == 70020 }

        assertEquals("饮品", milkTea.category)
        assertEquals("每100ml", milkTea.basis)
        assertEquals(80.0, milkTea.nutrients.caloriesKcal, 0.01)
        assertEquals(listOf(100.0, 200.0, 250.0, 330.0, 500.0), ServingPresetRules.forFood(milkTea))
        assertTrue(EntryFoodCatalog.items.count { FoodKind.of(it) == FoodKind.DRINK } >= 50)
    }

    @Test
    fun drinkCalculatesByEnteredMillilitres() {
        val cola = EntryFoodCatalog.items.first { it.name == "可乐" }
        val snapshot = NutritionEngine.calculateForRecord(cola, 330.0)

        assertEquals(141.9, snapshot.caloriesKcal, 0.01)
        assertEquals(34.98, snapshot.sugarG, 0.01)
    }

    private fun targets(
        calorieMin: Double, calorieMax: Double, proteinMin: Double, proteinMax: Double,
        fatMin: Double, fatMax: Double, satMax: Double, carbMin: Double, carbMax: Double,
        fiberMin: Double, sugarMax: Double, sodiumMax: Double, potassiumMin: Double, cholesterolMax: Double,
    ) = NutritionTargets(
        NutrientTarget("热量", calorieMin, calorieMax, "kcal", false),
        NutrientTarget("蛋白质", proteinMin, proteinMax, "g", false),
        NutrientTarget("脂肪", fatMin, fatMax, "g", true),
        NutrientTarget("饱和脂肪", 0.0, satMax, "g", true),
        NutrientTarget("碳水化合物", carbMin, carbMax, "g", false),
        NutrientTarget("膳食纤维", fiberMin, 0.0, "g", false),
        NutrientTarget("添加糖", 0.0, sugarMax, "g", true),
        NutrientTarget("钠", 0.0, sodiumMax, "mg", true),
        NutrientTarget("钾", potassiumMin, 0.0, "mg", false),
        NutrientTarget("胆固醇", 0.0, cholesterolMax, "mg", true),
    )
}
