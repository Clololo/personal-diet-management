package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class Version050RulesTest {
    private val profile = BodyProfile(30, Sex.MALE, 175.0, 70.0)

    @Test
    fun bodyMeasurementsKeepOneValuePerDateAndReplaceTheSameDay() {
        val date = LocalDate.of(2026, 11, 24)
        val first = BodyMeasurement(date, weightKg = 70.0, waistCm = 80.0, bodyFatPercent = 18.0)
        val replacement = first.copy(weightKg = 69.6, note = "晨起")

        val one = BodyMeasurementRules.upsert(emptyList(), first)
        val two = BodyMeasurementRules.upsert(one, replacement)

        assertEquals(1, two.size)
        assertEquals(69.6, two.single().weightKg, 0.01)
        assertEquals("晨起", two.single().note)
    }

    @Test
    fun mealTemplateCanBeAppliedWithoutChangingHistoricalLogs() {
        val rice = EntryFoodCatalog.items.first { it.name == "米饭" }
        val egg = EntryFoodCatalog.items.first { it.name == "番茄炒蛋" }
        val template = MealTemplate(
            id = 1,
            name = "午餐组合",
            meal = Meal.LUNCH,
            items = listOf(
                MealTemplateItem(rice, 150.0, OilLevel.ZERO, SaltLevel.ZERO),
                MealTemplateItem(egg, 200.0, OilLevel.LOW, SaltLevel.LOW),
            ),
            createdAt = LocalDate.of(2026, 11, 24),
            updatedAt = LocalDate.of(2026, 11, 24),
        )
        val date = LocalDate.of(2026, 11, 25)

        val logs = MealTemplateRules.toLogs(template, date, startId = 10)

        assertEquals(2, logs.size)
        assertEquals(listOf(10L, 11L), logs.map { it.id })
        assertTrue(logs.all { it.meal == Meal.LUNCH })
        assertEquals(150.0, logs[0].grams, 0.01)
        assertEquals(OilLevel.LOW, logs[1].oil)
    }

    @Test
    fun adaptiveTargetIsOffByDefaultAndUsesWeightTrendOnlyWhenEnabled() {
        val start = LocalDate.of(2026, 11, 1)
        val measurements = (0..27).filter { it % 3 == 0 }.map { offset ->
            BodyMeasurement(start.plusDays(offset.toLong()), weightKg = 70.0 - offset * 0.02)
        }
        val totals = (0..27).associate { offset ->
            start.plusDays(offset.toLong()) to NutritionSnapshot(2200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
        val formula = NutritionEngine.energyTarget(profile)
        val plan = DailyActivityPlan()

        val disabled = AdaptiveEnergyEngine.evaluate(
            enabled = false,
            effectiveFrom = null,
            date = start.plusDays(27),
            profile = profile,
            plan = plan,
            measurements = measurements,
            dailyTotals = totals,
        )
        val enabled = AdaptiveEnergyEngine.evaluate(
            enabled = true,
            effectiveFrom = start,
            date = start.plusDays(27),
            profile = profile,
            plan = plan,
            measurements = measurements,
            dailyTotals = totals,
        )

        assertEquals(formula.centerKcal, disabled.centerKcal, 0.01)
        assertFalse(disabled.usedTrend)
        assertTrue(enabled.usedTrend)
        assertNotNull(enabled.reason)
        assertTrue(enabled.sampleDays >= 7)
        assertTrue(enabled.centerKcal in (formula.centerKcal * 0.85)..(formula.centerKcal * 1.15))
    }

    @Test
    fun adaptiveTargetFallsBackWhenHistoryIsTooShort() {
        val result = AdaptiveEnergyEngine.evaluate(
            enabled = true,
            effectiveFrom = LocalDate.of(2026, 11, 1),
            date = LocalDate.of(2026, 11, 2),
            profile = profile,
            plan = DailyActivityPlan(),
            measurements = listOf(BodyMeasurement(LocalDate.of(2026, 11, 1), 70.0)),
            dailyTotals = emptyMap(),
        )

        assertFalse(result.usedTrend)
        assertEquals(NutritionEngine.energyTarget(profile).centerKcal, result.centerKcal, 0.01)
        assertTrue(result.reason.contains("不足"))
    }

    @Test
    fun enabledAdaptiveTargetIsDeterministicAndExplained() {
        val start = LocalDate.of(2026, 11, 1)
        val measurements = (0..20).filter { it % 2 == 0 }.map { offset ->
            BodyMeasurement(start.plusDays(offset.toLong()), 72.0 - offset * 0.03)
        }
        val totals = (0..20).associate { offset ->
            start.plusDays(offset.toLong()) to NutritionSnapshot(2100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
        fun evaluate() = AdaptiveEnergyEngine.evaluate(
            enabled = true,
            effectiveFrom = start,
            date = start.plusDays(20),
            profile = profile,
            plan = DailyActivityPlan(),
            measurements = measurements,
            dailyTotals = totals,
        )

        val first = evaluate()
        val second = evaluate()

        assertEquals(first.centerKcal, second.centerKcal, 0.01)
        assertTrue(first.usedTrend)
        assertNotNull(first.trendKcal)
        assertTrue(first.reason.contains("体重"))
    }

    @Test
    fun backupSchemaSevenRoundTripsTemplatesMeasurementsAndAdaptiveSettings() {
        val rice = EntryFoodCatalog.items.first { it.name == "米饭" }
        val template = MealTemplate(
            5, "晚餐组合", Meal.DINNER,
            listOf(MealTemplateItem(rice, 180.0, OilLevel.ZERO, SaltLevel.ZERO)),
            LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24), 3,
        )
        val state = PersistedState(
            templates = listOf(template),
            measurements = listOf(BodyMeasurement(LocalDate.of(2026, 11, 24), 69.8)),
            adaptiveSettings = AdaptiveEnergySettings(enabled = true, effectiveFrom = LocalDate.of(2026, 11, 20)),
        )

        val json = DataCodec.exportState(state)
        val decoded = DataCodec.decode(json)

        assertTrue(DataCodec.isValidBackup(json))
        assertEquals(1, decoded.templates.size)
        assertEquals(180.0, decoded.templates.single().items.single().grams, 0.01)
        assertEquals(69.8, decoded.measurements.single().weightKg, 0.01)
        assertTrue(decoded.adaptiveSettings.enabled)
    }

    @Test
    fun convenienceNoodlesDistinguishDryCakeAndSeasoningPack() {
        val plain = EntryFoodCatalog.items.first { it.name == "油炸方便面饼（仅面饼）" }
        val packed = EntryFoodCatalog.items.first { it.name == "油炸方便面（含调料包整套）" }

        assertFalse(plain.includesSeasoningPack!!)
        assertTrue(packed.includesSeasoningPack!!)
        assertTrue(packed.nutrients.sodiumMg > plain.nutrients.sodiumMg)
        assertEquals(FoodDataQuality.BRAND_LABEL, packed.dataQuality)
    }

    @Test
    fun autoBackupKeepsSevenDailySnapshotsAndCreatesOnePerDay() {
        val morning = LocalDateTime.of(2026, 11, 24, 8, 0)
        val evening = LocalDateTime.of(2026, 11, 24, 20, 0)
        val existing = (0..8).map { offset ->
            AutoBackupFile(
                name = "backup-${offset}.json",
                modifiedAt = LocalDateTime.of(2026, 11, 20, 10, 0).plusDays(offset.toLong()),
                sizeBytes = 100,
            )
        }

        assertFalse(AutoBackupRules.needsDailyBackup(existing, morning))
        assertFalse(AutoBackupRules.needsDailyBackup(existing, evening))
        assertEquals(7, AutoBackupRules.keepNewest(existing, 7).size)
    }

    @Test
    fun additionalCatalogEnrichesEveryMajorFoodKind() {
        val extras = AdditionalFoodCatalog.items
        assertTrue(extras.size >= 24)
        assertTrue(extras.any { it.category == "主食" })
        assertTrue(extras.any { it.category == "菜" })
        assertTrue(extras.any { it.category == "水果" })
        assertTrue(extras.any { it.category == "食材" })
        assertTrue(extras.any { it.category == "饮品" })
        assertEquals(extras.size, extras.map { it.name }.distinct().size)
    }
}
