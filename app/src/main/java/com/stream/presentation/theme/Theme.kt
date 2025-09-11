package com.stream.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val onSurface: Color = Color.White
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }

@Composable
fun Bullet(content: @Composable () -> Unit) {
    val colors = darkColorScheme()
    androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides AppColors()) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}




