package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NutrientTarget(
    val label: String,
    val min: Double,
    val max: Double,
    val unit: String,
    val lowerIsBetter: Boolean,
) {
    val targetText: String
        get() = when {
            min > 0.0 && max > 0.0 -> "${format(min)}–${format(max)} $unit"
            min > 0.0 -> "至少 ${format(min)} $unit"
            else -> "不超过 ${format(max)} $unit"
        }

    fun progress(value: Double): Float = if (max <= 0.0) 0f else (value / max).toFloat().coerceIn(0f, 1f)

    fun isAlert(value: Double): Boolean = if (lowerIsBetter) value > max else value < min

    private fun format(value: Double): String =
        if (value >= 100.0) value.toInt().toString() else "%.1f".format(value)
}

data class NutritionTargets(
    val calories: NutrientTarget,
    val protein: NutrientTarget,
    val fat: NutrientTarget,
    val saturatedFat: NutrientTarget,
    val carbohydrate: NutrientTarget,
    val fiber: NutrientTarget,
    val sugar: NutrientTarget,
    val sodium: NutrientTarget,
    val potassium: NutrientTarget,
    val cholesterol: NutrientTarget,
) {
    val all: List<NutrientTarget> get() = listOf(
        calories, protein, fat, saturatedFat, carbohydrate,
        fiber, sugar, sodium, potassium, cholesterol,
    )
}

data class NutritionValueRow(val label: String, val value: String)

object NutritionValues {
    fun rows(snapshot: NutritionSnapshot): List<NutritionValueRow> = listOf(
        NutritionValueRow("热量", formatValue(snapshot.caloriesKcal, "kcal")),
        NutritionValueRow("蛋白质", formatValue(snapshot.proteinG, "g")),
        NutritionValueRow("脂肪", formatValue(snapshot.fatG, "g")),
        NutritionValueRow("饱和脂肪", formatValue(snapshot.saturatedFatG, "g")),
        NutritionValueRow("碳水化合物", formatValue(snapshot.carbohydrateG, "g")),
        NutritionValueRow("膳食纤维", formatValue(snapshot.fiberG, "g")),
        NutritionValueRow("总糖", formatValue(snapshot.sugarG, "g")),
    ) + (snapshot.addedSugarG?.let { listOf(NutritionValueRow("添加糖", formatValue(it, "g"))) } ?: emptyList()) + listOf(
        NutritionValueRow("钠", formatValue(snapshot.sodiumMg, "mg")),
        NutritionValueRow("钾", formatValue(snapshot.potassiumMg, "mg")),
        NutritionValueRow("胆固醇", formatValue(snapshot.cholesterolMg, "mg")),
    )
}

@Composable
internal fun DailyNutritionPanel(
    total: NutritionSnapshot,
    targets: NutritionTargets,
    modifier: Modifier = Modifier,
) {
    val visibleTargets = targets.all.filterNot { it.label == "添加糖" && total.addedSugarG == null }
    Column(
        modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(22.dp))
            .border(1.dp, Hairline, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("营养素总量", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("实际 / 推荐区间", color = Muted, fontSize = 11.sp)
        }
        visibleTargets.forEach { target ->
            val value = when (target.label) {
                "热量" -> total.caloriesKcal
                "蛋白质" -> total.proteinG
                "脂肪" -> total.fatG
                "饱和脂肪" -> total.saturatedFatG
                "碳水化合物" -> total.carbohydrateG
                "膳食纤维" -> total.fiberG
                "添加糖" -> total.addedSugarG ?: 0.0
                "钠" -> total.sodiumMg
                "钾" -> total.potassiumMg
                else -> total.cholesterolMg
            }
            val known = target.label != "添加糖" || total.addedSugarG != null
            val highlight = if (known && target.isAlert(value)) Alert else Mint
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(target.label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (known) formatValue(value, target.unit) else "数据不足", color = highlight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (known) "推荐  ${target.targetText}" else "推荐上限  ${target.max.let { if (it >= 100.0) it.toInt().toString() else "%.1f".format(it) }} ${target.unit}",
                    color = Muted,
                    fontSize = 11.sp,
                )
                if (known) {
                    LinearProgressIndicator(
                        progress = { target.progress(value) },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = highlight,
                        trackColor = Hairline,
                    )
                }
            }
        }
        HorizontalDivider(color = Hairline.copy(alpha = .7f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("总糖（仅参考）", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("天然糖与添加糖合计，不单独设健康上限", color = Muted, fontSize = 11.sp)
            }
            Text(formatValue(total.sugarG, "g"), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun NutritionValuesPanel(
    total: NutritionSnapshot,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(22.dp))
            .border(1.dp, Hairline, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("实际摄入", color = Muted, fontSize = 11.sp)
        }
        NutritionValues.rows(total).forEachIndexed { index, row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(row.label, color = Muted, fontSize = 13.sp)
                Text(row.value, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            if (index != NutritionValues.rows(total).lastIndex) {
                HorizontalDivider(color = Hairline.copy(alpha = .55f))
            }
        }
    }
}

private fun formatValue(value: Double, unit: String): String = when (unit) {
    "kcal" -> "${value.toInt()} kcal"
    "mg" -> if (value % 1.0 == 0.0) "${value.toInt()} mg" else "%.1f mg".format(value)
    else -> "%.1f g".format(value)
}
