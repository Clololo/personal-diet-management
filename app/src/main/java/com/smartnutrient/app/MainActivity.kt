package com.smartnutrient.app

import android.content.Intent
import android.os.Bundle
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class Tab { TODAY, CALENDAR, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartNutrientTheme { SmartNutrientApp() } }
    }
}

@Composable
private fun SmartNutrientApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val model = remember { AppState(LocalStore(context.applicationContext)) }
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    Surface(color = Night, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Night,
            bottomBar = {
                NavigationBar(containerColor = DeepSurface) {
                    listOf(Tab.TODAY to "今日", Tab.CALENDAR to "日历", Tab.PROFILE to "我的").forEach { (item, label) ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(if (item == Tab.TODAY) "◉" else if (item == Tab.CALENDAR) "▦" else "◎", color = if (tab == item) Mint else Muted) },
                            label = { Text(label, color = if (tab == item) Mint else Muted) }
                        )
                    }
                }
            }
        ) { padding ->
            when (tab) {
                Tab.TODAY -> TodayScreen(model, Modifier.padding(padding))
                Tab.CALENDAR -> CalendarScreen(model, Modifier.padding(padding))
                Tab.PROFILE -> ProfileScreen(model, Modifier.padding(padding))
            }
        }
        if (model.adding || model.editing != null) AddFoodDialog(model)
        if (model.addingCustom) CustomFoodDialog(model, onDismiss = { model.addingCustom = false }) {
            model.addCustomFood(it)
            model.addingCustom = false
        }
        model.backupMode?.let { mode -> BackupDialog(model, mode) }
        model.confirmAction?.let { action -> ConfirmDialog(model, action) }
        model.copySourceMeal?.let { source -> CopyMealDialog(model, source) }
        model.recoveryNotice?.let { notice -> RecoveryDialog(model, notice) }
        model.viewingNutritionLog?.let { log -> NutritionDetailDialog(model, log) }
    }
}

internal class AppState(private val store: LocalStore) {
    private val loaded = store.load()
    private val initial = loaded.state
    var recoveryNoticeDismissed by mutableStateOf(false)
    val recoveryNotice: String? get() = loaded.recoveryNotice.takeUnless { recoveryNoticeDismissed }
    private val logs = mutableStateMapOf<LocalDate, DayLog>().apply { putAll(initial.days) }
    val customFoods = mutableStateListOf<FoodItem>().apply { addAll(initial.customFoods) }
    val favorites = mutableStateListOf<Int>().apply { addAll(initial.favorites) }
    val recentFoodIds = mutableStateListOf<Int>().apply { addAll(initial.recentFoodIds) }
    val ignoredRecommendationNames = mutableStateListOf<String>().apply { addAll(initial.ignoredRecommendationNames) }
    val mealTemplates = mutableStateListOf<MealTemplate>().apply { addAll(initial.templates) }
    val bodyMeasurements = mutableStateListOf<BodyMeasurement>().apply { addAll(initial.measurements) }
    var selectedDate by mutableStateOf(LocalDate.now())
    var adding by mutableStateOf(false)
    /** Kept for historical recipe compatibility; no primary entry point exposes it. */
    internal var addingFreeform by mutableStateOf(false)
    var addingCustom by mutableStateOf(false)
    var backupMode by mutableStateOf<String?>(null)
    var confirmAction by mutableStateOf<String?>(null)
    var editing by mutableStateOf<FoodLog?>(null)
    var viewingNutritionLog by mutableStateOf<FoodLog?>(null)
    var importNotice by mutableStateOf<String?>(null)
    var preferredMeal by mutableStateOf(Meal.BREAKFAST)
    var copySourceMeal by mutableStateOf<Meal?>(null)
    var importError by mutableStateOf<String?>(null)
    var quickAddFood by mutableStateOf<FoodItem?>(null)
    var shareNotice by mutableStateOf<String?>(null)
    var profile by mutableStateOf(initial.profile)
    var profileVersions by mutableStateOf(initial.profileVersions)
    var activityPlans by mutableStateOf(initial.activityPlans)
    var adaptiveSettings by mutableStateOf(initial.adaptiveSettings)
    private var nextId = (initial.days.values.flatMap { it.entries }.maxOfOrNull { it.id } ?: 0L) + 1L

    val allFoods: List<FoodItem> get() = EntryFoodCatalog.items + customFoods
    val selectedDay: DayLog get() = logs[selectedDate] ?: DayLog(selectedDate)
    val selectedActivityPlan: DailyActivityPlan get() = DailyPlans.activityPlanOn(activityPlans, DailyActivityPlan(), selectedDate)
    val activeProfile: BodyProfile get() = DailyPlans.profileForDate(profile, profileVersions, activityPlans, selectedDate)
    val energyTarget: EnergyTarget get() = energyTargetForDate(selectedDate)

    fun energyTargetForDate(date: LocalDate): EnergyTarget =
        adaptiveEvaluation(date).let { EnergyTarget(it.centerKcal, it.centerKcal * 0.9, it.centerKcal * 1.1) }

    fun add(food: FoodItem, grams: Double, meal: Meal, oil: OilLevel, salt: SaltLevel) {
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries + FoodLog(nextId++, food, grams, meal, oil, salt))
        noteRecent(food.id)
        persist()
    }

    fun update(id: Long, grams: Double, meal: Meal, oil: OilLevel, salt: SaltLevel) {
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries.map {
            if (it.id == id) it.copy(grams = grams, meal = meal, oil = oil, salt = salt) else it
        })
        persist()
    }

    fun remove(id: Long) {
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries.filterNot { it.id == id })
        persist()
    }

    fun clearDay() {
        logs[selectedDate] = DayLog(selectedDate)
        persist()
    }

    fun addWater(amountMl: Int = 250) {
        logs[selectedDate] = selectedDay.copy(waterMl = (selectedDay.waterMl + amountMl).coerceIn(0, 5000))
        persist()
    }

    fun copyMeal(from: Meal, to: Meal) {
        val copies = MealRules.copyMeal(selectedDay.entries, from, to, nextId)
        if (copies.isEmpty()) return
        nextId += copies.size
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries + copies)
        persist()
    }

    fun copyYesterday() {
        val previous = logs[selectedDate.minusDays(1)] ?: return
        val copies = MealRules.copyDay(previous.entries, nextId)
        if (copies.isEmpty()) return
        nextId += copies.size
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries + copies)
        persist()
    }

    fun addCustomFood(food: FoodItem) {
        customFoods.add(food)
        persist()
    }

    fun toggleFavorite(id: Int) {
        if (id in favorites) favorites.remove(id) else favorites.add(id)
        persist()
    }

    fun updateProfile(value: BodyProfile) {
        profile = value
        profileVersions = DailyPlans.applyProfileVersion(profileVersions, value, LocalDate.now(), value)
        persist()
    }

    fun updateActivityForDate(value: DailyActivityPlan) {
        activityPlans = DailyPlans.applyActivityPlan(activityPlans, DailyActivityPlan(), selectedDate, value)
        persist()
    }

    fun saveMealTemplate(template: MealTemplate) {
        val updated = MealTemplateRules.upsert(mealTemplates.toList(), template.copy(updatedAt = LocalDate.now()))
        mealTemplates.clear()
        mealTemplates.addAll(updated)
        persist()
    }

    fun removeMealTemplate(id: Long) {
        val updated = MealTemplateRules.remove(mealTemplates.toList(), id)
        mealTemplates.clear()
        mealTemplates.addAll(updated)
        persist()
    }

    fun applyMealTemplate(template: MealTemplate) {
        val logsToAdd = MealTemplateRules.toLogs(template, selectedDate, nextId)
        if (logsToAdd.isEmpty()) return
        nextId += logsToAdd.size
        logs[selectedDate] = selectedDay.copy(entries = selectedDay.entries + logsToAdd)
        val updated = MealTemplateRules.markUsed(mealTemplates.toList(), template.id, selectedDate)
        mealTemplates.clear()
        mealTemplates.addAll(updated)
        persist()
    }

    fun saveBodyMeasurement(value: BodyMeasurement) {
        val updated = BodyMeasurementRules.upsert(bodyMeasurements.toList(), value)
        bodyMeasurements.clear()
        bodyMeasurements.addAll(updated)
        persist()
    }

    fun configureAdaptiveEnergy(value: AdaptiveEnergySettings) {
        adaptiveSettings = value
        persist()
    }

    fun adaptiveEvaluation(date: LocalDate = selectedDate): AdaptiveEnergyEvaluation =
        AdaptiveEnergyEngine.evaluate(
            enabled = adaptiveSettings.enabled,
            effectiveFrom = adaptiveSettings.effectiveFrom,
            date = date,
            profile = DailyPlans.profileForDate(profile, profileVersions, activityPlans, date),
            plan = DailyPlans.activityPlanOn(activityPlans, DailyActivityPlan(), date),
            measurements = bodyMeasurements.toList(),
            dailyTotals = logs.mapNotNull { (day, value) ->
                value.takeIf { it.entries.isNotEmpty() }?.let { day to DailyNutrition.total(it.entries.map(::snapshot)) }
            }.toMap(),
        )

    fun snapshot(log: FoodLog): NutritionSnapshot {
        return NutritionEngine.recordSnapshot(log.food, log.grams, log.oil, log.salt)
    }

    fun total(date: LocalDate = selectedDate): NutritionSnapshot =
        DailyNutrition.total(logs[date]?.entries.orEmpty().map(::snapshot))

    fun totalsForDates(dates: List<LocalDate>): Map<LocalDate, NutritionSnapshot> =
        dates.mapNotNull { date -> logs[date]?.takeIf { it.entries.isNotEmpty() }?.let { date to DailyNutrition.total(it.entries.map(::snapshot)) } }.toMap()

    fun hasData(date: LocalDate) = logs[date]?.entries.orEmpty().isNotEmpty()
    fun waterOn(date: LocalDate) = logs[date]?.waterMl ?: 0
    fun entriesOn(date: LocalDate) = logs[date]?.entries.orEmpty()
    fun entriesAllDates(): List<FoodLog> = logs.values.flatMap { it.entries }
    fun restoreSnapshotFiles(): List<File> = store.restoreSnapshots()

    fun exportBackup() = store.exportBackup(persistedState())
    fun exportShareFile(): File = store.writeNamedBackup(exportBackup())
    fun automaticBackupFiles(): List<AutoBackupFile> = store.automaticBackupFiles()
    fun ensureAutomaticBackup(): AutoBackupFile? = store.ensureDailyAutomaticBackup(persistedState())

    fun toggleRecommendationIgnored(name: String) {
        if (name in ignoredRecommendationNames) ignoredRecommendationNames.remove(name) else ignoredRecommendationNames.add(name)
        persist()
    }

    fun importBackup(json: String): Boolean = runCatching {
        val imported = store.importBackup(json)
        AutoBackupRules.snapshotBeforeRestore(persistedState())?.let { store.saveRestoreSnapshot(it.json) }
        logs.clear()
        logs.putAll(imported.days)
        profile = imported.profile
        profileVersions = imported.profileVersions
        activityPlans = imported.activityPlans
        mealTemplates.clear()
        mealTemplates.addAll(imported.templates)
        bodyMeasurements.clear()
        bodyMeasurements.addAll(imported.measurements)
        adaptiveSettings = imported.adaptiveSettings
        customFoods.clear()
        customFoods.addAll(imported.customFoods)
        favorites.clear()
        favorites.addAll(imported.favorites)
        recentFoodIds.clear()
        recentFoodIds.addAll(imported.recentFoodIds)
        ignoredRecommendationNames.clear()
        ignoredRecommendationNames.addAll(imported.ignoredRecommendationNames)
        nextId = (logs.values.flatMap { it.entries }.maxOfOrNull { it.id } ?: 0L) + 1L
        persist()
        true
    }.getOrElse {
        importError = "备份格式无效或包含不支持的数值。当前记录没有被修改。"
        false
    }

    fun mergeBackup(json: String): Boolean = runCatching {
        val imported = store.importBackup(json)
        AutoBackupRules.snapshotBeforeRestore(persistedState())?.let { store.saveRestoreSnapshot(it.json) }
        val merged = StateMergeRules.merge(persistedState(), imported)
        applyImportedState(merged)
        importNotice = "已合并 ${imported.days.size} 天记录；现有数据未清空"
        true
    }.getOrElse {
        importError = "备份格式无效或包含不支持的数值。当前记录没有被修改。"
        false
    }

    private fun applyImportedState(value: PersistedState) {
        logs.clear()
        logs.putAll(value.days)
        profile = value.profile
        profileVersions = value.profileVersions
        activityPlans = value.activityPlans
        mealTemplates.clear()
        mealTemplates.addAll(value.templates)
        bodyMeasurements.clear()
        bodyMeasurements.addAll(value.measurements)
        adaptiveSettings = value.adaptiveSettings
        customFoods.clear()
        customFoods.addAll(value.customFoods)
        favorites.clear()
        favorites.addAll(value.favorites)
        recentFoodIds.clear()
        recentFoodIds.addAll(value.recentFoodIds)
        ignoredRecommendationNames.clear()
        ignoredRecommendationNames.addAll(value.ignoredRecommendationNames)
        nextId = (logs.values.flatMap { it.entries }.maxOfOrNull { it.id } ?: 0L) + 1L
        persist()
    }

    private fun persist() {
        store.save(persistedState())
        runCatching { store.ensureDailyAutomaticBackup(persistedState()) }
    }

    private fun persistedState() = PersistedState(
        days = logs.toMap(),
        profile = profile,
        profileVersions = profileVersions,
        activityPlans = activityPlans,
        customFoods = customFoods.toList(),
        favorites = favorites.toSet(),
        recentFoodIds = recentFoodIds.toList(),
        ignoredRecommendationNames = ignoredRecommendationNames.toList(),
        templates = mealTemplates.toList(),
        measurements = bodyMeasurements.toList(),
        adaptiveSettings = adaptiveSettings,
    )

    private fun noteRecent(id: Int) {
        recentFoodIds.remove(id)
        recentFoodIds.add(0, id)
        while (recentFoodIds.size > 30) recentFoodIds.removeLast()
    }
}

@Composable
private fun TodayScreen(model: AppState, modifier: Modifier) {
    val total = model.total()
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DayHeader(model) }
        item { EnergyOverview(total, model.energyTarget) }
        item {
            val targets = NutritionEngine.displayTargets(model.energyTarget, model.energyTarget.centerKcal, model.activeProfile.sex, model.activeProfile.age)
            TodayFocusCard(total, targets)
        }
        item { WaterTile(model.selectedDay.waterMl, model::addWater) }
        item { QuickFoodStrip(model) }
        item {
            MealTemplateSection(model) {
                val entries = model.selectedDay.entries
                if (entries.isNotEmpty()) {
                    val name = "${entries.first().meal.label}组合 ${model.mealTemplates.size + 1}"
                    model.saveMealTemplate(
                        MealTemplate(
                            id = (model.mealTemplates.maxOfOrNull { it.id } ?: 0L) + 1L,
                            name = name,
                            meal = entries.first().meal,
                            items = entries.map { MealTemplateItem(it.food, it.grams, it.oil, it.salt) },
                            createdAt = LocalDate.now(),
                            updatedAt = LocalDate.now(),
                        ),
                    )
                    model.shareNotice = "已保存常用组合：$name"
                } else {
                    model.shareNotice = "当天还没有食物记录，先添加食物再保存组合。"
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Meal.entries.filter { it != Meal.SNACK }.forEach { meal ->
                    OutlinedButton(
                        onClick = { model.preferredMeal = meal; model.adding = true; model.quickAddFood = null },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp),
                    ) { Text("+${meal.label}", color = Mint, fontSize = 11.sp) }
                }
            }
        }
        item {
            val targets = NutritionEngine.displayTargets(model.energyTarget, model.energyTarget.centerKcal, model.activeProfile.sex, model.activeProfile.age)
            DailyNutritionPanel(total, targets)
            if (total.addedSugarG == null) {
                Text(
                    "添加糖数据不足：部分菜品尚无可靠添加糖配方，暂不判断添加糖上限；总糖仍按原值统计。",
                    color = Muted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("今日记录", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { model.copyYesterday() }) { Text("复制昨日", color = Mint, fontSize = 11.sp) }
            }
        }
        Meal.entries.forEach { meal ->
            item { MealSection(meal, model.selectedDay.entries.filter { it.meal == meal }, model) }
        }
        item { ActivityEnergyCard(model) }
        item { SmartRecommendationBoard(model, total) }
        item { Text("营养值为可解释估算，不替代医生或注册营养师建议。", color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun DayHeader(model: AppState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("SMARTNUTRIENT", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text(if (model.selectedDate == LocalDate.now()) "今天，吃得明白。" else "回看这一天。", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(model.selectedDate.format(DateTimeFormatter.ofPattern("M月d日 EEEE")), color = Muted, fontSize = 13.sp)
        }
        Box(Modifier.size(42.dp).background(RaisedSurface, CircleShape).border(1.dp, Hairline, CircleShape), contentAlignment = Alignment.Center) {
            Text("N", color = Mint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TodayFocusCard(total: NutritionSnapshot, targets: NutritionTargets) {
    val items = remember(total, targets) { TodayFocusRules.items(total, targets) }
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(18.dp))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("今日重点", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(if (items.any { it.alert }) "先看上限" else "优先补缺口", color = if (items.any { it.alert }) Alert else Mint, fontSize = 10.sp)
        }
        if (items.isEmpty()) {
            Text("当前主要营养均在合理范围内，保持节奏。", color = Muted, fontSize = 12.sp)
        } else {
            items.forEach { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.label, color = if (item.alert) Alert else Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(item.detail, color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun EnergyOverview(total: NutritionSnapshot, target: EnergyTarget) {
    val fraction = (total.caloriesKcal / target.centerKcal).toFloat().coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(RaisedSurface, DeepSurface)), RoundedCornerShape(28.dp)).border(1.dp, Hairline, RoundedCornerShape(28.dp)).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(142.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(Muted.copy(alpha = .20f), -90f, 360f, false, style = Stroke(11.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Mint, -90f, 360f * fraction, false, style = Stroke(11.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(total.caloriesKcal.toInt().toString(), color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("kcal", color = Muted, fontSize = 11.sp)
                    Text("目标 ${target.centerKcal.toInt()}", color = Mint, fontSize = 11.sp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("今日能量", color = Muted, fontSize = 12.sp)
                Text(if (fraction < .35f) "还早，慢慢补充" else if (fraction < .8f) "节奏不错" else "接近目标", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Mint, trackColor = Hairline)
                Text("建议区间 ${target.minKcal.toInt()}–${target.maxKcal.toInt()} kcal", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EnergyRangeTile(total: NutritionSnapshot, target: EnergyTarget) {
    val fraction = (total.caloriesKcal / target.centerKcal).toFloat().coerceIn(0f, 1f)
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(17.dp)).border(1.dp, Hairline, RoundedCornerShape(17.dp)).padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("推荐热量区间", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("${target.minKcal.toInt()}–${target.maxKcal.toInt()} kcal", color = Mint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Mint, trackColor = Hairline)
        Text("今日已摄入 ${total.caloriesKcal.toInt()} kcal · 目标中心 ${target.centerKcal.toInt()} kcal", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun NutrientGrid(total: NutritionSnapshot, energy: EnergyTarget, profile: BodyProfile) {
    val targets = NutritionEngine.targets(energy.centerKcal, profile.sex, profile.age)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile("蛋白质", total.proteinG, "g", targets.proteinMaxG, Modifier.weight(1f))
            MetricTile("碳水", total.carbohydrateG, "g", targets.carbohydrateMaxG, Modifier.weight(1f))
            MetricTile("钠", total.sodiumMg, "mg", targets.sodiumMaxMg, Modifier.weight(1f), total.sodiumMg > targets.sodiumMaxMg)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile("膳食纤维", total.fiberG, "g", targets.fiberMinG, Modifier.weight(1f), total.fiberG >= targets.fiberMinG)
            MetricTile("脂肪", total.fatG, "g", targets.fatMaxG, Modifier.weight(1f))
            MetricTile("胆固醇", total.cholesterolMg, "mg", targets.cholesterolMaxMg, Modifier.weight(1f), total.cholesterolMg > targets.cholesterolMaxMg)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            MetricTile("饱和脂肪", total.saturatedFatG, "g", targets.fatMaxG * 0.35, Modifier.weight(1f))
            MetricTile("糖", total.sugarG, "g", targets.carbohydrateMaxG * 0.15, Modifier.weight(1f))
            MetricTile("钾", total.potassiumMg, "mg", 3000.0, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(label: String, value: Double, unit: String, target: Double, modifier: Modifier, alert: Boolean = false) {
    val highlight = if (alert) Alert else Mint
    Column(modifier.background(DeepSurface, RoundedCornerShape(16.dp)).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Muted, fontSize = 11.sp, maxLines = 1)
        Text(if (value >= 100) value.toInt().toString() else "%.1f".format(value), color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(unit, color = highlight, fontSize = 10.sp)
        LinearProgressIndicator(progress = { (value / target).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(3.dp), color = highlight, trackColor = Hairline)
    }
}

@Composable
private fun NutritionDetailDialog(model: AppState, log: FoodLog) {
    val n = model.snapshot(log)
    Dialog(onDismissRequest = { model.viewingNutritionLog = null }) {
        Surface(
            color = Night,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.92f).border(1.dp, Hairline, RoundedCornerShape(24.dp)),
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 19.dp, end = 10.dp, top = 14.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            FoodKindBadge(log.food)
                            Text(log.food.name, color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        val contextLine = if (log.id < 0L) {
                            "100 g · ${log.food.basis} · ${log.food.method}"
                        } else {
                            "${log.grams.toInt()} g · ${log.meal.label} · ${log.food.basis} · ${log.food.method}"
                        }
                        Text(contextLine, color = Muted, fontSize = 12.sp)
                    }
                    TextButton(onClick = { model.viewingNutritionLog = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("关闭", color = Muted)
                    }
                }
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 19.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NutritionValuesPanel(n, "本份完整营养", Modifier.fillMaxWidth())
                    HorizontalDivider(color = Hairline)
                    Text("油量 ${log.oil.label} · 盐量 ${log.salt.label}", color = Muted, fontSize = 11.sp)
                    if (log.food.ingredients.isNotEmpty()) {
                        Text("配方：${log.food.ingredients.joinToString("、")}", color = Muted, fontSize = 11.sp)
                    }
                    Text("数据质量：${log.food.dataQuality.label} · 来源：${log.food.source}", color = Muted, fontSize = 10.sp)
                    log.food.includesSeasoningPack?.let { included ->
                        Text(
                            if (included) "口径：包含调料包整套" else "口径：仅面饼/主体，不含调料包",
                            color = if (included) Alert else Mint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
@Composable
internal fun NutritionRows(rows: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        rows.forEachIndexed { index, (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = Muted, fontSize = 13.sp)
                Text(value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            if (index != rows.lastIndex) HorizontalDivider(color = Hairline.copy(alpha = .55f))
        }
    }
}

@Composable
private fun WaterTile(water: Int, onAdd: (Int) -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(19.dp))
            .border(1.dp, Hairline, RoundedCornerShape(19.dp)).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("饮水", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("$water / 2000 ml", color = Muted, fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = { (water / 2000f).coerceIn(0f, 1f) },
                modifier = Modifier.width(96.dp).height(5.dp),
                color = Mint,
                trackColor = Hairline,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(100, 250, 500).forEach { amount ->
                OutlinedButton(
                    onClick = { onAdd(amount) },
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) { Text("+$amount", color = Mint, fontSize = 12.sp) }
            }
            TextButton(
                onClick = { onAdd(-100) },
                enabled = water > 0,
                modifier = Modifier.heightIn(min = 44.dp),
            ) { Text("−100", color = if (water > 0) Muted else Hairline, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun QuickFoodStrip(model: AppState) {
    val suggestions = remember(model.recentFoodIds.toList(), model.favorites.toList(), model.entriesAllDates().size) {
        QuickFoodRules.suggestions(
            foods = model.allFoods,
            recentIds = model.recentFoodIds.toList(),
            allEntries = model.entriesAllDates(),
            favorites = model.favorites.toSet(),
            limit = 8,
        )
    }
    if (suggestions.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("快速添加", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("点击后确认分量", color = Muted, fontSize = 10.sp)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions, key = { it.food.id }) { suggestion ->
                Column(
                    Modifier.width(138.dp)
                        .background(RaisedSurface, RoundedCornerShape(15.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(15.dp))
                        .clickable {
                            model.editing = null
                            model.preferredMeal = Meal.DINNER
                            model.adding = true
                            model.quickAddFood = suggestion.food
                        }
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        FoodKindBadge(suggestion.food)
                        Text(suggestion.label, color = Lime, fontSize = 9.sp)
                    }
                    Text(suggestion.food.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${suggestion.food.nutrients.caloriesKcal.toInt()} kcal / 100g", color = Muted, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun MealSection(meal: Meal, entries: List<FoodLog>, model: AppState) {
    val mealCalories = entries.sumOf { model.snapshot(it).caloriesKcal }
    Column(Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(21.dp)).border(1.dp, Hairline, RoundedCornerShape(21.dp)).padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(if (entries.isEmpty()) Muted else Mint, CircleShape))
                Text(meal.label, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("${entries.size} 项", color = Muted, fontSize = 11.sp)
            }
            Text("${mealCalories.toInt()} kcal", color = if (entries.isEmpty()) Muted else Mint, fontSize = 12.sp)
        }
        if (entries.isEmpty()) Text("还没有记录", color = Muted, fontSize = 13.sp)
        entries.forEach { log ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        FoodKindBadge(log.food)
                        Text(log.food.name, color = Ink, fontSize = 14.sp, maxLines = 1)
                    }
                    Text("${log.grams.toInt()} g · ${log.food.basis} · ${log.oil.label} · ${log.salt.label}", color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                TextButton(onClick = { model.viewingNutritionLog = log }, contentPadding = PaddingValues(horizontal = 6.dp), modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("${model.snapshot(log).caloriesKcal.toInt()} kcal · 详情", color = Ink, fontSize = 12.sp)
                }
                TextButton(onClick = { model.editing = log }, contentPadding = PaddingValues(horizontal = 7.dp), modifier = Modifier.heightIn(min = 48.dp)) { Text("编辑", color = Mint, fontSize = 14.sp) }
                TextButton(onClick = { model.confirmAction = "remove:${log.id}" }, contentPadding = PaddingValues(horizontal = 7.dp), modifier = Modifier.heightIn(min = 48.dp)) { Text("×", color = Muted, fontSize = 21.sp) }
            }
            if (log.id != entries.last().id) HorizontalDivider(color = Hairline)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = { model.preferredMeal = meal; model.adding = true }, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.heightIn(min = 48.dp)) { Text("+ 添加到${meal.label}", color = Mint, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            if (entries.isNotEmpty()) TextButton(onClick = { model.copySourceMeal = meal }, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.heightIn(min = 48.dp)) { Text("复制餐次", color = Muted, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun SmartRecommendationBoard(model: AppState, total: NutritionSnapshot) {
    val entries = model.selectedDay.entries
    val ready = RecommendationEngine.canCalculate(entries)
    val targets = NutritionEngine.displayTargets(
        model.energyTarget,
        model.energyTarget.centerKcal,
        model.activeProfile.sex,
        model.activeProfile.age,
    )
    val ignored = model.ignoredRecommendationNames.toList()
    val recommendations = remember(entries, targets, model.allFoods, ignored) {
        if (ready) {
            RecommendationEngine.recommend(
                entries,
                targets,
                model.allFoods,
                excludedNames = ignored.toSet(),
            )
        } else {
            emptyList()
        }
    }

    Column(
        Modifier.fillMaxWidth()
            .background(Brush.linearGradient(listOf(RaisedSurface, DeepSurface)), RoundedCornerShape(23.dp))
            .border(1.dp, Mint.copy(alpha = .34f), RoundedCornerShape(23.dp))
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("智能推荐", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (ready) "基于今日营养缺口，仅推荐食材与水果" else "早餐和午餐都记录后开启计算",
                    color = if (ready) Mint else Muted,
                    fontSize = 11.sp,
                )
            }
            Text(
                if (ready) "10 项营养" else "等待记录",
                color = if (ready) Lime else Muted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background((if (ready) Lime else Muted).copy(alpha = .10f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        if (!ready) {
            Text(
                "请先添加早餐和午餐。推荐会避开成品菜，只从食材和水果中挑选，并给出建议分量。",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        } else if (recommendations.isEmpty()) {
            Text("暂无可推荐食物。若你已隐藏多个推荐，可在下方恢复。", color = Muted, fontSize = 12.sp)
        } else {
            recommendations.forEachIndexed { index, recommendation ->
                RecommendationRow(
                    rank = index + 1,
                    recommendation = recommendation,
                    onAdd = {
                        model.add(recommendation.food, recommendation.suggestedGrams, Meal.DINNER, OilLevel.ZERO, SaltLevel.ZERO)
                    },
                    onIgnore = { model.toggleRecommendationIgnored(recommendation.food.name) },
                )
                if (index != recommendations.lastIndex) HorizontalDivider(color = Hairline.copy(alpha = .65f))
            }
            Text(
                "已摄入 ${total.caloriesKcal.toInt()} kcal · 推荐值按当前记录实时计算，加入晚餐后自动更新",
                color = Muted,
                fontSize = 10.sp,
            )
        }

        if (ignored.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("已隐藏 ${ignored.size} 项", color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { ignored.forEach(model::toggleRecommendationIgnored) }) {
                    Text("恢复全部推荐", color = Mint, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(
    rank: Int,
    recommendation: NutritionRecommendation,
    onAdd: () -> Unit,
    onIgnore: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).background(Mint.copy(alpha = .13f), CircleShape)
                .border(1.dp, Mint.copy(alpha = .40f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(rank.toString(), color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                FoodKindBadge(recommendation.food)
                Text(recommendation.food.name, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(recommendation.reason, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            Text(
                "${recommendation.suggestedGrams.toInt()} g · ${recommendation.estimated.caloriesKcal.toInt()} kcal · 蛋白 ${"%.1f".format(recommendation.estimated.proteinG)} g · 纤维 ${"%.1f".format(recommendation.estimated.fiberG)} g",
                color = Mint,
                fontSize = 10.sp,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            TextButton(
                onClick = onAdd,
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier.heightIn(min = 44.dp),
            ) { Text("加入晚餐", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            TextButton(
                onClick = onIgnore,
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier.heightIn(min = 40.dp),
            ) { Text("少推荐", color = Muted, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun AddFoodDialog(model: AppState) {
    var query by rememberSaveable { mutableStateOf("") }
    var chosen by remember { mutableStateOf(model.editing?.food ?: model.quickAddFood) }
    var grams by rememberSaveable { mutableStateOf(model.editing?.grams?.toInt()?.toString() ?: "150") }
    var meal by rememberSaveable { mutableStateOf(EntryDefaults.meal(model.editing, model.preferredMeal)) }
    var oil by rememberSaveable { mutableStateOf(model.editing?.oil ?: OilLevel.ZERO) }
    var salt by rememberSaveable { mutableStateOf(model.editing?.salt ?: SaltLevel.ZERO) }
    var categoryIndex by rememberSaveable { mutableIntStateOf(0) }
    val entryCategories = listOf("全部", "成品菜", "食材", "调料", "水果", "饮品", "补剂")
    val selectedCategory = entryCategories[categoryIndex]
    val categoryFoods = model.allFoods.filter { food -> selectedCategory == "全部" || FoodKind.of(food).label == selectedCategory }
    val searchResults = FoodSearch.search(categoryFoods, query, model.favorites.toSet(), model.recentFoodIds)
    Dialog(onDismissRequest = {
        model.adding = false
        model.editing = null
        model.quickAddFood = null
    }) {
        Surface(
            color = Night,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.94f).border(1.dp, Hairline, RoundedCornerShape(25.dp)),
        ) {
            Column(Modifier.fillMaxSize().imePadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (chosen == null) "添加食物" else chosen!!.name, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(if (chosen == null) "菜名、食材、调料、别名、拼音首字母" else "${chosen!!.basis} · ${chosen!!.category} · ${chosen!!.method}", color = Muted, fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = {
                            if (chosen == null) {
                                model.adding = false
                                model.editing = null
                            } else {
                                chosen = null
                            }
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("返回", color = Muted, fontSize = 15.sp) }
                }
                if (chosen == null) {
                    Column(
                        Modifier.weight(1f).padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("试试：米饭、大蒜、生抽、fqcd", color = Muted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Hairline, cursorColor = Mint),
                        )
                        Text("食物类别", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        LargeChoiceRow(entryCategories, categoryIndex) { categoryIndex = it }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { model.addingCustom = true },
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.heightIn(min = 50.dp),
                            ) { Text("+ 自定义食物", color = Muted, fontSize = 13.sp) }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 14.dp),
                        ) {
                            items(searchResults, key = { it.id }) { food ->
                                FoodResultRow(food, model, onChoose = { chosen = food })
                            }
                        }
                    }
                } else {
                    val amount = grams.toDoubleOrNull() ?: 0.0
                    val canSubmit = EntryFormRules.canSubmit(grams)
                    val preview = NutritionEngine.recordSnapshot(chosen!!, amount, oil, salt)
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("餐次", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        LargeChoiceRow(Meal.entries.map { it.label }, Meal.entries.indexOf(meal)) { meal = Meal.entries[it] }
                        OutlinedTextField(
                            value = grams,
                            onValueChange = { grams = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("重量（克）", color = Muted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Hairline,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                            ),
                        )
                        Text("油量", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        LargeChoiceRow(OilLevel.entries.map { it.label }, OilLevel.entries.indexOf(oil)) { oil = OilLevel.entries[it] }
                        Text("盐量", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        LargeChoiceRow(SaltLevel.entries.map { it.label }, SaltLevel.entries.indexOf(salt)) { salt = SaltLevel.entries[it] }
                        Text("常用分量", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        LargeChoiceRow(
                            ServingPresetRules.forFood(chosen!!).map { "${it.toInt()} g" },
                            ServingPresetRules.forFood(chosen!!).indexOf(amount),
                        ) { grams = ServingPresetRules.forFood(chosen!!)[it].toInt().toString() }
                        NutritionValuesPanel(preview, "本份完整营养", Modifier.fillMaxWidth())
                        if (chosen!!.basis == "每份") {
                            Text(
                                "这是一份成品估算：请按包装标示重量填写，不要套用每 100g 数值。",
                                color = Muted,
                                fontSize = 11.sp,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Column(
                        Modifier.fillMaxWidth().background(Night).padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        if (!canSubmit) {
                            Text("请输入大于 0 克的重量后再提交", color = Alert, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val edit = model.editing
                                if (edit == null) {
                                    model.add(chosen!!, amount, meal, oil, salt)
                                } else {
                                    model.update(edit.id, amount, meal, oil, salt)
                                }
                                model.adding = false
                                model.editing = null
                            },
                            enabled = canSubmit,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(15.dp),
                        ) {
                            Text(
                                if (model.editing == null) "加入${meal.label}" else "保存修改",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodResultRow(food: FoodItem, model: AppState, onChoose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(15.dp)).border(1.dp, Hairline, RoundedCornerShape(15.dp)).clickable {
            model.viewingNutritionLog = FoodLog(-1L, food, 100.0, Meal.BREAKFAST, OilLevel.ZERO, SaltLevel.ZERO)
        }.padding(13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                FoodKindBadge(food)
                Text(food.name, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            val unit = if (food.basis == "每份") "kcal/份" else "kcal/100g"
            Text("${food.category} · ${food.basis} · ${food.method} · ${food.nutrients.caloriesKcal.toInt()} $unit · ${food.confidence}", color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        TextButton(onClick = { model.toggleFavorite(food.id) }) { Text(if (food.id in model.favorites) "★" else "☆", color = Alert, fontSize = 18.sp) }
        TextButton(onClick = onChoose, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.heightIn(min = 48.dp)) {
            Text("选择", color = Mint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FreeformRecipeDialog(model: AppState) {
    var recipeName by rememberSaveable { mutableStateOf("") }
    var ingredientQuery by rememberSaveable { mutableStateOf("") }
    var seasoningQuery by rememberSaveable { mutableStateOf("") }
    var grams by rememberSaveable { mutableStateOf("100") }
    var seasoningGrams by rememberSaveable { mutableStateOf("5") }
    var selectedIngredientId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedSeasoningId by rememberSaveable { mutableStateOf<String?>(null) }
    var meal by rememberSaveable { mutableStateOf(model.preferredMeal) }
    var ingredients by remember { mutableStateOf(emptyList<FreeformIngredient>()) }
    var seasonings by remember { mutableStateOf(emptyList<FreeformSeasoning>()) }
    var mode by rememberSaveable { mutableStateOf(FreeformMode.INGREDIENTS) }
    var saveNotice by rememberSaveable { mutableStateOf<String?>(null) }

    val ingredientResults = FoodSearch.search(
        RawIngredientCatalog.items,
        ingredientQuery,
        model.favorites.toSet(),
        model.recentFoodIds,
    ).take(40)
    val seasoningResults = SeasoningCatalog.search(seasoningQuery).take(40)
    val selectedIngredient = ingredientResults.firstOrNull { it.id == selectedIngredientId }
    val selectedSeasoning = seasoningResults.firstOrNull { it.id == selectedSeasoningId }
        ?: SeasoningCatalog.items.firstOrNull { it.id == selectedSeasoningId }
    val recipe = FreeformRecipe(recipeName, ingredients, seasonings)
    val total = recipe.totalNutrition()
    val per100 = recipe.per100gNutrition()

    Dialog(onDismissRequest = { model.addingFreeform = false }) {
        Surface(
            color = Night,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.94f).border(1.dp, Hairline, RoundedCornerShape(25.dp)),
        ) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("自由搭配", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("每种食材按生重 / 实际克数累加", color = Muted, fontSize = 12.sp)
                    }
                    TextButton(onClick = { model.addingFreeform = false }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("关闭", color = Muted, fontSize = 15.sp)
                    }
                }
                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    label = { Text("这道自制菜的名称", color = Muted) },
                    placeholder = { Text("例如：鸡胸黄瓜碗", color = Muted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                    colors = freeformFieldColors(),
                )
                LargeChoiceRow(listOf("1 加食材", "2 加调料"), mode.ordinal) { mode = FreeformMode.entries[it] }
                if (saveNotice != null) {
                    Text(
                        saveNotice!!,
                        color = Mint,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(12.dp)).padding(10.dp),
                    )
                }
                if (mode == FreeformMode.INGREDIENTS) {
                    FreeformIngredientPicker(
                        query = ingredientQuery,
                        onQueryChange = { ingredientQuery = it; selectedIngredientId = null },
                        grams = grams,
                        onGramsChange = { grams = it.filter(Char::isDigit).take(5) },
                        results = ingredientResults,
                        selectedId = selectedIngredientId,
                        onSelect = { selectedIngredientId = it.id },
                        onAdd = {
                            val food = selectedIngredient ?: return@FreeformIngredientPicker
                            val amount = grams.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                ingredients = ingredients + FreeformIngredient.fromFood(food, amount)
                                selectedIngredientId = null
                                ingredientQuery = ""
                                grams = "100"
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    FreeformSeasoningPicker(
                        query = seasoningQuery,
                        onQueryChange = { seasoningQuery = it; selectedSeasoningId = null },
                        grams = seasoningGrams,
                        onGramsChange = {
                            seasoningGrams = it.filter { c -> c.isDigit() || c == '.' }.take(6)
                        },
                        selectedSeasoning = selectedSeasoning,
                        results = seasoningResults,
                        selectedId = selectedSeasoningId,
                        onSelect = { selectedSeasoningId = it.id },
                        onAdd = {
                            val seasoning = selectedSeasoning ?: return@FreeformSeasoningPicker
                            val amount = seasoningGrams.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                seasonings = seasonings + FreeformSeasoning(seasoning, amount)
                                selectedSeasoningId = null
                                seasoningQuery = ""
                                seasoningGrams = "5"
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Column(
                    Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(16.dp)).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("成品合计 %.0f g".format(recipe.totalWeightG), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "%.0f kcal · 蛋白 %.1f g · 脂肪 %.1f g · 碳水 %.1f g · 钠 %.0f mg".format(
                            total.caloriesKcal, total.proteinG, total.fatG, total.carbohydrateG, total.sodiumMg,
                        ),
                        color = Mint,
                        fontSize = 12.sp,
                    )
                    Text(
                        "每 100g：%.0f kcal · 纤维 %.1f g · 糖 %.1f g · 饱和脂肪 %.1f g".format(
                            per100.caloriesKcal, per100.fiberG, per100.sugarG, per100.saturatedFatG,
                        ),
                        color = Muted,
                        fontSize = 11.sp,
                    )
                }
                Text("餐次", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                LargeChoiceRow(Meal.entries.map { it.label }, Meal.entries.indexOf(meal)) { meal = Meal.entries[it] }
                if (ingredients.isNotEmpty() || seasonings.isNotEmpty()) {
                    RecipeComposition(
                        ingredients = ingredients,
                        seasonings = seasonings,
                        onRemoveIngredient = { ingredients = ingredients.filterIndexed { index, _ -> index != it } },
                        onRemoveSeasoning = { seasonings = seasonings.filterIndexed { index, _ -> index != it } },
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(
                        onClick = {
                            val usedIds = (model.allFoods.map { it.id } + model.entriesAllDates().map { it.food.id }).toSet()
                            val id = FoodIdRules.nextCustomId(recipe.name, per100.caloriesKcal, usedIds)
                            model.addCustomFood(recipe.toCustomFood(id))
                            saveNotice = "已保存为自定义菜，可在普通搜索中再次添加"
                            recipeName = ""
                            ingredients = emptyList()
                            seasonings = emptyList()
                            selectedSeasoningId = null
                        },
                        enabled = recipe.canSave(),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("仅保存", color = Mint, fontSize = 14.sp) }
                    Button(
                        onClick = {
                            val usedIds = (model.allFoods.map { it.id } + model.entriesAllDates().map { it.food.id }).toSet()
                            val id = FoodIdRules.nextCustomId(recipe.name, per100.caloriesKcal, usedIds)
                            val food = recipe.toCustomFood(id)
                            model.addCustomFood(food)
                            model.add(food, recipe.totalWeightG, meal, OilLevel.ZERO, SaltLevel.ZERO)
                            model.addingFreeform = false
                        },
                        enabled = recipe.canSave(),
                        modifier = Modifier.weight(1.35f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("记入${meal.label}", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private enum class FreeformMode { INGREDIENTS, SEASONINGS }

@Composable
private fun FreeformIngredientPicker(
    query: String,
    onQueryChange: (String) -> Unit,
    grams: String,
    onGramsChange: (String) -> Unit,
    results: List<FoodItem>,
    selectedId: Int?,
    onSelect: (FoodItem) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索食材：鸡胸、黄瓜、hg", color = Muted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = freeformFieldColors(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = grams,
                onValueChange = onGramsChange,
                label = { Text("克", color = Muted) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = freeformFieldColors(),
            )
            Button(onClick = onAdd, enabled = selectedId != null && (grams.toDoubleOrNull() ?: 0.0) > 0.0, modifier = Modifier.height(50.dp)) {
                Text("加入食材", fontSize = 14.sp)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(results, key = { it.id }) { food ->
                IngredientResultRow(food, food.id == selectedId) { onSelect(food) }
            }
        }
    }
}

@Composable
private fun FreeformSeasoningPicker(
    query: String,
    onQueryChange: (String) -> Unit,
    grams: String,
    onGramsChange: (String) -> Unit,
    selectedSeasoning: SeasoningItem?,
    results: List<SeasoningItem>,
    selectedId: String?,
    onSelect: (SeasoningItem) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索调料：生抽、薄盐、sc", color = Muted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = freeformFieldColors(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = grams,
                onValueChange = onGramsChange,
                label = { Text("调料克数", color = Muted) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = freeformFieldColors(),
            )
            Button(onClick = onAdd, enabled = selectedId != null && (grams.toDoubleOrNull() ?: 0.0) > 0.0, modifier = Modifier.height(50.dp)) {
                Text("加入调料", fontSize = 14.sp)
            }
        }
        selectedSeasoning?.let { seasoning ->
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeasoningPortionRules.commonSpoons(seasoning).forEach { spoon ->
                    item {
                        FilterChip(
                            selected = grams.toDoubleOrNull() == spoon.grams,
                            onClick = { onGramsChange(spoon.grams.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.1f".format(it) }) },
                            label = { Text("${spoon.label} · ${spoon.grams.toInt()} g", fontSize = 12.sp) },
                            modifier = Modifier.heightIn(min = 44.dp),
                        )
                    }
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(results, key = { it.id }) { seasoning ->
                SeasoningResultRow(seasoning, seasoning.id == selectedId) { onSelect(seasoning) }
            }
        }
    }
}

@Composable
private fun IngredientResultRow(food: FoodItem, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (selected) RaisedSurface else DeepSurface, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) Mint else Hairline, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                FoodKindBadge(food)
                Text(food.name, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Text("${food.category} · %.0f kcal/100g · 钠 %.0f mg".format(food.nutrients.caloriesKcal, food.nutrients.sodiumMg), color = Muted, fontSize = 11.sp)
        }
        Text(if (selected) "✓" else "+", color = Mint, fontSize = 20.sp)
    }
}

@Composable
private fun SeasoningResultRow(seasoning: SeasoningItem, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(if (selected) RaisedSurface else DeepSurface, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) Mint else Hairline, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(seasoning.name, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("${seasoning.category} · %.0f kcal/100g · 钠 %.0f mg · ${seasoning.confidence}".format(seasoning.nutrientsPer100g.caloriesKcal, seasoning.nutrientsPer100g.sodiumMg), color = Muted, fontSize = 11.sp)
        }
        Text(if (selected) "✓" else "+", color = Mint, fontSize = 20.sp)
    }
}

@Composable
private fun RecipeComposition(
    ingredients: List<FreeformIngredient>,
    seasonings: List<FreeformSeasoning>,
    onRemoveIngredient: (Int) -> Unit,
    onRemoveSeasoning: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("已加入 ${ingredients.size + seasonings.size} 项", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("左右滑动可移除", color = Muted, fontSize = 10.sp)
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEachIndexed { index, item ->
                    item(key = "ingredient-$index-${item.name}") {
                        CompositionChip("${item.name} %.0f g".format(item.grams), Ink) { onRemoveIngredient(index) }
                    }
                }
                seasonings.forEachIndexed { index, item ->
                    item(key = "seasoning-$index-${item.seasoning.id}") {
                        CompositionChip(
                            "${item.seasoning.name} ${if (item.grams % 1.0 == 0.0) item.grams.toInt() else "%.1f".format(item.grams)} g",
                            Mint,
                        ) { onRemoveSeasoning(index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompositionChip(label: String, color: Color, onRemove: () -> Unit) {
    Row(
        Modifier.background(DeepSurface, RoundedCornerShape(12.dp)).border(1.dp, Hairline, RoundedCornerShape(12.dp)).padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = color, fontSize = 12.sp)
        TextButton(onClick = onRemove, contentPadding = PaddingValues(horizontal = 7.dp), modifier = Modifier.heightIn(min = 40.dp)) {
            Text("×", color = Muted, fontSize = 17.sp)
        }
    }
}

@Composable
private fun freeformFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Mint,
    unfocusedBorderColor = Hairline,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    cursorColor = Mint,
)

@Composable
private fun LargeChoiceRow(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        items(labels.size) { index ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = {
                    Text(
                        labels[index],
                        fontSize = 15.sp,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.heightIn(min = 50.dp)
            )
        }
    }
}

@Composable
private fun CopyMealDialog(model: AppState, source: Meal) {
    Dialog(onDismissRequest = { model.copySourceMeal = null }) {
        Surface(color = Night, shape = RoundedCornerShape(23.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("把${source.label}复制到", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Meal.entries.filter { it != source }.forEach { target ->
                    Button(
                        onClick = {
                            model.copyMeal(source, target)
                            model.copySourceMeal = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RaisedSurface, contentColor = Mint)
                    ) { Text(target.label) }
                }
                TextButton(onClick = { model.copySourceMeal = null }, modifier = Modifier.align(Alignment.End)) { Text("取消", color = Muted) }
            }
        }
    }
}

@Composable
private fun RecoveryDialog(model: AppState, notice: String) {
    AlertDialog(
        onDismissRequest = { model.recoveryNoticeDismissed = true },
        title = { Text("本地数据需要处理", color = Ink) },
        text = { Text(notice, color = Muted) },
        confirmButton = { TextButton(onClick = { model.recoveryNoticeDismissed = true }) { Text("知道了", color = Mint) } }
    )
}

@Composable
private fun CustomFoodDialog(model: AppState, onDismiss: () -> Unit, onSave: (FoodItem) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var kcal by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var carbs by rememberSaveable { mutableStateOf("") }
    var fat by rememberSaveable { mutableStateOf("") }
    var sodium by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Night, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("快速自定义食物", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("请输入每 100g 数值；不确定的字段可留空。", color = Muted, fontSize = 12.sp)
                InputLine("名称", name) { name = it }
                InputLine("能量 kcal", kcal) { kcal = it }
                InputLine("蛋白质 g", protein) { protein = it }
                InputLine("碳水 g", carbs) { carbs = it }
                InputLine("脂肪 g", fat) { fat = it }
                InputLine("钠 mg", sodium) { sodium = it }
                Button(onClick = {
                    val n = FoodNutrients(kcal.toDoubleOrNull() ?: 0.0, protein.toDoubleOrNull() ?: 0.0, fat.toDoubleOrNull() ?: 0.0, 0.0, carbs.toDoubleOrNull() ?: 0.0, 0.0, 0.0, sodium.toDoubleOrNull() ?: 0.0, 0.0, 0.0)
                    val usedIds = (model.allFoods.map { it.id } + model.entriesAllDates().map { it.food.id }).toSet()
                    onSave(FoodItem(FoodIdRules.nextCustomId(name.trim(), kcal.toDouble(), usedIds), name.trim(), "", "", "自定义", "每100g", n))
                }, enabled = name.isNotBlank() && (kcal.toDoubleOrNull() ?: 0.0) > 0.0, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("保存自定义食物")
                }
            }
        }
    }
}

@Composable
private fun InputLine(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label, color = Muted) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(13.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Hairline, focusedTextColor = Ink, unfocusedTextColor = Ink))
}

@Composable
private fun CalendarScreen(model: AppState, modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("无法读取文件")
        }.onSuccess {
            pendingImport = it
            model.importError = null
            model.importNotice = null
        }.onFailure {
            model.importError = "文件读取失败，请选择 SmartNutrient JSON 备份。"
        }
    }
    val monthStart = model.selectedDate.withDayOfMonth(1)
    val daysInMonth = model.selectedDate.lengthOfMonth()
    val offset = monthStart.dayOfWeek.value - 1
    val total = model.total()
    val week = WeeklyAnalysis.previousSevenDays(model.selectedDate)
    val totals = model.totalsForDates(week)
    val deviation = WeeklyAnalysis.deviationSummary(week, totals) { date ->
            val profile = DailyPlans.profileForDate(model.profile, model.profileVersions, model.activityPlans, date)
            val energy = model.energyTargetForDate(date)
            val target = NutritionEngine.displayTargets(energy, energy.centerKcal, profile.sex, profile.age)
        target
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("饮食日历", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("每天一点记录，形成可回看的趋势。", color = Muted, fontSize = 13.sp)
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }, modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp)) { Text("导入日历", color = Mint, fontSize = 13.sp) }
        }
        Box(Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(22.dp)).border(1.dp, Hairline, RoundedCornerShape(22.dp)).padding(15.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { model.selectedDate = CalendarRules.previousMonth(model.selectedDate) }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("‹", color = Mint, fontSize = 22.sp) }
                        Text(model.selectedDate.format(DateTimeFormatter.ofPattern("yyyy年 M月")), color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { model.selectedDate = CalendarRules.nextMonth(model.selectedDate) }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("›", color = Mint, fontSize = 22.sp) }
                    }
                    Text("${model.selectedDay.entries.size} 项", color = Mint, fontSize = 11.sp)
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                        Text(it, color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                repeat(6) { weekIndex ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { weekday ->
                            val day = weekIndex * 7 + weekday - offset + 1
                            if (day in 1..daysInMonth) {
                                val date = monthStart.withDayOfMonth(day)
                                val active = date == model.selectedDate
                                Column(Modifier.weight(1f).padding(2.dp).background(if (active) Mint else Color.Transparent, RoundedCornerShape(9.dp)).clickable { model.selectedDate = date }.padding(vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(day.toString(), color = if (active) Night else Ink, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                    Box(Modifier.size(3.dp).background(if (model.hasData(date)) (if (active) Night else Mint) else Color.Transparent, CircleShape))
                                }
                            } else Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        CalendarDayDetail(model, total)
        Box(Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(20.dp)).border(1.dp, Hairline, RoundedCornerShape(20.dp)).padding(17.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("近 7 天总结", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("${deviation.periodStart} 至 ${deviation.periodEnd} · 已填 ${deviation.filledDays}/7 天", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("计算依据：汇总填写日的实际摄入，再与同期每天的推荐下限、上限总和比较。", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
                WeeklyDeviationPanel(deviation)
                model.importNotice?.let { Text(it, color = Mint, fontSize = 12.sp) }
                model.importError?.let { Text(it, color = Danger, fontSize = 12.sp) }
            }
        }
        Button(onClick = { model.selectedDate = LocalDate.now() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = RaisedSurface, contentColor = Mint)) {
            Text("回到今天")
        }
        if (pendingImport != null) {
            ImportConfirmDialog(
                onMerge = { if (model.mergeBackup(pendingImport!!)) pendingImport = null },
                onReplace = {
                    if (model.importBackup(pendingImport!!)) {
                        pendingImport = null
                        model.importNotice = "已用备份替换当前记录"
                    }
                },
                onCancel = { pendingImport = null },
            )
        }
    }
}

@Composable
private fun ProfileScreen(model: AppState, modifier: Modifier) {
    var trendDays by rememberSaveable { mutableIntStateOf(30) }
    val energy = model.energyTarget
    val breakdown = NutritionEngine.energyBreakdown(model.activeProfile)
    val adaptive = model.adaptiveEvaluation()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("身体与目标", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("让数字服务于你的生活。", color = Muted, fontSize = 13.sp)
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(RaisedSurface, DeepSurface)), RoundedCornerShape(22.dp)).border(1.dp, Hairline, RoundedCornerShape(22.dp)).padding(17.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("每日能量目标", color = Muted, fontSize = 12.sp)
                Text("${energy.centerKcal.toInt()} kcal", color = Mint, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("参考区间 ${energy.minKcal.toInt()}–${energy.maxKcal.toInt()} kcal", color = Ink, fontSize = 12.sp)
                Text(
                    "静息 ${breakdown.restingKcal.toInt()} + 额外运动 ${breakdown.manualExerciseKcal.toInt()} + 步行净耗 ${breakdown.walkingKcal.toInt()} kcal",
                    color = Muted,
                    fontSize = 11.sp,
                )
                Text(
                    if (adaptive.usedTrend) adaptive.reason else "当前使用公式目标；可在下方开启趋势修正。",
                    color = if (adaptive.usedTrend) Mint else Muted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                )
            }
        }
        Text("身体档案", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        BodyTrendSection(model, trendDays) { trendDays = it }
        EditField("年龄", model.activeProfile.age.toString()) { value -> value.toIntOrNull()?.takeIf { it in 18..100 }?.let { model.updateProfile(model.activeProfile.copy(age = it)) } }
        EditField("身高 cm", model.activeProfile.heightCm.toInt().toString()) { value -> value.toDoubleOrNull()?.takeIf { it in 100.0..250.0 }?.let { model.updateProfile(model.activeProfile.copy(heightCm = it)) } }
        EditField("体重 kg", model.activeProfile.weightKg.toInt().toString()) { value -> value.toDoubleOrNull()?.takeIf { it in 30.0..300.0 }?.let { model.updateProfile(model.activeProfile.copy(weightKg = it)) } }
        Text("身材类型（细分能量参考）", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            items(BodyType.entries) { bodyType ->
                FilterChip(
                    selected = model.activeProfile.bodyType == bodyType,
                    onClick = { model.updateProfile(model.activeProfile.copy(bodyType = bodyType)) },
                    label = { Text(bodyType.label, fontSize = 14.sp) },
                    modifier = Modifier.heightIn(min = 50.dp)
                )
            }
        }
        EditField("腰围 cm（可留空）", model.activeProfile.waistCm?.toInt()?.toString() ?: "") { value -> model.updateProfile(model.activeProfile.copy(waistCm = value.toDoubleOrNull())) }
        EditField("体脂率%（可留空）", model.activeProfile.bodyFatPercent?.toInt()?.toString() ?: "") { value -> model.updateProfile(model.activeProfile.copy(bodyFatPercent = value.toDoubleOrNull())) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = model.activeProfile.sex == Sex.MALE, onClick = { model.updateProfile(model.activeProfile.copy(sex = Sex.MALE)) }, label = { Text("男", fontSize = 15.sp) }, modifier = Modifier.weight(1f).heightIn(min = 50.dp))
            FilterChip(selected = model.activeProfile.sex == Sex.FEMALE, onClick = { model.updateProfile(model.activeProfile.copy(sex = Sex.FEMALE)) }, label = { Text("女", fontSize = 15.sp) }, modifier = Modifier.weight(1f).heightIn(min = 50.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("趋势修正目标", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("默认关闭；开启后根据体重趋势和饮食记录估算实际消耗。", color = Muted, fontSize = 10.sp)
            }
            Switch(
                checked = model.adaptiveSettings.enabled,
                onCheckedChange = { enabled ->
                    model.configureAdaptiveEnergy(
                        AdaptiveEnergySettings(
                            enabled = enabled,
                            effectiveFrom = if (enabled) LocalDate.now() else null,
                        ),
                    )
                },
            )
        }
        Text("目标", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeightGoal.entries.forEach { goal ->
                FilterChip(selected = model.activeProfile.goal == goal, onClick = { model.updateProfile(model.activeProfile.copy(goal = goal)) }, label = { Text(when (goal) { WeightGoal.LOSE -> "减脂"; WeightGoal.MAINTAIN -> "维持"; WeightGoal.GAIN -> "增重" }, fontSize = 14.sp) }, modifier = Modifier.heightIn(min = 50.dp))
            }
        }
        ProfileHistorySection(model)
        Text("备份与恢复", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("导出会生成完整 JSON 文件，可分享到微信、网盘或邮件；覆盖恢复前会自动保留当前数据快照。", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { model.backupMode = "export" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = RaisedSurface, contentColor = Mint)) { Text("备份文件", fontSize = 11.sp) }
            Button(onClick = { model.importError = null; model.backupMode = "import" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = RaisedSurface, contentColor = Mint)) { Text("恢复备份", fontSize = 11.sp) }
        }
        model.shareNotice?.let { Text(it, color = Mint, fontSize = 11.sp) }
        Text("营养建议面向普通成年人，不替代医生或注册营养师建议。", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun EditField(label: String, initial: String, onCommit: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    OutlinedTextField(
        value = value,
        onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
        label = { Text(label, color = Muted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        trailingIcon = { TextButton(onClick = { onCommit(value) }) { Text("应用", color = Mint, fontSize = 11.sp) } },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Mint, unfocusedBorderColor = Hairline, focusedTextColor = Ink, unfocusedTextColor = Ink)
    )
}

@Composable
private fun BackupDialog(model: AppState, mode: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exportMode = mode == "export"
    var text by remember(mode) { mutableStateOf(if (exportMode) model.exportBackup() else "") }
    val snapshots = model.restoreSnapshotFiles()
    val automaticBackups = model.automaticBackupFiles()
    val inspection = remember(text) { if (text.isBlank()) null else DataCodec.inspect(text) }
    val saveBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(text) } ?: error("无法写入文件")
            }.onSuccess { model.shareNotice = "备份已保存到所选位置。" }
                .onFailure { model.shareNotice = "保存失败，请改用分享或复制 JSON。" }
        }
    }
    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: error("无法读取文件")
            }.onSuccess { selected ->
                text = selected
                model.importError = null
            }.onFailure {
                model.importError = "文件读取失败，请选择 SmartNutrient JSON 备份。"
            }
        }
    }
    Dialog(onDismissRequest = { model.backupMode = null }) {
        Surface(color = Night, shape = RoundedCornerShape(23.dp), modifier = Modifier.fillMaxWidth().fillMaxHeight(.82f)) {
            Column(
                Modifier.padding(18.dp).imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(if (exportMode) "备份文件" else "恢复备份", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (exportMode) "分享到微信、网盘、邮件，或复制 JSON。" else "检查摘要后，再选择合并或覆盖恢复。",
                            color = Muted,
                            fontSize = 11.sp,
                        )
                    }
                    TextButton(onClick = { model.backupMode = null }) { Text("关闭", color = Muted) }
                }
                if (exportMode) {
                    val size = text.toByteArray(Charsets.UTF_8).size
                    Text("${AutoBackupRules.fileName()} · ${"%.1f".format(size / 1024.0)} KB", color = Mint, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                runCatching {
                                    val file = model.exportShareFile()
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "SmartNutrient 饮食营养备份")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "分享备份到"))
                                    model.shareNotice = "已生成备份文件，可在分享面板选择微信、网盘或邮件。"
                                }.onFailure {
                                    model.shareNotice = "分享失败，请改用复制 JSON 保存。"
                                }
                            },
                            modifier = Modifier.weight(1.35f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("分享备份文件", fontWeight = FontWeight.Bold) }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("SmartNutrient 备份", text))
                                model.shareNotice = "备份 JSON 已复制到剪贴板。"
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("复制 JSON", color = Mint, fontSize = 12.sp) }
                    }
                    Text("分享面板里可选择微信、网盘、邮件等已安装应用。", color = Muted, fontSize = 10.sp)
                    if (automaticBackups.isNotEmpty()) {
                        val latest = automaticBackups.maxByOrNull { it.modifiedAt }
                        Text("自动备份：${automaticBackups.size}/7 份 · 最近 ${latest?.modifiedAt}", color = Mint, fontSize = 10.sp)
                    } else {
                        Text("自动备份：每天首次修改后生成，保留 7 份。", color = Muted, fontSize = 10.sp)
                    }
                    if (snapshots.isNotEmpty()) {
                        Text("最近恢复快照", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        snapshots.take(3).forEach { snapshot ->
                            TextButton(
                                onClick = {
                                    runCatching { snapshot.readText() }
                                        .getOrNull()
                                        ?.let { restored ->
                                            if (model.importBackup(restored)) model.shareNotice = "已恢复快照 ${snapshot.name}"
                                        }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("恢复 ${snapshot.name}", color = Muted, fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { pickBackup.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("选择备份文件", color = Mint, fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { model.backupMode = "paste" },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("粘贴 JSON", color = Mint, fontSize = 12.sp) }
                    }
                    if (mode == "paste") {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text("粘贴 SmartNutrient JSON 备份", color = Muted) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                            shape = RoundedCornerShape(13.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                        )
                    }
                    inspection?.let { preview ->
                        Surface(
                            color = DeepSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, if (preview.valid) Hairline else Danger.copy(alpha = .6f), RoundedCornerShape(16.dp)),
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (preview.valid) "备份内容检查通过" else "备份不可用", color = if (preview.valid) Mint else Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(preview.message, color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                                if (preview.valid) Text("恢复前会自动保存当前数据快照", color = Lime, fontSize = 10.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { if (model.mergeBackup(text)) { model.backupMode = null; model.shareNotice = "备份已合并，当前数据保持不变。" } },
                            enabled = inspection?.valid == true,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("合并恢复", fontSize = 13.sp) }
                        Button(
                            onClick = { if (model.importBackup(text)) { model.backupMode = null; model.shareNotice = "已覆盖恢复；恢复前数据已自动快照。" } },
                            enabled = inspection?.valid == true,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Alert, contentColor = Night),
                        ) { Text("覆盖恢复", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    }
                    model.importError?.let { Text(it, color = Danger, fontSize = 11.sp) }
                }
            }
        }
    }
}





@Composable
private fun ConfirmDialog(model: AppState, action: String) {
    val isClear = action == "clearDay"
    val logId = action.removePrefix("remove:").toLongOrNull()
    AlertDialog(
        onDismissRequest = { model.confirmAction = null },
        title = { Text(if (isClear) "清空当日记录？" else "删除这条记录？", color = Ink) },
        text = { Text(if (isClear) "餐次、饮水和当日营养记录会被清空，此操作不能自动撤销。" else "删除后可以重新添加，但当前记录本身不能自动恢复。", color = Muted) },
        confirmButton = {
            TextButton(onClick = {
                if (isClear) model.clearDay() else logId?.let(model::remove)
                model.confirmAction = null
            }) { Text("确认", color = Alert) }
        },
        dismissButton = { TextButton(onClick = { model.confirmAction = null }) { Text("取消", color = Muted) } }
    )
}
