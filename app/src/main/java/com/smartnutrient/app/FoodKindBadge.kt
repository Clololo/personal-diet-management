package com.smartnutrient.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FoodKindBadge(food: FoodItem, modifier: Modifier = Modifier) {
    val kind = FoodKind.of(food)
    val color = when (kind) {
        FoodKind.DISH -> Mint
        FoodKind.INGREDIENT -> Lime
        FoodKind.SEASONING -> Alert
        FoodKind.FRUIT -> Color(0xFFFFC46B)
        FoodKind.DRINK -> Color(0xFF7BC8FF)
        FoodKind.SUPPLEMENT -> Color(0xFFC9A7FF)
    }
    Text(
        kind.label,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(7.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}
