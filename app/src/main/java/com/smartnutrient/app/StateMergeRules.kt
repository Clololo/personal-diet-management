package com.smartnutrient.app

object StateMergeRules {
    fun merge(current: PersistedState, incoming: PersistedState): PersistedState {
        val currentFoodIds = current.customFoods.map { it.id }.toSet()
        val usedIds = (currentFoodIds + incoming.customFoods.map { it.id }).toMutableSet()
        val idMap = mutableMapOf<Int, Int>()
        val mergedCustomFoods = current.customFoods.toMutableList()

        incoming.customFoods.forEach { food ->
            if (food.id !in currentFoodIds) {
                mergedCustomFoods += food
            } else {
                var candidate = food.id
                while (candidate in usedIds) {
                    candidate = if (candidate >= 999_999) 100_000 else candidate + 1
                }
                usedIds += candidate
                idMap[food.id] = candidate
                mergedCustomFoods += food.copy(id = candidate)
            }
        }

        val days = current.days.toMutableMap()
        incoming.days.forEach { (date, incomingDay) ->
            val currentDay = days[date]
            if (currentDay == null) {
                days[date] = incomingDay.copy(entries = incomingDay.entries.map { it.remapFoodId(idMap) })
            } else {
                val existing = currentDay.entries.toMutableList()
                incomingDay.entries.forEach { entry ->
                    val mapped = entry.remapFoodId(idMap)
                    val duplicate = existing.any {
                        it.food.name == mapped.food.name && it.grams == mapped.grams && it.meal == mapped.meal &&
                            it.oil == mapped.oil && it.salt == mapped.salt
                    }
                    if (!duplicate) existing += mapped
                }
                days[date] = currentDay.copy(entries = existing, waterMl = maxOf(currentDay.waterMl, incomingDay.waterMl))
            }
        }

        val remappedIds = idMap.values.toSet()
        val favorites = (current.favorites + incoming.favorites.map { idMap[it] ?: it }).toSet()
        val recent = (incoming.recentFoodIds.map { idMap[it] ?: it } + current.recentFoodIds)
            .distinct()
            .filter { it !in remappedIds || it in favorites }
            .take(30)

        return PersistedState(
            days = days,
            profile = current.profile,
            profileVersions = current.profileVersions + incoming.profileVersions,
            activityPlans = current.activityPlans + incoming.activityPlans,
            templates = (current.templates + incoming.templates).distinctBy { it.id },
            measurements = (current.measurements + incoming.measurements).groupBy { it.date }.map { it.value.last() }.sortedBy { it.date },
            adaptiveSettings = incoming.adaptiveSettings,
            customFoods = mergedCustomFoods,
            favorites = favorites,
            recentFoodIds = recent,
            ignoredRecommendationNames = (current.ignoredRecommendationNames + incoming.ignoredRecommendationNames)
                .distinct()
                .take(100),
        )
    }

    private fun FoodLog.remapFoodId(idMap: Map<Int, Int>): FoodLog {
        val newId = idMap[food.id] ?: return this
        return copy(food = food.copy(id = newId))
    }
}
