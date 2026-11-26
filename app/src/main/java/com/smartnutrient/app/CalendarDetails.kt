package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter

@Composable
internal fun ImportConfirmDialog(onMerge: () -> Unit, onReplace: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Surface(color = Night, shape = RoundedCornerShape(23.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("导入日历数据", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("推荐合并导入：保留当前记录，只补充备份中的日期和不同记录。重复导入不会重复添加。", color = Muted, fontSize = 12.sp)
                Button(onClick = onMerge, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("合并导入（保留现有数据）", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onReplace, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("覆盖导入（清空当前记录）", color = Alert)
                }
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) { Text("取消", color = Muted) }
            }
        }
    }
}

@Composable
internal fun CalendarDayDetail(model: AppState, total: NutritionSnapshot) {
    val entries = model.entriesOn(model.selectedDate)
    val activity = CalendarActivitySummary.from(model.activeProfile, model.selectedActivityPlan)
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(20.dp))
            .border(1.dp, Hairline, RoundedCornerShape(20.dp)).padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(model.selectedDate.format(DateTimeFormatter.ofPattern("M月d日 EEEE")), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("当日总量 · 饮水 ${model.waterOn(model.selectedDate)} ml · ${entries.size} 条记录", color = Muted, fontSize = 11.sp)
        Column(
            Modifier.fillMaxWidth().background(RaisedSurface, RoundedCornerShape(16.dp))
                .border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("运动与能量消耗", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            CalendarMetricRow("额外运动", activity.manualExerciseText)
            CalendarMetricRow("今日步数", activity.stepsText)
            CalendarMetricRow("步行净消耗", activity.walkingText)
            HorizontalDivider(color = Hairline.copy(alpha = .55f))
            CalendarMetricRow("总活动消耗", activity.totalActivityText, Mint)
            CalendarMetricRow("推荐中心热量", activity.dailyTargetText, Mint)
            Text("推荐 = 完全静息 + 额外运动 + 步行净消耗，再应用减脂/维持/增重目标。", color = Muted, fontSize = 9.sp, lineHeight = 14.sp)
        }
        NutritionValuesPanel(total, "当日营养素总量")
        HorizontalDivider(color = Hairline)
        Text("餐次明细", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Meal.entries.forEach { meal ->
            val mealEntries = entries.filter { it.meal == meal }
            if (mealEntries.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(meal.label, color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${mealEntries.sumOf { model.snapshot(it).caloriesKcal }.toInt()} kcal · ${mealEntries.size} 项", color = Muted, fontSize = 11.sp)
                }
                mealEntries.forEach { log ->
                    Row(
                        Modifier.fillMaxWidth().clickable { model.viewingNutritionLog = log }.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            FoodKindBadge(log.food)
                            Text("${log.food.name} · ${log.grams.toInt()} g", color = Ink, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("${model.snapshot(log).caloriesKcal.toInt()} kcal", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { model.adding = true }) { Text("继续编辑", color = Mint, fontSize = 12.sp) }
            TextButton(onClick = { model.copyYesterday() }) { Text("复制昨日", color = Mint, fontSize = 12.sp) }
            TextButton(onClick = { model.confirmAction = "clearDay" }) { Text("清空当日", color = Alert, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun CalendarMetricRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = Ink) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
