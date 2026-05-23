package com.druk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MonkScheme = darkColorScheme(
    primary = MonkAmber,
    secondary = MonkGreen,
    background = MonkBg,
    surface = MonkSurface2,
    onPrimary = MonkBg,
    onSecondary = MonkBg,
    onBackground = MonkCream,
    onSurface = MonkCream
)

@Composable
fun DrukTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MonkScheme,
        content = content
    )
}
