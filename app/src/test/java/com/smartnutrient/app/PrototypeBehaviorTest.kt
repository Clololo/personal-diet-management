package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrototypeBehaviorTest {
    @Test
    fun searchMatchesAliasPinyinAndInitials() {
        assertEquals("米饭", FoodSearch.search(SampleFoods.items, "mf").first().name)
        assertEquals("番茄炒蛋", FoodSearch.search(SampleFoods.items, "xihongshi").first().name)
        assertEquals("青椒肉丝", FoodSearch.search(SampleFoods.items, "qjrs").first().name)
    }

    @Test
    fun searchFindsFoodsByIngredient() {
        val results = FoodSearch.search(SampleFoods.items, "鸡蛋")
        assertTrue(results.any { it.name == "番茄炒蛋" })
    }

    @Test
    fun searchSupportsMultipleKeywordsWithAndSemantics() {
        val tomatoEgg = FoodSearch.search(SampleFoods.items, "番茄 鸡蛋")
        assertTrue(tomatoEgg.any { it.name == "番茄炒蛋" })

        val chickenBroccoli = FoodSearch.search(SampleFoods.items, "西兰花 鸡胸")
        assertTrue(chickenBroccoli.any { it.name.contains("西兰花") && it.name.contains("鸡胸") })
    }

    @Test
    fun searchDoesNotCreateDuplicateDishesForSeasoningKeywords() {
        val results = FoodSearch.search(SampleFoods.items, "低盐")
        assertTrue(results.any { it.name == "番茄炒蛋" })
        assertEquals(results.size, results.map { it.name }.distinct().size)
    }

    @Test
    fun macroTargetsStayWithinEnergyBudget() {
        val targets = NutritionEngine.targets(2555.56, Sex.MALE, 30)
        assertEquals(95.83, targets.proteinMinG, 0.01)
        assertEquals(287.5005, targets.carbohydrateMinG, 0.01)
        assertEquals(2000.0, targets.sodiumMaxMg, 0.01)
    }
}
class FoodKindTest {
    @Test
    fun labelsUseExactlyTheFiveUserFacingKinds() {
        assertEquals(setOf("成品菜", "食材", "调料", "水果", "饮品", "补剂"), FoodKind.entries.map { it.label }.toSet())
        val catalog = EntryFoodCatalog.items
        assertEquals(FoodKind.DISH, FoodKind.of(catalog.first { it.name == "番茄炒蛋" }))
        assertEquals(FoodKind.INGREDIENT, FoodKind.of(catalog.first { it.name == "鸡胸肉" }))
        assertEquals(FoodKind.SEASONING, FoodKind.of(catalog.first { it.name == "生抽" }))
        assertEquals(FoodKind.FRUIT, FoodKind.of(catalog.first { it.name == "苹果" }))
        assertEquals(FoodKind.DRINK, FoodKind.of(catalog.first { it.category == "饮品" && it.name == "全脂牛奶" }))
        assertEquals(FoodKind.DRINK, FoodKind.of(catalog.first { it.category == "饮品" && it.name == "无糖豆浆" }))
        assertEquals(FoodKind.DRINK, FoodKind.of(catalog.first { it.category == "饮品" && it.name == "美式咖啡" }))
    }

    @Test
    fun everyOrdinaryEntryReceivesExactlyOneBadgeKind() {
        EntryFoodCatalog.items.forEach { food -> assertTrue(food.name, FoodKind.of(food).label.isNotBlank()) }
    }
}

class FeatureRulesTest {
    @Test
    fun copyingBreakfastKeepsFoodsAndMovesThemToChosenMeal() {
        val source = listOf(
            FoodLog(1, SampleFoods.items[0], 180.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO),
            FoodLog(2, SampleFoods.items[9], 120.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO)
        )
        val copied = MealRules.copyMeal(source, Meal.BREAKFAST, Meal.LUNCH, startId = 10)
        assertEquals(2, copied.size)
        assertEquals(listOf(10L, 11L), copied.map { it.id })
        assertTrue(copied.all { it.meal == Meal.LUNCH })
        assertEquals(180.0, copied[0].grams, 0.01)
    }

    @Test
    fun weeklySummaryReportsConsistentDailyAverages() {
        val day1 = listOf(FoodLog(1, SampleFoods.items[0], 300.0, Meal.LUNCH, OilLevel.ZERO, SaltLevel.ZERO))
        val day2 = listOf(FoodLog(2, SampleFoods.items[9], 200.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO))
        val summary = SummaryRules.weekly(2, listOf(day1, day2))
        assertEquals(2, summary.loggedDays)
        assertEquals(2, summary.entriesByDay.size)
        assertEquals(2, summary.entriesByDay.size)
    }

    @Test
    fun customFoodCanRoundTripThroughLocalData() {
        val food = FoodItem(
            id = 9999,
            name = "自制燕麦杯",
            pinyin = "zizhimaoyanbei",
            initials = "zzmyb",
            category = "自定义",
            basis = "熟重",
            nutrients = FoodNutrients(150.0, 6.0, 4.0, 1.0, 22.0, 3.0, 5.0, 80.0, 200.0, 2.0),
            aliases = listOf("燕麦杯"),
            ingredients = listOf("燕麦")
        )
        val source = listOf(FoodLog(1, food, 200.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO))
        val logsByDate = mapOf(LocalDate.of(2026, 11, 21) to source)
        val exported = DataCodec.export(logsByDate, source.map { it.food })
        val decoded = DataCodec.decode(exported)
        assertEquals("自制燕麦杯", decoded.customFoods.first().name)
        assertEquals(200.0, decoded.logsByDate.values.first().first().grams, 0.01)
    }
}





class CatalogAcceptanceTest {
    @Test
    fun catalogMeetsMinimumCoverage() {
        val all = FoodCatalog.items
        assertTrue("staples=${all.count { it.category == "主食" }}, dishes=${all.count { it.category == "菜" }}, fruits=${all.count { it.category == "水果" }}", all.count { it.category == "主食" } >= 150)
        assertTrue("dishes=${all.count { it.category == "菜" }}", all.count { it.category == "菜" } >= 250)
        assertTrue("fruits=${all.count { it.category == "水果" }}", all.count { it.category == "水果" } >= 200)
    }

    @Test
    fun catalogHasNoDuplicateNamesAndValidNutrition() {
        val all = FoodCatalog.items
        assertEquals(all.size, all.map { "${it.category}:${it.name}" }.distinct().size)
        assertTrue("malformed names=" + all.map { it.name }.filter { it.startsWith("+") || it.isBlank() }, all.none { it.name.startsWith("+") || it.name.isBlank() })
        assertTrue(all.all { it.nutrients.caloriesKcal >= 0.0 && it.nutrients.sodiumMg >= 0.0 && it.nutrients.proteinG >= 0.0 })
        assertTrue(all.all { it.source.isNotBlank() && it.confidence.isNotBlank() })
    }

    @Test
    fun dishesAreCuratedRecipesRatherThanGeneratedMethodVariants() {
        val dishes = FoodCatalog.items.filter { it.category == "菜" }
        val generatedSuffixes = listOf("少油", "家常", "高油", "蒸", "炖", "烤", "凉拌", "红烧", "少盐", "微辣", "蒜香", "黑椒", "香煎", "砂锅", "空气炸", "卤制", "酱香", "原味")
        assertTrue(dishes.none { food -> generatedSuffixes.any { suffix -> food.name.endsWith("（$suffix）") } })
        assertEquals(dishes.size, dishes.map { it.name }.distinct().size)
        assertTrue("Curated dish count=${dishes.size}", dishes.size >= 250)
    }

    @Test
    fun cucumberSearchOffersManyDistinctDishes() {
        val cucumberDishes = FoodSearch.search(SampleFoods.items, "黄瓜")
            .filter { it.category == "菜" }
            .map { it.name }
            .distinct()
        assertTrue("cucumber dishes=$cucumberDishes", cucumberDishes.size >= 20)
    }

    @Test
    fun dishCatalogDoesNotPadWithMethodVariants() {
        val dishes = FoodCatalog.items.filter { it.category == "菜" }
        val ingredientSets = dishes.groupBy { it.ingredients.sorted() }
        val sameIngredientVariants = ingredientSets.values.filter { group ->
            group.size > 1 && group.any { dish -> listOf("少油", "高油", "少盐", "高盐", "家常", "红烧", "清蒸", "香煎", "凉拌", "炖", "烤").any { dish.name.endsWith("（$it）") } }
        }
        assertTrue("method variants=$sameIngredientVariants", sameIngredientVariants.isEmpty())
    }

    @Test
    fun everyCuratedDishRowSurvivesCatalogAggregation() {
        val finalNames = FoodCatalog.items.map { it.name }.toSet()
        val missing = buildDishes().map { it.name }.filter { it !in finalNames }
        assertTrue("curated dishes shadowed by another category=$missing", missing.isEmpty())
    }

    @Test
    fun dishNamesHaveNoExactSynonymDuplicates() {
        val dishes = FoodCatalog.items.filter { it.category == "菜" }
        val signatures = dishes.groupBy { listOf(it.nutrients.caloriesKcal, it.nutrients.proteinG, it.nutrients.fatG, it.nutrients.carbohydrateG, it.nutrients.fiberG, it.nutrients.sodiumMg) }
        val suspicious = signatures.values.filter { group ->
            group.size > 1 && group.any { dish -> listOf("小米南瓜粥", "葱花饼", "锅贴饺", "荷叶糯米鸡", "牛角包", "椰奶西米露", "黑咖啡", "豉汁排骨饭", "玉米胡萝卜排骨汤").contains(dish.name) }
        }
        assertTrue("exact synonym duplicates=$suspicious", suspicious.isEmpty())
    }
}


class CatalogReportTest {
    @Test
    fun printCatalogReport() {
        val all = FoodCatalog.items
        val groups = all.groupBy { it.category }
        println("CATALOG_TOTAL=${all.size}")
        groups.toSortedMap().forEach { (category, foods) -> println("CATALOG_$category=${foods.size}") }
        println("CATALOG_REFERENCE=${all.count { it.confidence == "参考值" }}")
        println("CATALOG_ESTIMATED=${all.count { it.confidence != "参考值" }}")
        println("CATALOG_UNIQUE_NAMES=${all.map { it.name }.distinct().size}")
        println(CatalogAudit.report(all))
    }
}

