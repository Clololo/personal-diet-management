package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ActivityEnergyCard(model: AppState, modifier: Modifier = Modifier) {
    val plan = model.selectedActivityPlan
    val breakdown = NutritionEngine.energyBreakdown(model.activeProfile, plan)
    var exerciseText by remember(model.selectedDate, plan.manualExerciseKcal) {
        mutableStateOf(plan.manualExerciseKcal.toInt().toString())
    }
    var stepsText by remember(model.selectedDate, plan.steps) {
        mutableStateOf(plan.steps.toString())
    }
    Column(
        modifier.fillMaxWidth().background(DeepSurface, RoundedCornerShape(20.dp))
            .border(1.dp, Hairline, RoundedCornerShape(20.dp)).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("今日运动消耗", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("额外运动由你填写；步数按体重估算净消耗，再与完全静息消耗合并计算推荐。", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = exerciseText,
                onValueChange = { exerciseText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("额外运动 kcal", color = Muted, fontSize = 11.sp) },
                modifier = Modifier.weight(1f).heightIn(min = 58.dp),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Mint,
                    unfocusedBorderColor = Hairline,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
            )
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it.filter(Char::isDigit) },
                label = { Text("今天走了多少步", color = Muted, fontSize = 11.sp) },
                modifier = Modifier.weight(1.25f).heightIn(min = 58.dp),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Mint,
                    unfocusedBorderColor = Hairline,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
            )
            Button(
                onClick = {
                    model.updateActivityForDate(
                        DailyActivityPlan(
                            manualExerciseKcal = DailyActivityPlan.normalizeManualExercise(exerciseText),
                            steps = DailyActivityPlan.normalizeSteps(stepsText),
                        ),
                    )
                },
                modifier = Modifier.heightIn(min = 56.dp),
                shape = RoundedCornerShape(13.dp),
            ) { Text("保存", fontWeight = FontWeight.Bold) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("静息 ${breakdown.restingKcal.toInt()} + 额外运动 ${breakdown.manualExerciseKcal.toInt()} + 步行净耗 ${breakdown.walkingKcal.toInt()}", color = Muted, fontSize = 10.sp)
            Text("推荐 ${breakdown.target.centerKcal.toInt()} kcal", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
