package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AutoBackupRulesTest {
    @Test
    fun destructiveRestoreRequiresSnapshotOfCurrentState() {
        val current = PersistedState(
            days = mapOf(LocalDate.of(2026, 11, 22) to DayLog(LocalDate.of(2026, 11, 22), listOf(entry(1)))),
            profile = BodyProfile(36, Sex.FEMALE, 165.0, 59.0),
        )

        val snapshot = AutoBackupRules.snapshotBeforeRestore(current)

        assertNotNull(snapshot)
        assertTrue(snapshot!!.json.contains("smartnutrient"))
        assertEquals(1, snapshot.dayCount)
        assertEquals(1, snapshot.entryCount)
    }

    @Test
    fun noSnapshotIsNeededForCompletelyEmptyState() {
        assertNull(AutoBackupRules.snapshotBeforeRestore(PersistedState()))
    }

    @Test
    fun backupFileNameIsStableReadableAndSafe() {
        val name = AutoBackupRules.fileName(java.time.LocalDateTime.of(2026, 11, 22, 14, 5, 9))

        assertEquals("SmartNutrient-备份-20261122-140509.json", name)
        assertTrue(name.none { it in "/\\:*?\"<>|" })
    }

    private fun entry(id: Long): FoodLog {
        val food = EntryFoodCatalog.items.first { it.name == "米饭" }
        return FoodLog(id, food, 100.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO)
    }
}
