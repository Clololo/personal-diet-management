package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileHistorySection(model: AppState) {
    val versions = model.profileVersions
    Column(
        Modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(18.dp))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("档案历史版本", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (versions.isEmpty()) {
            Text("升级到 0.4.2 前的历史记录统一按当前档案计算；修改身体档案后会自动建立当日版本。", color = Muted, fontSize = 11.sp)
        } else {
            versions.toSortedMap(compareByDescending { it }).forEach { (date, profile) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), color = Mint, fontSize = 13.sp)
                    Text(
                        "${profile.age} 岁 · ${profile.heightCm.toInt()} cm · ${profile.weightKg.toInt()} kg · ${profile.goal.name}",
                        color = Ink,
                        fontSize = 12.sp,
                    )
                }
                HorizontalDivider(color = Hairline.copy(alpha = .5f))
            }
        }
    }
}
