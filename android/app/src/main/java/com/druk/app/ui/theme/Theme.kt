package com.druk.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = DrukTeal,
    secondary = DrukGold,
    background = DrukPaper,
    surface = DrukPaper,
    onPrimary = DrukPaper,
    onSecondary = DrukInk,
    onBackground = DrukInk,
    onSurface = DrukInk
)

@Composable
fun DrukTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content
    )
}

