package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class Version040RulesTest {
    @Test
    fun backupInspectionReportsRestorePreviewData() {
        val state = PersistedState(
            days = mapOf(
                LocalDate.of(2026, 11, 20) to DayLog(LocalDate.of(2026, 11, 20), listOf(log(1, "米饭", Meal.LUNCH)), 500),
                LocalDate.of(2026, 11, 21) to DayLog(LocalDate.of(2026, 11, 21), listOf(log(2, "鸡蛋", Meal.BREAKFAST)), 750),
            ),
            customFoods = listOf(customFood(100_001)),
            profile = BodyProfile(37, Sex.FEMALE, 165.0, 58.0),
        )

        val preview = DataCodec.inspect(DataCodec.exportState(state))

        assertTrue(preview.valid)
        assertEquals(2, preview.dayCount)
        assertEquals(2, preview.entryCount)
        assertEquals(1, preview.customFoodCount)
        assertEquals(LocalDate.of(2026, 11, 20), preview.firstDate)
        assertEquals(LocalDate.of(2026, 11, 21), preview.lastDate)
        assertTrue(preview.profileSummary.contains("37"))
    }

    @Test
    fun invalidBackupInspectionIsExplicitAndDoesNotThrow() {
        val preview = DataCodec.inspect("{not-json")

        assertFalse(preview.valid)
        assertTrue(preview.message.isNotBlank())
    }

    @Test
    fun manualExerciseAndStepsAreAddedToCompleteRestingEnergy() {
        val profile = BodyProfile(age = 30, sex = Sex.MALE, heightCm = 175.0, weightKg = 70.0)
        val plan = DailyActivityPlan(manualExerciseKcal = 520.0, steps = 4_000)

        assertEquals(646.0, NutritionEngine.activityEnergy(profile, plan), 0.01)
    }

    @Test
    fun quickFoodRulesPutRecentFirstAndDeduplicateFrequentHistory() {
        val rice = food("米饭")
        val egg = food("鸡蛋")
        val tomatoEgg = food("番茄炒蛋")
        val history = listOf(
            log(1, tomatoEgg, Meal.LUNCH), log(2, tomatoEgg, Meal.DINNER),
            log(3, rice, Meal.BREAKFAST), log(4, rice, Meal.LUNCH),
            log(5, egg, Meal.BREAKFAST),
        )

        val result = QuickFoodRules.suggestions(
            foods = EntryFoodCatalog.items,
            recentIds = listOf(egg.id),
            allEntries = history,
            favorites = setOf(rice.id),
            limit = 4,
        )

        assertEquals(egg.name, result.first().food.name)
        assertEquals(result.size, result.map { it.food.id }.distinct().size)
        assertTrue(result.any { it.food.name == "番茄炒蛋" })
        assertTrue(result.any { it.food.name == "米饭" })
    }

    @Test
    fun servingPresetsRespectFoodBasisAndKind() {
        assertEquals(listOf(100.0, 150.0, 200.0), ServingPresetRules.forFood(food("米饭")))
        assertEquals(listOf(100.0, 150.0, 200.0), ServingPresetRules.forFood(food("苹果")))
        assertTrue(ServingPresetRules.forFood(food("生抽")).all { it <= 15.0 })
    }

    @Test
    fun recommendationsHonorPersistentExclusions() {
        val targets = NutritionEngine.displayTargets(EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30)
        val logs = listOf(log(1, "米饭", Meal.BREAKFAST), log(2, "番茄炒蛋", Meal.LUNCH))
        val baseline = RecommendationEngine.recommend(logs, targets, EntryFoodCatalog.items, limit = 3)
        val excluded = baseline.first().food.name

        val filtered = RecommendationEngine.recommend(
            logs, targets, EntryFoodCatalog.items, limit = 3, excludedNames = setOf(excluded),
        )

        assertTrue(baseline.isNotEmpty())
        assertTrue(filtered.none { it.food.name == excluded })
    }

    @Test
    fun mergeKeepsRecommendationExclusionsFromBothSides() {
        val merged = StateMergeRules.merge(
            PersistedState(ignoredRecommendationNames = listOf("黄豆")),
            PersistedState(ignoredRecommendationNames = listOf("扁豆", "黄豆")),
        )

        assertEquals(listOf("黄豆", "扁豆"), merged.ignoredRecommendationNames)
    }

    @Test
    fun legacySchemaWithoutActivityModeRemainsRestorable() {
        val state = PersistedState(days = mapOf(java.time.LocalDate.of(2026, 11, 22) to DayLog(java.time.LocalDate.of(2026, 11, 22), listOf(log(1, "米饭", Meal.LUNCH)))))
        val json = DataCodec.exportState(state)
            .replace("\"schemaVersion\":4", "\"schemaVersion\":2")
            .replace("\"activityMode\":\"MANUAL\",", "")

        assertTrue(DataCodec.isValidBackup(json))
        assertEquals(ActivityEnergyMode.AUTO, DataCodec.decode(json).profile.activityMode)
    }
    @Test
    fun backupRoundTripKeepsActivityModeAndRecommendationExclusions() {
        val state = PersistedState(
            profile = BodyProfile(40, Sex.FEMALE, 165.0, 60.0),
            activityPlans = mapOf(LocalDate.of(2026, 11, 23) to DailyActivityPlan(manualExerciseKcal = 480.0, steps = 2_000)),
            ignoredRecommendationNames = listOf("黄豆"),
        )

        val decoded = DataCodec.decode(DataCodec.exportState(state))

        assertEquals(480.0, decoded.activityPlans[LocalDate.of(2026, 11, 23)]!!.manualExerciseKcal, 0.01)
        assertEquals(2_000, decoded.activityPlans[LocalDate.of(2026, 11, 23)]!!.steps)
        assertEquals(listOf("黄豆"), decoded.ignoredRecommendationNames)
    }

    private fun food(name: String): FoodItem = EntryFoodCatalog.items.first { it.name == name }
    private fun log(id: Long, name: String, meal: Meal): FoodLog = FoodLog(id, food(name), 100.0, meal, OilLevel.ZERO, SaltLevel.ZERO)
    private fun log(id: Long, food: FoodItem, meal: Meal): FoodLog = FoodLog(id, food, 100.0, meal, OilLevel.ZERO, SaltLevel.ZERO)
    private fun customFood(id: Int) = FoodItem(id, "自制测试", "", "", "自定义", "每100g", FoodNutrients(100.0, 5.0, 2.0, 0.5, 10.0, 1.0, 1.0, 100.0, 100.0, 0.0))
}
