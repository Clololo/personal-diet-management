package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SugarDataRulesTest {
    private val targets = NutritionEngine.displayTargets(
        EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30,
    )

    @Test
    fun wholeFruitAndPlainMilkCountNaturalSugarButNoAddedSugar() {
        val orange = food("橘子", 150.0)
        val milk = food("全脂牛奶", 250.0)

        assertEquals(13.5, orange.sugarG, 0.01)
        assertEquals(0.0, orange.addedSugarG!!, 0.01)
        assertEquals(SugarCoverage.COMPLETE, orange.sugarCoverage)
        assertEquals(12.5, milk.sugarG, 0.01)
        assertEquals(0.0, milk.addedSugarG!!, 0.01)
        assertEquals(SugarCoverage.COMPLETE, milk.sugarCoverage)

        val total = DailyNutrition.total(listOf(orange, milk))
        assertEquals(26.0, total.sugarG, 0.01)
        assertEquals(0.0, total.addedSugarG!!, 0.01)
    }

    @Test
    fun unknownAddedSugarPropagatesInsteadOfBecomingZero() {
        val orange = food("橘子", 150.0)
        val dish = food("番茄炒蛋", 200.0)
        val total = DailyNutrition.total(listOf(orange, dish))

        assertNull(total.addedSugarG)
        assertEquals(SugarCoverage.TOTAL_ONLY, total.sugarCoverage)
        assertFalse(TodayFocusRules.items(total, targets).any { it.label.contains("添加糖") })
    }

    @Test
    fun addedSugarTargetUsesTenPercentOfEnergyNotTenPercentOfTotalSugarCarbohydrate() {
        assertEquals(50.0, targets.sugar.max, 0.01)
        assertEquals("添加糖", targets.sugar.label)
    }

    @Test
    fun sugarSeasoningIsCompleteAddedSugar() {
        val sugar = food("白糖", 10.0)
        assertEquals(9.99, sugar.sugarG, 0.01)
        assertEquals(sugar.sugarG, sugar.addedSugarG!!, 0.01)
        assertEquals(SugarCoverage.COMPLETE, sugar.sugarCoverage)
    }

    @Test
    fun displayShowsTotalAndAddedSugarSeparately() {
        val snapshot = DailyNutrition.total(listOf(food("橘子", 150.0), food("全脂牛奶", 250.0)))
        val rows = NutritionValues.rows(snapshot)

        assertEquals("总糖", rows.first { it.label == "总糖" }.label)
        assertEquals("26.0 g", rows.first { it.label == "总糖" }.value)
        assertEquals("0.0 g", rows.first { it.label == "添加糖" }.value)
    }

    @Test
    fun knownAddedSugarAboveTheTargetCreatesAnAddedSugarWarning() {
        val total = DailyNutrition.total(listOf(food("白糖", 60.0)))

        assertEquals(SugarCoverage.COMPLETE, total.sugarCoverage)
        assertTrue(TodayFocusRules.items(total, targets).any {
            it.label == "添加糖偏高" && it.alert
        })
    }

    @Test
    fun unknownAddedSugarIsHiddenInsteadOfZero() {
        val dish = food("番茄炒蛋", 200.0)
        val rows = NutritionValues.rows(dish)

        assertFalse(rows.any { it.label == "添加糖" })
    }

    @Test
    fun mixedFruitMilkAndSugarTotalsOnlyWhenEveryEntryIsKnown() {
        val fruitMilk = DailyNutrition.total(listOf(food("橘子", 150.0), food("全脂牛奶", 250.0)))
        assertEquals(26.0, fruitMilk.sugarG, 0.01)
        assertEquals(0.0, fruitMilk.addedSugarG!!, 0.01)

        val withSugar = DailyNutrition.total(listOf(food("橘子", 150.0), food("白糖", 10.0)))
        assertEquals(13.5 + 9.99, withSugar.sugarG, 0.01)
        assertEquals(9.99, withSugar.addedSugarG!!, 0.01)
    }

    @Test
    fun backupSchemaFourPreservesKnownAndUnknownAddedSugar() {
        val orange = EntryFoodCatalog.items.first { it.name == "橘子" }
        val dish = EntryFoodCatalog.items.first { it.name == "番茄炒蛋" }
        val decoded = DataCodec.decode(DataCodec.exportState(PersistedState(customFoods = listOf(orange, dish))))

        assertEquals(0.0, decoded.customFoods.first { it.name == "橘子" }.nutrients.addedSugarG!!, 0.01)
        assertNull(decoded.customFoods.first { it.name == "番茄炒蛋" }.nutrients.addedSugarG)
    }

    @Test
    fun legacySchemaThreeBackupIsMigratedToSchemaFour() {
        val orange = EntryFoodCatalog.items.first { it.name == "橘子" }.let {
            it.copy(nutrients = it.nutrients.copy(addedSugarG = null))
        }
        val json = DataCodec.exportState(
            PersistedState(customFoods = listOf(orange)),
        ).replace("\"schemaVersion\":4", "\"schemaVersion\":3")

        assertTrue(DataCodec.isValidBackup(json))
        val restored = DataCodec.decode(json).customFoods.first()
        assertEquals(0.0, restored.nutrients.addedSugarG!!, 0.01)
    }

    @Test
    fun legacyFoodWithoutAddedSugarGetsKnownWholeFoodExceptionWithoutInventingDishSugar() {
        val orange = EntryFoodCatalog.items.first { it.name == "橘子" }.let {
            it.copy(nutrients = it.nutrients.copy(addedSugarG = null))
        }
        val dish = EntryFoodCatalog.items.first { it.name == "番茄炒蛋" }.let {
            it.copy(nutrients = it.nutrients.copy(addedSugarG = null))
        }
        val migrated = DataCodec.migrateLegacyFoods(listOf(orange, dish))

        assertEquals(0.0, migrated.first { it.name == "橘子" }.nutrients.addedSugarG!!, 0.01)
        assertNull(migrated.first { it.name == "番茄炒蛋" }.nutrients.addedSugarG)
    }

    private fun food(name: String, grams: Double): NutritionSnapshot {
        val item = EntryFoodCatalog.items.first { it.name == name }
        return NutritionEngine.calculateForRecord(item, grams)
    }
}
