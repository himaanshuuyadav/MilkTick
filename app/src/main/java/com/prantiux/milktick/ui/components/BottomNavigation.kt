package com.prantiux.milktick.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen


data class BottomNavItem(
    val route: String,
    @DrawableRes val iconRes: Int,
    val label: String
)

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, R.drawable.ic_fa_house, stringResource(R.string.nav_home)),
        BottomNavItem(Screen.Records.route, R.drawable.ic_fa_list, stringResource(R.string.nav_records)),
        BottomNavItem(Screen.Summary.route, R.drawable.ic_fa_file_lines, stringResource(R.string.nav_summary)),
        BottomNavItem(Screen.Settings.route, R.drawable.ic_fa_gear, stringResource(R.string.nav_settings))
    )

    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navBarInset + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(60.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 7.dp)
                    .height(46.dp)
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.route == currentRoute

                        val itemWidth by animateDpAsState(
                            targetValue = if (isSelected) 92.dp else 40.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "itemWidth"
                        )

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                            label = "iconColor"
                        )

                        val backgroundAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "backgroundAlpha"
                        )

                        val backgroundScale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.88f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "backgroundScale"
                        )

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.96f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "iconScale"
                        )

                        val iconOffsetX by animateFloatAsState(
                            targetValue = if (isSelected) -1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "iconOffsetX"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .width(itemWidth)
                                .fillMaxHeight()
                                .zIndex(if (isSelected) 1f else 0f)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        if (!isSelected) {
                                            onNavigate(item.route)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer {
                                        alpha = backgroundAlpha
                                        scaleX = backgroundScale
                                        scaleY = backgroundScale
                                    }
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(percent = 50)
                                    )
                            )

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                            translationX = iconOffsetX
                                        },
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label,
                                    tint = if (isSelected) iconColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(animationSpec = tween(260)),
                                    exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(animationSpec = tween(180))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        if (item.route == Screen.Summary.route) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f))
                            )
                        }
                }
            }
        }
    }
}
