package com.calisthenia.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CalisteniaSpacing(
    val xxSmall: Dp = 2.dp,
    val xSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val xxLarge: Dp = 48.dp,
)

val LocalSpacing = staticCompositionLocalOf { CalisteniaSpacing() }

fun calisteniaSpacing(): CalisteniaSpacing = CalisteniaSpacing()
