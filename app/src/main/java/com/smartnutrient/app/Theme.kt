package com.smartnutrient.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Night = Color(0xFF07110F)
val DeepSurface = Color(0xFF0D1B18)
val RaisedSurface = Color(0xFF132622)
val Hairline = Color(0xFF244039)
val Mint = Color(0xFF68E0B4)
val Lime = Color(0xFFC4F36C)
val Ink = Color(0xFFEAF7F0)
val Muted = Color(0xFF8BA79D)
val Alert = Color(0xFFFFB56B)
val Danger = Color(0xFFFF7B70)

@Composable
fun SmartNutrientTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Mint,
            secondary = Lime,
            background = Night,
            surface = DeepSurface,
            surfaceVariant = RaisedSurface,
            onSurface = Ink,
            onSurfaceVariant = Muted,
            error = Danger
        ),
        content = content
    )
}
