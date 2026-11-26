package com.smartnutrient.app

import kotlin.math.roundToInt

enum class Sex { MALE, FEMALE }
enum class ActivityLevel(val factor: Double) { SEDENTARY(1.2), LIGHT(1.375), MODERATE(1.55), ACTIVE(1.725) }
enum class WeightGoal(val energyMultiplier: Double) { LOSE(0.85), MAINTAIN(1.0), GAIN(1.10) }
enum class ActivityEnergyMode { AUTO, MANUAL }
enum class SugarCoverage { COMPLETE, TOTAL_ONLY, UNKNOWN }
enum class BodyType(val label: String, val energyMultiplier: Double) {
    SLIM("偏瘦", 0.95),
    STANDARD("标准", 1.0),
    ATHLETIC("健壮", 1.05),
    FULL("偏丰满", 1.08)
}

data class BodyProfile(
    val age: Int,
    val sex: Sex,
    val heightCm: Double,
    val weightKg: Double,
    val bodyType: BodyType = BodyType.STANDARD,
    val waistCm: Double? = null,
    val bodyFatPercent: Double? = null,
    val activity: ActivityLevel = ActivityLevel.MODERATE,
    val activityKcal: Double? = null,
    val activityMode: ActivityEnergyMode = if (activityKcal != null) ActivityEnergyMode.MANUAL else ActivityEnergyMode.AUTO,
    val goal: WeightGoal = WeightGoal.MAINTAIN,
)

data class EnergyTarget(val centerKcal: Double, val minKcal: Double, val maxKcal: Double)

data class EnergyBreakdown(
    val restingKcal: Double,
    val manualExerciseKcal: Double,
    val walkingKcal: Double,
    val target: EnergyTarget
) {
    val activityKcal: Double get() = manualExerciseKcal + walkingKcal
}

data class FoodNutrients(
    val caloriesKcal: Double,
    val proteinG: Double,
    val fatG: Double,
    val saturatedFatG: Double,
    val carbohydrateG: Double,
    val fiberG: Double,
    val sugarG: Double,
    val sodiumMg: Double,
    val potassiumMg: Double,
    val cholesterolMg: Double,
    val addedSugarG: Double? = null,
)

data class NutritionSnapshot(
    val caloriesKcal: Double,
    val proteinG: Double,
    val fatG: Double,
    val saturatedFatG: Double,
    val carbohydrateG: Double,
    val fiberG: Double,
    val sugarG: Double,
    val sodiumMg: Double,
    val potassiumMg: Double,
    val cholesterolMg: Double,
    val addedSugarG: Double? = null,
) {
    val sugarCoverage: SugarCoverage
        get() = when {
            addedSugarG != null -> SugarCoverage.COMPLETE
            sugarG > 0.0 -> SugarCoverage.TOTAL_ONLY
            else -> SugarCoverage.UNKNOWN
        }
}

data class DailyNutrition(val total: NutritionSnapshot) {
    companion object {
        fun total(entries: List<NutritionSnapshot>): NutritionSnapshot {
            val addedSugar = SugarDataPolicy.sumKnown(entries.map { it.addedSugarG })
            return NutritionSnapshot(
                entries.sumOf { it.caloriesKcal },
                entries.sumOf { it.proteinG },
                entries.sumOf { it.fatG },
                entries.sumOf { it.saturatedFatG },
                entries.sumOf { it.carbohydrateG },
                entries.sumOf { it.fiberG },
                entries.sumOf { it.sugarG },
                entries.sumOf { it.sodiumMg },
                entries.sumOf { it.potassiumMg },
                entries.sumOf { it.cholesterolMg },
                addedSugar,
            )
        }
    }
}

data class ServingSeasoning(
    val oilG: Double,
    val saltG: Double,
) {
    companion object {
        val Low = ServingSeasoning(3.0, 0.5)
        val Medium = ServingSeasoning(8.0, 1.5)
        val High = ServingSeasoning(15.0, 3.0)
    }
}

object NutritionEngine {
    fun calculateForRecord(food: FoodItem, weightG: Double): NutritionSnapshot {
        require(weightG >= 0.0) { "Weight cannot be negative" }
        if (food.basis != "每份") return calculate(food.nutrients, weightG)
        val servingWeight = servingWeight(food)
        return calculate(food.nutrients, 100.0 * weightG / servingWeight)
    }

    /**
     * Calculates an actual logged portion. The selected oil and salt describe the
     * preparation of the food's declared basis, so they must use the same scale as
     * the food nutrients rather than being added as a fixed amount to every portion.
     */
    fun recordSnapshot(
        food: FoodItem,
        weightG: Double,
        oil: OilLevel,
        salt: SaltLevel,
    ): NutritionSnapshot = recordSnapshot(food, weightG, ServingSeasoning(oil.grams, salt.grams))

    fun recordSnapshot(food: FoodItem, weightG: Double, seasoning: ServingSeasoning): NutritionSnapshot {
        require(weightG >= 0.0) { "Weight cannot be negative" }
        val scale = weightG / basisWeight(food)
        return applySeasoning(
            calculateForRecord(food, weightG),
            ServingSeasoning(
                oilG = seasoning.oilG.coerceAtLeast(0.0) * scale,
                saltG = seasoning.saltG.coerceAtLeast(0.0) * scale,
            ),
        )
    }

    private fun basisWeight(food: FoodItem): Double =
        if (food.basis == "每份") servingWeight(food) else 100.0

    private fun servingWeight(food: FoodItem): Double =
        food.aliases.firstOrNull { it.startsWith("每份") }
            ?.let { Regex("""[0-9]+(?:\\.[0-9]+)?""").find(it)?.value?.toDoubleOrNull() }
            ?.takeIf { it > 0.0 }
            ?: 200.0

    fun calculate(food: FoodNutrients, weightG: Double): NutritionSnapshot {
        require(weightG >= 0.0) { "Weight cannot be negative" }
        val scale = weightG / 100.0
        return NutritionSnapshot(
            caloriesKcal = food.caloriesKcal * scale,
            proteinG = food.proteinG * scale,
            fatG = food.fatG * scale,
            saturatedFatG = food.saturatedFatG * scale,
            carbohydrateG = food.carbohydrateG * scale,
            fiberG = food.fiberG * scale,
            sugarG = food.sugarG * scale,
            sodiumMg = food.sodiumMg * scale,
            potassiumMg = food.potassiumMg * scale,
            cholesterolMg = food.cholesterolMg * scale,
            addedSugarG = food.addedSugarG?.times(scale),
        )
    }

    fun applySeasoning(base: NutritionSnapshot, oil: OilLevel, salt: SaltLevel): NutritionSnapshot {
        return applySeasoning(base, ServingSeasoning(oil.grams, salt.grams))
    }

    fun applySeasoning(base: NutritionSnapshot, seasoning: ServingSeasoning): NutritionSnapshot = base.copy(
        caloriesKcal = base.caloriesKcal + seasoning.oilG * 9.0,
        fatG = base.fatG + seasoning.oilG,
        saturatedFatG = base.saturatedFatG + seasoning.oilG * 0.16,
        sodiumMg = base.sodiumMg + seasoning.saltG * 400.0,
    )

    fun restingEnergy(profile: BodyProfile): Double {
        val bmr = if (profile.sex == Sex.MALE) {
            10.0 * profile.weightKg + 6.25 * profile.heightCm - 5.0 * profile.age + 5.0
        } else {
            10.0 * profile.weightKg + 6.25 * profile.heightCm - 5.0 * profile.age - 161.0
        }
        return bmr * profile.bodyType.energyMultiplier
    }

    fun estimatedActivityEnergy(profile: BodyProfile): Double =
        restingEnergy(profile) * (profile.activity.factor - 1.0)

    fun estimatedWalkingGrossKcal(weightKg: Double, steps: Int): Double =
        (weightKg.coerceAtLeast(0.0) * steps.coerceAtLeast(0) / 1_000.0) * 0.55

    fun estimatedWalkingNetKcal(weightKg: Double, steps: Int): Double =
        (weightKg.coerceAtLeast(0.0) * steps.coerceAtLeast(0) / 1_000.0) * 0.45

    fun activityEnergy(profile: BodyProfile, plan: DailyActivityPlan = DailyActivityPlan()): Double =
        plan.manualExerciseKcal.coerceIn(0.0, 3000.0) +
            estimatedWalkingNetKcal(profile.weightKg, plan.steps)

    fun energyBreakdown(
        profile: BodyProfile,
        plan: DailyActivityPlan = DailyActivityPlan(),
    ): EnergyBreakdown {
        val resting = restingEnergy(profile)
        val manual = plan.manualExerciseKcal.coerceIn(0.0, 3000.0)
        val walking = estimatedWalkingNetKcal(profile.weightKg, plan.steps)
        val activity = manual + walking
        val center = (resting + activity) * profile.goal.energyMultiplier
        val target = EnergyTarget(centerKcal = center, minKcal = center * 0.9, maxKcal = center * 1.1)
        return EnergyBreakdown(resting, manual, walking, target)
    }

    fun displayTargets(energy: EnergyTarget, baselineCalories: Double, sex: Sex, age: Int): NutritionTargets {
        val macro = targets(energy.centerKcal, sex, age)
        val proteinScale = energy.centerKcal / baselineCalories.coerceAtLeast(1.0)
        val fatScale = energy.centerKcal / baselineCalories.coerceAtLeast(1.0)
        val carbScale = energy.centerKcal / baselineCalories.coerceAtLeast(1.0)
        return NutritionTargets(
            calories = NutrientTarget("热量", energy.minKcal, energy.maxKcal, "kcal", false),
            protein = NutrientTarget("蛋白质", macro.proteinMinG * proteinScale, macro.proteinMaxG * proteinScale, "g", false),
            fat = NutrientTarget("脂肪", macro.fatMinG * fatScale, macro.fatMaxG * fatScale, "g", true),
            saturatedFat = NutrientTarget("饱和脂肪", 0.0, macro.fatMaxG * fatScale * 0.35, "g", true),
            carbohydrate = NutrientTarget("碳水化合物", macro.carbohydrateMinG * carbScale, macro.carbohydrateMaxG * carbScale, "g", false),
            fiber = NutrientTarget("膳食纤维", macro.fiberMinG, 0.0, "g", false),
            sugar = NutrientTarget("添加糖", 0.0, energy.centerKcal * 0.10 / 4.0, "g", true),
            sodium = NutrientTarget("钠", 0.0, macro.sodiumMaxMg, "mg", true),
            potassium = NutrientTarget("钾", 3000.0, 0.0, "mg", false),
            cholesterol = NutrientTarget("胆固醇", 0.0, macro.cholesterolMaxMg, "mg", true),
        )
    }
    fun energyTarget(profile: BodyProfile, plan: DailyActivityPlan = DailyActivityPlan()): EnergyTarget {
        val breakdown = energyBreakdown(profile, plan)
        val center = breakdown.target.centerKcal
        return EnergyTarget(centerKcal = center, minKcal = center * 0.9, maxKcal = center * 1.1)
    }
}



data class MacroTargets(
    val proteinMinG: Double,
    val proteinMaxG: Double,
    val fatMinG: Double,
    val fatMaxG: Double,
    val carbohydrateMinG: Double,
    val carbohydrateMaxG: Double,
    val sodiumMaxMg: Double,
    val cholesterolMaxMg: Double,
    val fiberMinG: Double
)

fun NutritionEngine.targets(calories: Double, sex: Sex, age: Int): MacroTargets {
    val proteinMin = calories * 0.15 / 4.0
    val proteinMax = if (age <= 65) calories * 0.20 / 4.0 else proteinMin
    return MacroTargets(
        proteinMinG = proteinMin,
        proteinMaxG = proteinMax,
        fatMinG = calories * 0.20 / 9.0,
        fatMaxG = calories * 0.30 / 9.0,
        carbohydrateMinG = calories * 0.45 / 4.0,
        carbohydrateMaxG = calories * 0.65 / 4.0,
        sodiumMaxMg = 2000.0,
        cholesterolMaxMg = 300.0,
        fiberMinG = if (sex == Sex.MALE) 30.0 else 25.0
    )
}
