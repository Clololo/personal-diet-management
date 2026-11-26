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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WeeklyDeviationPanel(summary: WeeklyDeviationSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        summary.metrics.forEach { metric -> WeeklyMetricCard(metric) }
        if (!summary.addedSugarKnown) {
            Text("添加糖数据不完整，本项已隐藏；总糖仍在详情中单独显示。", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun WeeklyMetricCard(metric: NutrientDeviation) {
    val deviationColor = when {
        metric.excess > 0.0 -> Alert
        metric.shortfall > 0.0 -> Mint
        else -> Lime
    }
    Column(
        Modifier.fillMaxWidth().background(RaisedSurface, RoundedCornerShape(15.dp))
            .border(1.dp, Hairline.copy(alpha = .75f), RoundedCornerShape(15.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(metric.label, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        WeeklyValueRow("实际累计", metric.actualText, Ink)
        WeeklyValueRow("推荐累计", metric.recommendedText, Muted)
        Text(metric.deviationText, color = deviationColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WeeklyValueRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
