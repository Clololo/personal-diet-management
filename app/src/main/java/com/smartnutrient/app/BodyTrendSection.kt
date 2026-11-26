package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
internal fun BodyTrendSection(model: AppState, days: Int, onDaysChange: (Int) -> Unit) {
    val start = LocalDate.now().minusDays(days.toLong() - 1)
    val values = BodyMeasurementRules.between(model.bodyMeasurements.toList(), start, LocalDate.now())
    val latest = values.lastOrNull()
    var weightText by remember(latest?.date, latest?.weightKg) { mutableStateOf(latest?.weightKg?.toString().orEmpty()) }
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(18.dp))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text("身体趋势", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(7, 30, 90).forEach { option ->
                Button(onClick = { onDaysChange(option) }, shape = RoundedCornerShape(10.dp)) {
                    Text("${option}天", fontSize = 10.sp)
                }
            }
        }
        Text("最近 ${days} 天 · ${values.size} 条测量", color = Muted, fontSize = 10.sp)
        if (values.size >= 2) {
            Text("体重 ${values.first().weightKg} kg → ${values.last().weightKg} kg", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            BodyTrendBar(values)
        } else {
            Text("至少记录两次体重后显示趋势。", color = Muted, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("今天体重 kg", color = Muted, fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            Button(
                onClick = {
                    val value = weightText.toDoubleOrNull()?.takeIf { it in 30.0..300.0 } ?: return@Button
                    model.saveBodyMeasurement(BodyMeasurement(LocalDate.now(), value))
                },
                shape = RoundedCornerShape(12.dp),
            ) { Text("记录", fontSize = 11.sp) }
        }
        Text("同一天只保留一条，重新记录会覆盖当天值。", color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun BodyTrendBar(values: List<BodyMeasurement>) {
    val min = values.minOf { it.weightKg }
    val max = values.maxOf { it.weightKg }
    val span = (max - min).coerceAtLeast(1.0)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        values.forEach { value ->
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                androidx.compose.foundation.layout.Box(
                    Modifier.background(Mint, RoundedCornerShape(3.dp)).padding(vertical = ((value.weightKg - min) / span * 35 + 5).dp),
                )
                Text(value.date.toString().takeLast(2), color = Muted, fontSize = 8.sp)
            }
        }
    }
}
