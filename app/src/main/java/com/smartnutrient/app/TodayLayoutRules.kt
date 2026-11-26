package com.smartnutrient.app

enum class TodaySection {
    HEADER,
    ENERGY,
    FOCUS,
    WATER,
    QUICK_FOOD,
    MEAL_SHORTCUTS,
    NUTRITION_TOTAL,
    RECORD_HEADER,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    ACTIVITY,
    FOOTNOTE,
    RECOMMENDATION,
}

object TodayLayoutRules {
    val sections: List<TodaySection> = listOf(
        TodaySection.HEADER,
        TodaySection.ENERGY,
        TodaySection.FOCUS,
        TodaySection.WATER,
        TodaySection.QUICK_FOOD,
        TodaySection.MEAL_SHORTCUTS,
        TodaySection.NUTRITION_TOTAL,
        TodaySection.RECORD_HEADER,
        TodaySection.BREAKFAST,
        TodaySection.LUNCH,
        TodaySection.DINNER,
        TodaySection.SNACK,
        TodaySection.ACTIVITY,
        TodaySection.RECOMMENDATION,
        TodaySection.FOOTNOTE,
    )
}
