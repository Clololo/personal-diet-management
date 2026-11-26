package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AuditSimulationTest {
    @Test
    fun seriousCatalogNutritionOutliersAreExcluded() {
        val issues = CatalogAudit.issues(SampleFoods.items)
        assertTrue("nutrition audit issues: $issues", issues.isEmpty())
        assertTrue("invalid basis: ${CatalogAudit.invalidServingBasis(SampleFoods.items)}", CatalogAudit.invalidServingBasis(SampleFoods.items).isEmpty())
    }

    @Test
    fun hotDryNoodlesUseAWholeServingAndDoNotHaveADryWeightVariant() {
        val foods = SampleFoods.items.filter { it.name.startsWith("热干面") }
        assertTrue(foods.any { it.name == "热干面" })
        assertTrue(foods.none { it.name.contains("干重") })
        val hotDry = foods.first { it.name == "热干面" }
        assertEquals("每份", hotDry.basis)
        assertTrue(hotDry.aliases.any { it == "每份200g" })
        assertTrue("hot dry kcal=${hotDry.nutrients.caloriesKcal}", hotDry.nutrients.caloriesKcal in 320.0..450.0)
        assertTrue(hotDry.nutrients.sodiumMg in 700.0..1500.0)
        val oneServing = NutritionEngine.calculateForRecord(hotDry, 200.0)
        assertEquals(368.0, oneServing.caloriesKcal, 0.01)
        assertEquals(1060.0, oneServing.sodiumMg, 0.01)
    }

    @Test
    fun preparedCompositeStaplesDoNotPretendToHaveDryWeightVariants() {
        val forbidden = listOf("菜包","肉包","豆沙包","叉烧包","流沙包","奶黄包","烧麦","蒸饺","水饺","馄饨","煎饺")
        forbidden.forEach { name ->
            assertTrue(name, SampleFoods.items.none { it.name == "$name（干重）" })
        }
    }

    @Test
    fun perServingFoodIsCalculatedFromItsDeclaredServingBasis() {
        val servingFood = CatalogSpec("整份测试面","zhengfenceshimian","zfcs m","主食","每份",200.0,8.0,6.0,1.0,30.0,2.0,1.0,600.0,100.0,0.0,emptyList(),emptyList(),"拌","测试","估算").toFoodForTest(99)
        val amount = NutritionEngine.calculateForRecord(servingFood, 250.0)
        assertEquals(250.0, amount.caloriesKcal, 0.01)
        assertEquals(750.0, amount.sodiumMg, 0.01)
    }

    @Test
    fun emptyObjectIsNotAcceptedAsABackup() {
        assertFalse(DataCodec.isValidBackup("{}"))
    }

    @Test
    fun customFoodIdAllocationAvoidsExistingIds() {
        val used = SampleFoods.items.map { it.id }.toSet()
        val first = FoodIdRules.nextCustomId("同一名称", 100.0, used)
        val second = FoodIdRules.nextCustomId("同一名称", 100.0, used + first)
        assertFalse(first in used)
        assertTrue(second != first)
        assertFalse(second in used + first)
    }

    @Test
    fun addEntryUsesRequestedMealUnlessEditingExistingEntry() {
        val lunch = FoodLog(1, SampleFoods.items.first(), 100.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO)
        assertEquals(Meal.DINNER, EntryDefaults.meal(null, Meal.DINNER))
        assertEquals(Meal.LUNCH, EntryDefaults.meal(lunch, Meal.DINNER))
        assertEquals(Meal.BREAKFAST, EntryDefaults.meal(null, null))
    }

    @Test
    fun monthNavigationPreservesDayOrClampsToMonthEnd() {
        assertEquals(LocalDate.of(2026, 10, 30), CalendarRules.previousMonth(LocalDate.of(2026, 11, 30)))
        assertEquals(LocalDate.of(2026, 12, 30), CalendarRules.nextMonth(LocalDate.of(2026, 11, 30)))
        assertEquals(LocalDate.of(2027, 1, 31), CalendarRules.nextMonth(LocalDate.of(2026, 12, 31)))
        assertEquals(LocalDate.of(2026, 2, 28), CalendarRules.nextMonth(LocalDate.of(2026, 1, 31)))
    }

    @Test
    fun backupKeepsProfileWaterAndFoodMetadata() {
        val date = LocalDate.of(2026, 11, 21)
        val profile = BodyProfile(age = 37, sex = Sex.FEMALE, heightCm = 164.0, weightKg = 57.5, bodyType = BodyType.ATHLETIC, waistCm = 71.0, bodyFatPercent = 24.5, activity = ActivityLevel.ACTIVE, activityKcal = 650.0, goal = WeightGoal.LOSE)
        val food = CatalogSpec("审核菜", "shenhecai", "shc", "菜", "熟重", 120.0, 7.0, 5.0, 1.0, 9.0, 2.0, 1.0, 330.0, 210.0, 20.0, emptyList(), listOf("审核食材"), "蒸", "审核来源", "估算").toFoodForTest(999001)
        val state = PersistedState(
            days = mapOf(date to DayLog(date, listOf(FoodLog(1, food, 200.0, Meal.DINNER, OilLevel.MEDIUM, SaltLevel.LOW)), 750)),
            profile = profile,
            customFoods = listOf(food),
            favorites = setOf(999001)
        )
        val restored = DataCodec.decode(DataCodec.exportState(state))
        assertEquals(profile, restored.profile)
        assertEquals(750, restored.waterByDate[date])
        assertEquals("审核来源", restored.customFoods.first().source)
        assertEquals("蒸", restored.customFoods.first().method)
        assertEquals(BodyType.ATHLETIC, restored.profile.bodyType)
        assertEquals(650.0, restored.profile.activityKcal!!, 0.01)
        assertEquals(1, restored.favorites.size)
    }

    @Test
    fun weeklySummaryAveragesAllTenNutrients() {
        val food = SampleFoods.items.first { it.name == "番茄炒蛋" }
        val logs = listOf(FoodLog(1, food, 200.0, Meal.LUNCH, OilLevel.MEDIUM, SaltLevel.LOW))
        val summary = SummaryRules.weekly(1, listOf(logs))
        val expected = NutritionEngine.recordSnapshot(food, 200.0, OilLevel.MEDIUM, SaltLevel.LOW)
        assertEquals(expected.fatG, summary.averageFatG, 0.01)
        assertEquals(expected.saturatedFatG, summary.averageSaturatedFatG, 0.01)
        assertEquals(expected.carbohydrateG, summary.averageCarbohydrateG, 0.01)
        assertEquals(expected.sugarG, summary.averageSugarG, 0.01)
        assertEquals(expected.potassiumMg, summary.averagePotassiumMg, 0.01)
        assertEquals(expected.cholesterolMg, summary.averageCholesterolMg, 0.01)
        assertEquals(expected, summary.average)
    }

    fun weeklySummaryIncludesOilAndSalt() {
        val food = SampleFoods.items.first { it.name == "番茄炒蛋" }
        val logs = listOf(FoodLog(1, food, 200.0, Meal.LUNCH, OilLevel.HIGH, SaltLevel.HIGH))
        val summary = SummaryRules.weekly(1, listOf(logs))
        val expected = NutritionEngine.recordSnapshot(food, 200.0, OilLevel.HIGH, SaltLevel.HIGH)
        assertEquals(expected.caloriesKcal, summary.averageCalories, 0.01)
        assertEquals(expected.sodiumMg, summary.averageSodiumMg, 0.01)
    }

    @Test
    fun weeklySummaryUsesTheSamePerServingBasisAsTodayScreen() {
        val hotDry = SampleFoods.items.first { it.name == "热干面" }
        val logs = listOf(FoodLog(1, hotDry, 200.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO))
        val summary = SummaryRules.weekly(1, listOf(logs))
        val today = NutritionEngine.calculateForRecord(hotDry, 200.0)
        assertEquals(today.caloriesKcal, summary.averageCalories, 0.01)
        assertEquals(today.sodiumMg, summary.averageSodiumMg, 0.01)
    }

    @Test
    fun copyMealRejectsSameMealAndCopiesToRequestedMeal() {
        val food = SampleFoods.items.first { it.name == "米饭" }
        val source = listOf(FoodLog(1, food, 100.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO))
        assertTrue(MealRules.copyMeal(source, Meal.BREAKFAST, Meal.BREAKFAST, 10).isEmpty())
        val dinner = MealRules.copyMeal(source, Meal.BREAKFAST, Meal.DINNER, 10)
        assertEquals(1, dinner.size)
        assertEquals(Meal.DINNER, dinner.first().meal)
    }

    @Test
    fun everyCopyDestinationIsAnotherMeal() {
        Meal.entries.forEach { source ->
            val destinations = Meal.entries.filter { it != source }
            assertEquals(3, destinations.size)
            assertTrue(destinations.none { it == source })
        }
    }

    @Test
    fun invalidBackupValuesCannotBeImported() {
        val valid = DataCodec.exportState(PersistedState())
        assertFalse(DataCodec.isValidBackup(valid.replace("\"format\":\"smartnutrient\"", "\"format\":\"other\"")))
        assertFalse(DataCodec.isValidBackup(valid.replace("\"heightCm\":175.0", "\"heightCm\":-2.0")))
    }

    @Test
    fun backupRejectsInvalidEnumsAndInvalidVersion() {
        assertFalse(DataCodec.isValidBackup("{\"schemaVersion\":1,\"days\":[{\"date\":\"2026-11-21\",\"entries\":[{\"grams\":100,\"meal\":\"BOGUS\",\"oil\":\"ZERO\",\"salt\":\"ZERO\"}]}]}"))
        assertFalse(DataCodec.isValidBackup("{\"schemaVersion\":99}"))
    }

    @Test
    fun favoriteSearchResultsComeFirst() {
        val food = SampleFoods.items.first { it.name == "米饭" }
        val results = FoodSearch.search(SampleFoods.items, "米饭", favorites = setOf(food.id))
        assertEquals(food.id, results.first().id)
    }

    @Test
    fun malformedOrEmptyQueriesDoNotCrash() {
        assertTrue(FoodSearch.search(SampleFoods.items, "").isNotEmpty())
        assertTrue(FoodSearch.search(SampleFoods.items, "   ").isNotEmpty())
        assertTrue(FoodSearch.search(emptyList(), "米饭").isEmpty())
        assertTrue(FoodSearch.search(SampleFoods.items, "不存在的食物xyz").isEmpty())
    }

    @Test
    fun waterStateUsesBoundedIncrementRules() {
        var water = 0
        repeat(30) { water = (water + 250).coerceAtMost(5000) }
        assertEquals(5000, water)
    }

    @Test
    fun summaryOfEmptyWeekHasSafeValues() {
        val summary = SummaryRules.weekly(0, List(7) { emptyList() })
        assertEquals(0, summary.loggedDays)
        assertEquals(0.0, summary.averageCalories, 0.01)
        assertEquals(0.0, summary.averageSodiumMg, 0.01)
    }

    @Test
    fun legacyV1BackupWithFoodIdCanBeMigrated() {
        val legacy = "{\"schemaVersion\":1,\"days\":[{\"date\":\"2026-11-21\",\"entries\":[{\"id\":1,\"grams\":150,\"meal\":\"LUNCH\",\"oil\":\"LOW\",\"salt\":\"LOW\",\"foodId\":1}]}]}"
        assertTrue(DataCodec.isValidBackup(legacy))
        val decoded = DataCodec.decode(legacy)
        assertEquals("米饭", decoded.logsByDate[LocalDate.of(2026, 11, 21)]!!.first().food.name)
    }

    @Test
    fun customFoodRoundTripKeepsSearchFieldsAndSource() {
        val food = FoodItem(1001, "自制蛋白饼", "zizhidanbaibing", "zzdb", "自定义", "每100g", FoodNutrients(180.0, 20.0, 5.0, 1.0, 15.0, 2.0, 1.0, 200.0, 300.0, 20.0), listOf("蛋白饼"), listOf("蛋白粉"), "煎", "用户输入", "估算")
        val state = PersistedState(customFoods = listOf(food))
        val restored = DataCodec.decode(DataCodec.exportState(state)).customFoods.first()
        assertEquals(food, restored)
    }
}

private fun CatalogSpec.toFoodForTest(id: Int) = FoodItem(
    id, name, pinyin, initials, category, basis,
    FoodNutrients(kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, cholesterol),
    aliases, ingredients, method, source, confidence
)

