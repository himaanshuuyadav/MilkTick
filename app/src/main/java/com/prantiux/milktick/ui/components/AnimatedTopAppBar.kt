package com.prantiux.milktick.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTopAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = tween(400),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(400))) togetherWith
                    (slideOutVertically(
                        animationSpec = tween(400),
                        targetOffsetY = { -it / 2 }
                    ) + fadeOut(animationSpec = tween(400)))
                },
                label = "titleAnimation"
            ) { animatedTitle ->
                Text(
                    text = animatedTitle,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        actions = actions
    )
}
