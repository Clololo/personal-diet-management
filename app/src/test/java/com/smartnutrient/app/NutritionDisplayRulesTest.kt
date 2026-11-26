package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionDisplayRulesTest {
    private val sample = NutritionSnapshot(
        caloriesKcal = 321.0,
        proteinG = 12.3,
        fatG = 8.4,
        saturatedFatG = 2.5,
        carbohydrateG = 45.6,
        fiberG = 3.7,
        sugarG = 6.8,
        sodiumMg = 789.0,
        potassiumMg = 432.0,
        cholesterolMg = 54.0,
        addedSugarG = 1.2,
    )

    @Test
    fun valueOnlyRowsShowTotalAndAddedSugarInStableOrder() {
        val rows = NutritionValues.rows(sample)

        assertEquals(
            listOf("热量", "蛋白质", "脂肪", "饱和脂肪", "碳水化合物", "膳食纤维", "总糖", "添加糖", "钠", "钾", "胆固醇"),
            rows.map { it.label },
        )
        assertEquals(
            listOf("321 kcal", "12.3 g", "8.4 g", "2.5 g", "45.6 g", "3.7 g", "6.8 g", "1.2 g", "789 mg", "432 mg", "54 mg"),
            rows.map { it.value },
        )
    }

    @Test
    fun valueOnlyRowsNeverExposeRecommendationRanges() {
        val rendered = NutritionValues.rows(sample).joinToString(" ") { "${it.label} ${it.value}" }

        assertFalse(rendered.contains("推荐"))
        assertFalse(rendered.contains("目标"))
        assertFalse(rendered.contains("至少"))
        assertFalse(rendered.contains("不超过"))
    }

    @Test
    fun unknownAddedSugarIsHiddenInsteadOfShownAsInsufficient() {
        val targets = NutritionEngine.displayTargets(EnergyTarget(2000.0, 1800.0, 2200.0), 2000.0, Sex.FEMALE, 30)
        val unknown = NutritionSnapshot(
            300.0, 10.0, 9.0, 2.0, 40.0, 2.0, 7.0, 500.0, 300.0, 20.0, null,
        )

        assertFalse(NutritionValues.rows(unknown).any { it.label == "添加糖" })
        assertEquals(50.0, targets.sugar.max, 0.01)
        assertEquals("添加糖", targets.sugar.label)
    }

    @Test
    fun entryCanOnlyBeSubmittedForAPositiveWeight() {
        assertFalse(EntryFormRules.canSubmit(""))
        assertFalse(EntryFormRules.canSubmit("0"))
        assertFalse(EntryFormRules.canSubmit("0.0"))
        assertFalse(EntryFormRules.canSubmit("not-a-number"))
        assertTrue(EntryFormRules.canSubmit("150"))
        assertTrue(EntryFormRules.canSubmit("12.5"))
    }
}
