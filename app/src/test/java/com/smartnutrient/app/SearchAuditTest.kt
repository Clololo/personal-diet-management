package com.smartnutrient.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAuditTest {
    @Test
    fun ordinaryEntrySearchReturnsIngredientsAndSeasoningsDirectly() {
        val all = EntryFoodCatalog.items
        val soy = FoodSearch.search(all, "黄豆")
        assertTrue("黄豆 results=${soy.map { "${it.category}:${it.name}" }}", soy.any { it.name == "黄豆" && it.category == "食材" })
        assertTrue(FoodSearch.search(all, "生抽").any { it.name == "生抽" && it.category == "调料" })
        assertTrue(FoodSearch.search(all, "鹰嘴豆").any { it.name == "鹰嘴豆" && it.category == "食材" })
        assertTrue(FoodSearch.search(all, "鸡胸").any { it.name == "鸡胸肉" && it.category == "食材" })
    }

    @Test
    fun basicMealFoodsAreDirectlySearchable() {
        listOf("鸡蛋", "牛奶", "豆浆").forEach { query ->
            val names = FoodSearch.search(SampleFoods.items, query).take(6).map { it.name }
            assertTrue("query=$query results=$names", when (query) {
                "鸡蛋" -> names.contains("鸡蛋")
                "牛奶" -> names.contains("全脂牛奶")
                "豆浆" -> names.contains("无糖豆浆")
                else -> false
            })
        }
    }

    @Test
    fun milkSoyMilkAndEggsAlsoHaveMealDishes() {
        assertTrue(FoodSearch.search(SampleFoods.items, "牛奶").any { it.name == "牛奶燕麦粥" })
        assertTrue(FoodSearch.search(SampleFoods.items, "豆浆").any { it.name == "豆浆燕麦粥" })
        assertTrue(FoodSearch.search(SampleFoods.items, "鸡蛋").any { it.category == "菜" })
    }

    @Test
    fun commonIngredientsReturnMultipleDistinctRealDishes() {
        listOf("黄瓜", "番茄", "鸡蛋", "豆腐", "土豆", "牛肉", "鸡肉", "虾仁", "香菇", "米饭", "面条").forEach { query ->
            val names = FoodSearch.search(SampleFoods.items, query)
                .filter { it.category == "菜" }
                .map { it.name }
                .distinct()
            assertTrue("query=$query names=$names", names.size >= 5)
        }
    }
    @Test
    fun multiKeywordAndMethodSynonymsReturnRelevantFoods() {
        val tomatoEgg = FoodSearch.search(SampleFoods.items, "番茄 鸡蛋")
        assertTrue(tomatoEgg.any { it.name == "番茄炒蛋" })

        val chickenBroccoli = FoodSearch.search(SampleFoods.items, "西兰花 鸡胸")
        assertTrue(chickenBroccoli.isNotEmpty())
        assertTrue(chickenBroccoli.all { it.name.contains("西兰花") && it.name.contains("鸡胸") })

        val rice = FoodSearch.search(SampleFoods.items, "米饭")
        assertEquals("米饭", rice.first().name)
        assertTrue(rice.any { it.category == "主食" })

        val lowSalt = FoodSearch.search(SampleFoods.items, "低盐")
        assertTrue(lowSalt.isNotEmpty())
        assertTrue(lowSalt.any { it.name == "番茄炒蛋" })
        assertEquals(lowSalt.size, lowSalt.map { it.name }.distinct().size)
    }

    @Test
    fun printSearchAudit() {
        listOf("番茄 鸡蛋", "西兰花 鸡胸", "低盐", "低油", "米饭", "mf", "xg").forEach { query ->
            println("QUERY[$query] -> ${FoodSearch.search(SampleFoods.items, query).take(6).map { it.name }}")
        }
    }

    @Test
    fun multiKeywordSearchCoversCommonMealScenario() {
        val queries = listOf(
            "番茄 鸡蛋 少油" to "番茄炒蛋",
            "西兰花 鸡胸" to "西兰花炒鸡胸",
            "米饭 干重" to "米饭（干重）",
            "熟米饭" to "米饭",
            "全麦 意面 熟" to "全麦意面（熟）",
            "低盐 鸡胸" to "鸡胸",
            "苹果 红富士" to "红富士苹果"
        )
        queries.forEach { (query, expectedName) ->
            val names = FoodSearch.search(SampleFoods.items, query).map { it.name }
            println("MATRIX_QUERY[$query]=$names")
            assertTrue("query=$query expected=$expectedName results=$names", names.any { it.contains(expectedName) })
        }
        val xg = FoodSearch.search(SampleFoods.items, "xg").map { it.name }
        println("INITIAL_QUERY[xg]=$xg")
        assertTrue("xg results=$xg", xg.any { it == "香瓜" })
    }

    @Test
    fun commonStandaloneIngredientsRankAsExactMatches() {
        val all = EntryFoodCatalog.items
        mapOf(
            "黄豆" to "黄豆",
            "鹰嘴豆" to "鹰嘴豆",
            "红腰豆" to "红腰豆（干）",
            "白芸豆" to "白芸豆（干）",
            "生抽" to "生抽",
            "大蒜" to "大蒜",
            "鸡胸肉" to "鸡胸肉"
        ).forEach { (query, expected) ->
            val first = FoodSearch.search(all, query).first()
            assertEquals("query=$query first=${first.name}", expected, first.name)
        }
    }
    @Test
    fun favoritesThenRecentsThenBestMatchesAreStable() {
        val all = SampleFoods.items
        val favorite = all.first { it.name == "米饭" }
        val recent = all.first { it.name == "番茄炒蛋" }
        val plain = all.first { it.name == "青椒肉丝" }
        val results = FoodSearch.search(all, "", favorites = setOf(favorite.id), recent = listOf(recent.id))
        assertEquals(favorite.id, results[0].id)
        assertEquals(recent.id, results[1].id)
        val firstOrdinary = results.indexOfFirst { it.id !in setOf(favorite.id, recent.id) }
        assertTrue(firstOrdinary >= 2)
        if (plain.id !in setOf(favorite.id, recent.id)) assertTrue(results.indexOf(plain) >= firstOrdinary)
    }
}


