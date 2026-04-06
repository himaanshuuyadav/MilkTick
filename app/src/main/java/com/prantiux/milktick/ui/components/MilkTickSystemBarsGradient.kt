package com.prantiux.milktick.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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

private val FloatingHeaderHeight = 60.dp
private val FloatingHeaderTopOffset = 4.dp
private const val FloatingHeaderSolidFraction = 0.25f
private val FloatingHeaderFadeTail = 24.dp

private val AppNavBarHeight = 60.dp
private const val AppNavBarSolidFraction = 0.25f
private val AppNavBarFadeTail = 24.dp

@Composable
fun BoxScope.MilkTickSystemBarsGradient() {
    val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val topGradientHeight = statusBarInset + FloatingHeaderTopOffset + FloatingHeaderHeight + FloatingHeaderFadeTail
    val topSolidHeight = statusBarInset + FloatingHeaderTopOffset + (FloatingHeaderHeight * FloatingHeaderSolidFraction)

    val bottomGradientHeight = navBarInset + AppNavBarHeight + AppNavBarFadeTail
    val bottomSolidHeight = navBarInset + (AppNavBarHeight * AppNavBarSolidFraction)

    val baseColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    val topSolidStop = (topSolidHeight / topGradientHeight).coerceIn(0f, 1f)
    val bottomSolidStart = ((bottomGradientHeight - bottomSolidHeight) / bottomGradientHeight).coerceIn(0f, 1f)

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

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .zIndex(6f)
            .fillMaxWidth()
            .height(bottomGradientHeight)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        (bottomSolidStart * 0.14f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.04f),
                        (bottomSolidStart * 0.26f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.1f),
                        (bottomSolidStart * 0.38f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.18f),
                        (bottomSolidStart * 0.5f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.28f),
                        (bottomSolidStart * 0.62f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.42f),
                        (bottomSolidStart * 0.74f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.58f),
                        (bottomSolidStart * 0.84f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.72f),
                        (bottomSolidStart * 0.92f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.86f),
                        (bottomSolidStart * 0.97f).coerceIn(0f, 1f) to baseColor.copy(alpha = 0.94f),
                        bottomSolidStart to baseColor,
                        1f to baseColor
                    )
                )
            )
    )
}