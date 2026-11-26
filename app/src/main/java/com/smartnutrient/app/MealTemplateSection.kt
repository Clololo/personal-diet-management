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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MealTemplateSection(model: AppState, onSaveCurrent: () -> Unit) {
    val templates = model.mealTemplates.sortedWith(compareByDescending<MealTemplate> { it.usageCount }.thenByDescending { it.updatedAt }).take(6)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("常用组合", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onSaveCurrent) { Text("保存今天为组合", color = Mint, fontSize = 10.sp) }
        }
        if (templates.isEmpty()) {
            Text("把常吃的一餐保存为组合，之后两次触控即可复记。", color = Muted, fontSize = 11.sp)
        } else {
            templates.forEach { template ->
                Column(
                    Modifier.fillMaxWidth().background(RaisedSurface, RoundedCornerShape(15.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(15.dp))
                        .clickable { model.applyMealTemplate(template) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(template.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${template.meal.label} · ${template.items.size} 项", color = Mint, fontSize = 10.sp)
                    }
                    Text(template.items.joinToString("、") { "${it.food.name} ${it.grams.toInt()}g" }, color = Muted, fontSize = 10.sp)
                }
            }
        }
    }
}
