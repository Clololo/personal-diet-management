package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarImportTest {
    private val date = LocalDate.of(2026, 11, 21)
    private val rice = SampleFoods.items.first { it.name == "米饭" }
    private val egg = SampleFoods.items.first { it.name == "番茄炒蛋" }

    @Test
    fun mergeKeepsExistingDaysAndImportedDays() {
        val current = PersistedState(
            days = mapOf(date to DayLog(date, listOf(FoodLog(1, rice, 150.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO)), 500)),
        )
        val incomingDate = date.minusDays(1)
        val incoming = PersistedState(
            days = mapOf(incomingDate to DayLog(incomingDate, listOf(FoodLog(9, egg, 200.0, Meal.DINNER, OilLevel.LOW, SaltLevel.LOW)), 750)),
        )
        val merged = StateMergeRules.merge(current, incoming)
        assertEquals(2, merged.days.size)
        assertEquals("米饭", merged.days.getValue(date).entries.single().food.name)
        assertEquals("番茄炒蛋", merged.days.getValue(incomingDate).entries.single().food.name)
        assertEquals(750, merged.days.getValue(incomingDate).waterMl)
    }

    @Test
    fun sameDayImportAppendsOnlyNewRecordsAndIsIdempotent() {
        val existingLog = FoodLog(1, rice, 150.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO)
        val incomingLog = FoodLog(9, egg, 200.0, Meal.DINNER, OilLevel.LOW, SaltLevel.LOW)
        val current = PersistedState(days = mapOf(date to DayLog(date, listOf(existingLog), 500)))
        val incoming = PersistedState(days = mapOf(date to DayLog(date, listOf(incomingLog), 1000)))
        val first = StateMergeRules.merge(current, incoming)
        val second = StateMergeRules.merge(first, incoming)
        assertEquals(2, first.days.getValue(date).entries.size)
        assertEquals(2, second.days.getValue(date).entries.size)
        assertEquals(1000, second.days.getValue(date).waterMl)
        assertEquals(2, second.days.getValue(date).entries.map { it.id }.distinct().size)
    }

    @Test
    fun mergePreservesCurrentProfileAndUnionsSearchMetadata() {
        val current = PersistedState(
            profile = BodyProfile(41, Sex.FEMALE, 165.0, 58.0),
            favorites = setOf(1),
            recentFoodIds = listOf(1),
        )
        val incoming = PersistedState(
            profile = BodyProfile(25, Sex.MALE, 180.0, 80.0),
            favorites = setOf(2),
            recentFoodIds = listOf(2),
        )
        val merged = StateMergeRules.merge(current, incoming)
        assertEquals(current.profile, merged.profile)
        assertEquals(setOf(1, 2), merged.favorites)
        assertEquals(listOf(2, 1), merged.recentFoodIds.take(2).toSet().toList().sortedDescending())
    }

    @Test
    fun mergeRemapsImportedCustomFoodCollision() {
        val currentFood = FoodItem(100_001, "当前自制菜", "", "", "自定义", "每100g", FoodNutrients(100.0, 5.0, 2.0, 0.5, 10.0, 1.0, 1.0, 100.0, 100.0, 0.0))
        val incomingFood = FoodItem(100_001, "导入自制菜", "", "", "自定义", "每100g", FoodNutrients(120.0, 6.0, 3.0, 0.6, 12.0, 2.0, 1.0, 150.0, 120.0, 0.0))
        val merged = StateMergeRules.merge(
            PersistedState(customFoods = listOf(currentFood), favorites = setOf(currentFood.id)),
            PersistedState(customFoods = listOf(incomingFood), favorites = setOf(incomingFood.id)),
        )
        assertEquals(2, merged.customFoods.size)
        assertTrue(merged.customFoods.map { it.id }.distinct().size == 2)
        assertTrue(merged.favorites.size == 2)
    }
}
