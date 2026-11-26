package com.smartnutrient.app

import java.time.LocalDate

enum class Meal(val label: String) {
    BREAKFAST("早餐"),
    LUNCH("午餐"),
    DINNER("晚餐"),
    SNACK("加餐")
}

enum class OilLevel(val label: String, val grams: Double) {
    ZERO("不加油", 0.0),
    LOW("低油", 3.0),
    MEDIUM("中油", 8.0),
    HIGH("高油", 15.0)
}

enum class SaltLevel(val label: String, val grams: Double) {
    ZERO("不加盐", 0.0),
    LOW("低盐", 0.5),
    MEDIUM("中盐", 1.5),
    HIGH("高盐", 3.0)
}

data class FoodLog(
    val id: Long,
    val food: FoodItem,
    val grams: Double,
    val meal: Meal,
    val oil: OilLevel,
    val salt: SaltLevel
)

data class DayLog(
    val date: LocalDate,
    val entries: List<FoodLog> = emptyList(),
    val waterMl: Int = 0
)

data class PersistedState(
    val days: Map<LocalDate, DayLog> = emptyMap(),
    val profile: BodyProfile = BodyProfile(30, Sex.MALE, 175.0, 70.0),
    val profileVersions: Map<LocalDate, BodyProfile> = emptyMap(),
    val activityPlans: Map<LocalDate, DailyActivityPlan> = emptyMap(),
    val customFoods: List<FoodItem> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val recentFoodIds: List<Int> = emptyList(),
    val ignoredRecommendationNames: List<String> = emptyList(),
    val templates: List<MealTemplate> = emptyList(),
    val measurements: List<BodyMeasurement> = emptyList(),
    val adaptiveSettings: AdaptiveEnergySettings = AdaptiveEnergySettings(),
)

object EntryDefaults {
    fun meal(editing: FoodLog?, requested: Meal?): Meal = editing?.meal ?: requested ?: Meal.BREAKFAST
}

object EntryFormRules {
    fun canSubmit(weightText: String): Boolean = (weightText.toDoubleOrNull() ?: 0.0) > 0.0
}

object CalendarRules {
    fun previousMonth(date: LocalDate): LocalDate = date.minusMonths(1)
    fun nextMonth(date: LocalDate): LocalDate = date.plusMonths(1)
}

object FoodIdRules {
    private const val CUSTOM_ID_START = 100_000

    fun nextCustomId(name: String, kcal: Double, existingIds: Set<Int>): Int {
        var candidate = CUSTOM_ID_START + (name + kcal).hashCode().mod(900_000)
        while (candidate in existingIds) {
            candidate = if (candidate >= 999_999) CUSTOM_ID_START else candidate + 1
        }
        return candidate
    }
}
