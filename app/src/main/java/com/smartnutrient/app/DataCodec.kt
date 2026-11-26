package com.smartnutrient.app

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.time.LocalDate

data class DecodedBackup(
    val logsByDate: Map<LocalDate, List<FoodLog>>,
    val customFoods: List<FoodItem>,
    val favorites: Set<Int> = emptySet(),
    val profile: BodyProfile = BodyProfile(30, Sex.MALE, 175.0, 70.0),
    val waterByDate: Map<LocalDate, Int> = emptyMap(),
    val recentFoodIds: List<Int> = emptyList(),
    val ignoredRecommendationNames: List<String> = emptyList(),
    val profileVersions: Map<LocalDate, BodyProfile> = emptyMap(),
    val activityPlans: Map<LocalDate, DailyActivityPlan> = emptyMap(),
    val templates: List<MealTemplate> = emptyList(),
    val measurements: List<BodyMeasurement> = emptyList(),
    val adaptiveSettings: AdaptiveEnergySettings = AdaptiveEnergySettings(),
)

data class BackupFood(
    val id: Int = 0,
    val name: String = "",
    val pinyin: String = "",
    val initials: String = "",
    val category: String = "",
    val basis: String = "",
    val nutrients: FoodNutrients = FoodNutrients(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val aliases: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val method: String = "",
    val source: String = "标准配方估算",
    val confidence: String = "估算",
    val dataQuality: String = FoodDataQuality.RECIPE_ESTIMATE.name,
    val includesSeasoningPack: Boolean? = null,
)

data class BackupLog(
    val id: Long = 0,
    val grams: Double = 0.0,
    val meal: String = "",
    val oil: String = "",
    val salt: String = "",
    val foodId: Int? = null,
    val food: BackupFood = BackupFood()
)

data class BackupDay(val date: String = "", val entries: List<BackupLog> = emptyList(), val waterMl: Int = 0)
data class BackupProfile(
    val age: Int = 30,
    val sex: String = Sex.MALE.name,
    val heightCm: Double = 175.0,
    val weightKg: Double = 70.0,
    val bodyType: String = BodyType.STANDARD.name,
    val waistCm: Double? = null,
    val bodyFatPercent: Double? = null,
    val activity: String = ActivityLevel.MODERATE.name,
    val activityKcal: Double? = null,
    val activityMode: String = ActivityEnergyMode.AUTO.name,
    val goal: String = WeightGoal.MAINTAIN.name
)

data class BackupPayload(
    val schemaVersion: Int = CURRENT_SCHEMA,
    val days: List<BackupDay> = emptyList(),
    val customFoods: List<BackupFood> = emptyList(),
    val favorites: List<Int> = emptyList(),
    val profile: BackupProfile? = null,
    val format: String? = null,
    val recentFoodIds: List<Int> = emptyList(),
    val ignoredRecommendationNames: List<String> = emptyList(),
    val profileVersions: List<BackupProfileVersion> = emptyList(),
    val activityPlans: List<BackupActivityPlan> = emptyList(),
    val templates: List<BackupMealTemplate> = emptyList(),
    val measurements: List<BackupBodyMeasurement> = emptyList(),
    val adaptiveSettings: BackupAdaptiveSettings = BackupAdaptiveSettings(),
)

data class BackupProfileVersion(
    val date: String = "",
    val profile: BackupProfile = BackupProfile(),
)

data class BackupActivityPlan(
    val date: String = "",
    val manualExerciseKcal: Double = 0.0,
    val steps: Int = 0,
    /** Legacy fields kept so existing backups keep restoring. */
    val activity: String? = null,
    val activityKcal: Double? = null,
    val activityMode: String? = null,
)

data class BackupMealTemplateItem(
    val food: BackupFood = BackupFood(),
    val grams: Double = 0.0,
    val oil: String = OilLevel.ZERO.name,
    val salt: String = SaltLevel.ZERO.name,
)

data class BackupMealTemplate(
    val id: Long = 0,
    val name: String = "",
    val meal: String = Meal.BREAKFAST.name,
    val items: List<BackupMealTemplateItem> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = "",
    val usageCount: Int = 0,
)

data class BackupBodyMeasurement(
    val date: String = "",
    val weightKg: Double = 0.0,
    val waistCm: Double? = null,
    val bodyFatPercent: Double? = null,
    val note: String = "",
)

data class BackupAdaptiveSettings(
    val enabled: Boolean = false,
    val effectiveFrom: String? = null,
)

data class LoadedState(
    val state: PersistedState,
    val recoveryNotice: String? = null
)

const val CURRENT_SCHEMA = 7
private const val BACKUP_FORMAT = "smartnutrient"

object DataCodec {
    private val gson = Gson()

    fun exportState(state: PersistedState): String {
        val payload = BackupPayload(
            schemaVersion = CURRENT_SCHEMA,
            days = state.days.toSortedMap().map { (date, day) ->
                BackupDay(date.toString(), day.entries.map(::toBackupLog), day.waterMl)
            },
            customFoods = state.customFoods.map(::toBackupFood),
            favorites = state.favorites.toList(),
            profile = toBackupProfile(state.profile),
            format = BACKUP_FORMAT,
            recentFoodIds = state.recentFoodIds.take(30),
            ignoredRecommendationNames = state.ignoredRecommendationNames.take(100),
            profileVersions = state.profileVersions.toSortedMap().map { (date, value) ->
                BackupProfileVersion(date.toString(), toBackupProfile(value))
            },
            activityPlans = state.activityPlans.toSortedMap().map { (date, plan) ->
                BackupActivityPlan(
                    date = date.toString(),
                    manualExerciseKcal = plan.manualExerciseKcal,
                    steps = plan.steps,
                )
            },
            templates = state.templates.map { template ->
                BackupMealTemplate(
                    id = template.id,
                    name = template.name,
                    meal = template.meal.name,
                    items = template.items.map { item ->
                        BackupMealTemplateItem(toBackupFood(item.food), item.grams, item.oil.name, item.salt.name)
                    },
                    createdAt = template.createdAt.toString(),
                    updatedAt = template.updatedAt.toString(),
                    usageCount = template.usageCount,
                )
            },
            measurements = state.measurements.map { measurement ->
                BackupBodyMeasurement(
                    date = measurement.date.toString(),
                    weightKg = measurement.weightKg,
                    waistCm = measurement.waistCm,
                    bodyFatPercent = measurement.bodyFatPercent,
                    note = measurement.note,
                )
            },
            adaptiveSettings = BackupAdaptiveSettings(
                enabled = state.adaptiveSettings.enabled,
                effectiveFrom = state.adaptiveSettings.effectiveFrom?.toString(),
            ),
        )
        return gson.toJson(payload)
    }

    fun export(logsByDate: Map<LocalDate, List<FoodLog>>, customFoods: List<FoodItem>, favorites: Set<Int> = emptySet()): String =
        exportState(PersistedState(days = logsByDate.mapValues { (date, logs) -> DayLog(date, logs) }, customFoods = customFoods, favorites = favorites))

    /** Migrate legacy food snapshots without inventing an added-sugar value. */
    fun migrateLegacyFoods(foods: List<FoodItem>): List<FoodItem> = foods.map { food ->
        if (food.nutrients.addedSugarG != null) food else food.copy(
            nutrients = food.nutrients.copy(
                addedSugarG = SugarDataPolicy.addedSugarG(
                    food.category,
                    food.name,
                    food.nutrients.sugarG,
                ),
            ),
        )
    }

    fun decode(json: String): DecodedBackup {
        val payload = gson.fromJson(json, BackupPayload::class.java) ?: BackupPayload()
        val customFoods = payload.customFoods.map { toFoodItem(it) }
        val days = linkedMapOf<LocalDate, List<FoodLog>>()
        val water = linkedMapOf<LocalDate, Int>()
        payload.days.forEach { day ->
            val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: return@forEach
            days[date] = day.entries.mapNotNull { toFoodLog(it, payload.schemaVersion) }
            water[date] = day.waterMl
        }
        val profile = decodeProfile(payload.profile ?: BackupProfile(), payload.schemaVersion)
        val profileVersions = payload.profileVersions.mapNotNull { version ->
            val date = runCatching { LocalDate.parse(version.date) }.getOrNull() ?: return@mapNotNull null
            date to decodeProfile(version.profile, payload.schemaVersion)
        }.toMap()
        val activityPlans = payload.activityPlans.mapNotNull { plan ->
            val date = runCatching { LocalDate.parse(plan.date) }.getOrNull() ?: return@mapNotNull null
            if (plan.manualExerciseKcal !in 0.0..3000.0 || plan.steps !in 0..100_000) return@mapNotNull null
            val legacyKcal = plan.activityKcal?.takeIf { it in 0.0..3000.0 } ?: 0.0
            date to DailyActivityPlan(plan.manualExerciseKcal.takeIf { it > 0.0 } ?: legacyKcal, plan.steps)
        }.toMap()
        val templates = payload.templates.mapNotNull { template ->
            val created = runCatching { LocalDate.parse(template.createdAt) }.getOrDefault(LocalDate.now())
            val updated = runCatching { LocalDate.parse(template.updatedAt) }.getOrDefault(created)
            val meal = runCatching { Meal.valueOf(template.meal) }.getOrNull() ?: return@mapNotNull null
            val items = template.items.mapNotNull { item ->
                if (item.grams <= 0.0) return@mapNotNull null
                MealTemplateItem(
                    toFoodItem(item.food),
                    item.grams,
                    runCatching { OilLevel.valueOf(item.oil) }.getOrDefault(OilLevel.ZERO),
                    runCatching { SaltLevel.valueOf(item.salt) }.getOrDefault(SaltLevel.ZERO),
                )
            }
            if (template.name.isBlank() || items.isEmpty()) return@mapNotNull null
            MealTemplate(template.id, template.name, meal, items, created, updated, template.usageCount.coerceAtLeast(0))
        }
        val measurements = payload.measurements.mapNotNull { measurement ->
            val date = runCatching { LocalDate.parse(measurement.date) }.getOrNull() ?: return@mapNotNull null
            if (measurement.weightKg !in 30.0..300.0) return@mapNotNull null
            BodyMeasurement(date, measurement.weightKg, measurement.waistCm, measurement.bodyFatPercent, measurement.note)
        }.distinctBy { it.date }.sortedBy { it.date }
        val adaptiveSettings = AdaptiveEnergySettings(
            enabled = payload.adaptiveSettings.enabled,
            effectiveFrom = payload.adaptiveSettings.effectiveFrom?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        )
        return DecodedBackup(
            days,
            customFoods,
            payload.favorites.toSet(),
            profile,
            water,
            payload.recentFoodIds.take(30).distinct(),
            payload.ignoredRecommendationNames.distinct().take(100),
            profileVersions,
            activityPlans,
            templates,
            measurements,
            adaptiveSettings,
        )
    }

    fun inspect(json: String): BackupInspection {
        if (!isValidBackup(json)) {
            return BackupInspection(valid = false, message = "文件不是可用的 SmartNutrient 备份，或包含不支持的数值。")
        }
        val decoded = decode(json)
        val first = decoded.logsByDate.keys.minOrNull()
        val last = decoded.logsByDate.keys.maxOrNull()
        val entries = decoded.logsByDate.values.sumOf { it.size }
        val preview = buildList {
            if (first != null && last != null) add("记录范围 $first 至 $last")
            add("$entries 条饮食记录")
            if (decoded.waterByDate.any { it.value > 0 }) add("含饮水记录")
            if (decoded.customFoods.isNotEmpty()) add("${decoded.customFoods.size} 个自定义食物")
            add("身体档案 ${decoded.profile.age} 岁 / ${decoded.profile.weightKg} kg")
        }.joinToString(" · ")
        return BackupInspection(
            valid = true,
            dayCount = decoded.logsByDate.size,
            entryCount = entries,
            customFoodCount = decoded.customFoods.size,
            firstDate = first,
            lastDate = last,
            profileSummary = "${decoded.profile.age} 岁 / ${decoded.profile.weightKg} kg",
            message = preview,
        )
    }
    fun isValidBackup(json: String): Boolean {
        val root = runCatching { gson.fromJson(json, JsonObject::class.java) }.getOrNull() ?: return false
        if (!root.has("schemaVersion") || !root.has("days")) return false
        val payload = runCatching { gson.fromJson(root, BackupPayload::class.java) }.getOrNull() ?: return false
        if (payload.schemaVersion !in 1..CURRENT_SCHEMA) return false
        if (payload.schemaVersion == CURRENT_SCHEMA && payload.format != BACKUP_FORMAT) return false
        if (!isValidProfile(payload.profile, payload.schemaVersion)) return false
        if (!isValidProfileVersions(payload.profileVersions) || !isValidActivityPlans(payload.activityPlans)) return false
        if (!isValidTemplates(payload.templates) || !isValidMeasurements(payload.measurements)) return false
        if (payload.adaptiveSettings.effectiveFrom?.let { runCatching { LocalDate.parse(it) }.isSuccess } == false) return false
        return payload.days.all { day ->
            runCatching { LocalDate.parse(day.date) }.isSuccess &&
                day.waterMl in 0..5000 &&
                day.entries.all { isValidLog(it, payload.schemaVersion) }
        } && payload.customFoods.all(::isValidFood) && payload.favorites.size == payload.favorites.distinct().size &&
            payload.recentFoodIds.size == payload.recentFoodIds.distinct().size && payload.recentFoodIds.size <= 30 &&
            payload.ignoredRecommendationNames.size == payload.ignoredRecommendationNames.distinct().size &&
            payload.ignoredRecommendationNames.size <= 100
    }

    private fun isValidLog(log: BackupLog, schemaVersion: Int = CURRENT_SCHEMA): Boolean =
        log.id >= 0 && log.grams in 0.01..10000.0 && Meal.entries.any { it.name == log.meal } &&
            OilLevel.entries.any { it.name == log.oil } && SaltLevel.entries.any { it.name == log.salt } &&
            (isValidFood(log.food) || (schemaVersion == 1 && SampleFoods.items.any { it.id == log.foodId }))

    private fun isValidFood(food: BackupFood) = food.name.isNotBlank() && food.nutrients.caloriesKcal >= 0.0 &&
        food.nutrients.proteinG >= 0.0 && food.nutrients.fatG >= 0.0 && food.nutrients.carbohydrateG >= 0.0 &&
        food.nutrients.fiberG >= 0.0 && food.nutrients.sodiumMg >= 0.0 &&
        food.nutrients.addedSugarG?.let { it >= 0.0 && it <= food.nutrients.sugarG + 0.01 } != false

    private fun isValidProfile(profile: BackupProfile?, schemaVersion: Int): Boolean {
        if (profile == null) return schemaVersion == 1
        return profile.age in 18..100 && profile.heightCm in 100.0..250.0 &&
            profile.weightKg in 30.0..300.0 && profile.waistCm?.let { it in 40.0..200.0 } != false &&
            profile.bodyFatPercent?.let { it in 2.0..70.0 } != false &&
            Sex.entries.any { it.name == profile.sex } && ActivityLevel.entries.any { it.name == profile.activity } &&
            WeightGoal.entries.any { it.name == profile.goal } && BodyType.entries.any { it.name == profile.bodyType } &&
            (schemaVersion < 3 || ActivityEnergyMode.entries.any { it.name == profile.activityMode }) &&
            profile.activityKcal?.let { it in 0.0..3000.0 } != false
    }

    private fun decodeProfile(profile: BackupProfile, schemaVersion: Int): BodyProfile = BodyProfile(
        age = profile.age,
        sex = runCatching { Sex.valueOf(profile.sex) }.getOrDefault(Sex.MALE),
        heightCm = profile.heightCm,
        weightKg = profile.weightKg,
        bodyType = runCatching { BodyType.valueOf(profile.bodyType) }.getOrDefault(BodyType.STANDARD),
        waistCm = profile.waistCm,
        bodyFatPercent = profile.bodyFatPercent,
        activity = runCatching { ActivityLevel.valueOf(profile.activity) }.getOrDefault(ActivityLevel.MODERATE),
        activityKcal = profile.activityKcal,
        activityMode = if (schemaVersion < 3) {
            if (profile.activityKcal != null) ActivityEnergyMode.MANUAL else ActivityEnergyMode.AUTO
        } else {
            runCatching { ActivityEnergyMode.valueOf(profile.activityMode) }.getOrDefault(ActivityEnergyMode.AUTO)
        },
        goal = runCatching { WeightGoal.valueOf(profile.goal) }.getOrDefault(WeightGoal.MAINTAIN),
    )

    private fun isValidProfileVersions(values: List<BackupProfileVersion>): Boolean {
        val dates = values.map { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        if (dates.any { it == null }) return false
        return values.zip(dates).groupBy { it.second }.all { it.value.size == 1 } &&
            values.all { isValidProfile(it.profile, CURRENT_SCHEMA) }
    }

    private fun isValidActivityPlans(values: List<BackupActivityPlan>): Boolean {
        val dates = values.map { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        if (dates.any { it == null }) return false
        return values.zip(dates).groupBy { it.second }.all { it.value.size == 1 } &&
            values.all { plan ->
                plan.manualExerciseKcal in 0.0..3000.0 &&
                    plan.steps in 0..100_000 &&
                    (plan.activity == null || ActivityLevel.entries.any { it.name == plan.activity }) &&
                    (plan.activityMode == null || ActivityEnergyMode.entries.any { it.name == plan.activityMode }) &&
                    plan.activityKcal?.let { it in 0.0..3000.0 } != false
            }
    }

    private fun isValidTemplates(values: List<BackupMealTemplate>): Boolean =
        values.all { template ->
            template.name.isNotBlank() && Meal.entries.any { it.name == template.meal } &&
                runCatching { LocalDate.parse(template.createdAt) }.isSuccess &&
                runCatching { LocalDate.parse(template.updatedAt) }.isSuccess &&
                template.items.isNotEmpty() && template.items.all { item ->
                    item.grams in 0.01..10_000.0 && isValidFood(item.food) &&
                        OilLevel.entries.any { it.name == item.oil } && SaltLevel.entries.any { it.name == item.salt }
                }
        }

    private fun isValidMeasurements(values: List<BackupBodyMeasurement>): Boolean {
        val dates = values.map { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        if (dates.any { it == null }) return false
        return values.zip(dates).groupBy { it.second }.all { it.value.size == 1 } &&
            values.all { it.weightKg in 30.0..300.0 && it.waistCm?.let { v -> v in 40.0..200.0 } != false && it.bodyFatPercent?.let { v -> v in 2.0..70.0 } != false }
    }

    private fun toBackupLog(log: FoodLog) = BackupLog(
        log.id, log.grams, log.meal.name, log.oil.name, log.salt.name, log.food.id, toBackupFood(log.food)
    )

    private fun toFoodLog(log: BackupLog, schemaVersion: Int): FoodLog? {
        if (!isValidLog(log, schemaVersion)) return null
        val food = if (isValidFood(log.food)) toFoodItem(log.food) else SampleFoods.items.first { it.id == log.foodId }
        return FoodLog(log.id, food, log.grams, Meal.valueOf(log.meal), OilLevel.valueOf(log.oil), SaltLevel.valueOf(log.salt))
    }

    private fun toBackupFood(food: FoodItem) = BackupFood(
        food.id, food.name, food.pinyin, food.initials, food.category, food.basis,
        food.nutrients, food.aliases, food.ingredients, food.method, food.source, food.confidence,
        food.dataQuality.name, food.includesSeasoningPack,
    )

    private fun toFoodItem(food: BackupFood): FoodItem {
        val nutrients = food.nutrients.copy(
            addedSugarG = food.nutrients.addedSugarG ?: SugarDataPolicy.addedSugarG(
                food.category,
                food.name,
                food.nutrients.sugarG,
            ),
        )
        return FoodItem(
            food.id, food.name, food.pinyin, food.initials, food.category, food.basis,
            nutrients, food.aliases, food.ingredients, food.method, food.source, food.confidence,
            runCatching { FoodDataQuality.valueOf(food.dataQuality) }.getOrDefault(FoodDataQuality.RECIPE_ESTIMATE),
            food.includesSeasoningPack,
        )
    }

    private fun toBackupProfile(profile: BodyProfile) = BackupProfile(
        profile.age, profile.sex.name, profile.heightCm, profile.weightKg, profile.bodyType.name,
        profile.waistCm, profile.bodyFatPercent, profile.activity.name, profile.activityKcal,
        profile.activityMode.name, profile.goal.name,
    )
}

data class BackupInspection(
    val valid: Boolean,
    val message: String,
    val dayCount: Int = 0,
    val entryCount: Int = 0,
    val customFoodCount: Int = 0,
    val firstDate: LocalDate? = null,
    val lastDate: LocalDate? = null,
    val profileSummary: String = "",
)
