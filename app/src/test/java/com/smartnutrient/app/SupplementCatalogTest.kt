package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplementCatalogTest {
    @Test
    fun catalogContainsSixtyFiveUniqueSupplements() {
        val supplements = SupplementCatalog.items

        assertEquals(65, supplements.size)
        assertEquals(supplements.size, supplements.map { it.name }.distinct().size)
        assertTrue(supplements.all { it.category == "补剂" })
        assertTrue(supplements.all { it.basis == "每100g" })
        assertTrue(supplements.all { it.nutrients.caloriesKcal >= 0.0 && it.nutrients.sodiumMg >= 0.0 })
        assertTrue(supplements.all { it.source.isNotBlank() && it.confidence.isNotBlank() })
    }

    @Test
    fun commonSupplementPowdersAreDirectlySearchable() {
        listOf("羽衣甘蓝粉", "甜菜根粉", "螺旋藻粉", "乳清蛋白粉", "肌酸一水合物").forEach { name ->
            val results = FoodSearch.search(EntryFoodCatalog.items, name)
            assertTrue("query=$name results=${results.map { it.name }}", results.any { it.name == name })
        }
    }

    @Test
    fun supplementsUseDedicatedKindAndEntryCategory() {
        val kale = EntryFoodCatalog.items.first { it.name == "羽衣甘蓝粉" }
        val beet = EntryFoodCatalog.items.first { it.name == "甜菜根粉" }

        assertEquals(FoodKind.SUPPLEMENT, FoodKind.of(kale))
        assertEquals(FoodKind.SUPPLEMENT, FoodKind.of(beet))
        assertEquals("补剂", FoodKind.SUPPLEMENT.label)
        assertTrue(FoodKind.entries.any { it == FoodKind.SUPPLEMENT })
    }

    @Test
    fun supplementPresetsUsePracticalPowderDoses() {
        val kale = SupplementCatalog.items.first { it.name == "羽衣甘蓝粉" }

        assertEquals(listOf(3.0, 5.0, 10.0, 15.0, 20.0, 30.0), ServingPresetRules.forFood(kale))
    }
}
