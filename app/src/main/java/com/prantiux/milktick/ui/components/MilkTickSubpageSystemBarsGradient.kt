package com.prantiux.milktick.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private val SubpageHeaderHeight = 40.dp
private val SubpageHeaderTopOffset = 4.dp
private const val SubpageHeaderSolidFraction = 0.25f
private val SubpageHeaderFadeTail = 24.dp

@Composable
fun BoxScope.MilkTickSubpageSystemBarsGradient() {
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val topGradientHeight = statusBarInset + SubpageHeaderTopOffset + SubpageHeaderHeight + SubpageHeaderFadeTail
    val topSolidHeight = statusBarInset + SubpageHeaderTopOffset + (SubpageHeaderHeight * SubpageHeaderSolidFraction)

    val baseColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    val topSolidStop = (topSolidHeight / topGradientHeight).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(6f)
            .fillMaxWidth()
            .height(topGradientHeight)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to baseColor,
                        topSolidStop to baseColor,
                        (topSolidStop + (1f - topSolidStop) * 0.08f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.985f),
                        (topSolidStop + (1f - topSolidStop) * 0.18f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.95f),
                        (topSolidStop + (1f - topSolidStop) * 0.30f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.88f),
                        (topSolidStop + (1f - topSolidStop) * 0.42f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.78f),
                        (topSolidStop + (1f - topSolidStop) * 0.54f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.64f),
                        (topSolidStop + (1f - topSolidStop) * 0.66f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.5f),
                        (topSolidStop + (1f - topSolidStop) * 0.76f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.36f),
                        (topSolidStop + (1f - topSolidStop) * 0.86f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.22f),
                        (topSolidStop + (1f - topSolidStop) * 0.94f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.1f),
                        1f to Color.Transparent
                    )
                )
            )
    )
}