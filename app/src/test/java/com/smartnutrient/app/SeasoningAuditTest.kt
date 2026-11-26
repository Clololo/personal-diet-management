package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasoningAuditTest {
    @Test
    fun seasoningCatalogHasNoDuplicateIdentityOrMissingProvenance() {
        val items = SeasoningCatalog.items
        assertTrue("count=${items.size}", items.size >= 30)
        assertEquals(items.size, items.map { it.id }.distinct().size)
        assertEquals(items.size, items.map { it.name }.distinct().size)
        assertTrue(items.all { it.source.isNotBlank() && it.confidence.isNotBlank() })
    }

    @Test
    fun seasoningNutritionStaysWithinPhysicalRanges() {
        SeasoningCatalog.items.forEach { food ->
            val n = food.nutrientsPer100g
            val issues = buildList {
                if (n.caloriesKcal !in 0.0..900.0) add("kcal=${n.caloriesKcal}")
                if (n.proteinG !in 0.0..100.0) add("protein=${n.proteinG}")
                if (n.fatG !in 0.0..100.0) add("fat=${n.fatG}")
                if (n.carbohydrateG !in 0.0..100.0) add("carbs=${n.carbohydrateG}")
                if (n.sodiumMg !in 0.0..65000.0) add("sodium=${n.sodiumMg}")
                if (n.saturatedFatG > n.fatG + 0.01) add("satFat=${n.saturatedFatG}>fat=${n.fatG}")
                if (n.fiberG > n.carbohydrateG + 0.01) add("fiber=${n.fiberG}>carbs=${n.carbohydrateG}")
                if (n.sugarG > n.carbohydrateG + 0.01) add("sugar=${n.sugarG}>carbs=${n.carbohydrateG}")
            }
            assertTrue("${food.name}: $issues", issues.isEmpty())
        }
    }

    @Test
    fun energyIsPlausibleForSeasoningsExceptBalancedSaltAndGlutamateProducts() {
        SeasoningCatalog.items
            .filter { it.category !in setOf("盐", "鲜味") }
            .forEach { food ->
                val n = food.nutrientsPer100g
                val macro = n.proteinG * 4.0 + n.carbohydrateG * 4.0 + n.fatG * 9.0
                if (macro >= 20.0 && n.caloriesKcal >= 20.0) {
                    val ratio = n.caloriesKcal / macro
                    assertTrue("${food.name}: ratio=$ratio macro=$macro kcal=${n.caloriesKcal}", ratio in 0.78..1.20)
                }
            }
    }

    @Test
    fun commonSpoonsArePhysicallyPlausible() {
        SeasoningCatalog.items.forEach { food ->
            val spoons = SeasoningPortionRules.commonSpoons(food)
            assertTrue(food.name, spoons.any { it.grams == 1.0 })
            assertTrue(food.name, spoons.all { it.grams in 0.5..20.0 })
        }
    }
}
