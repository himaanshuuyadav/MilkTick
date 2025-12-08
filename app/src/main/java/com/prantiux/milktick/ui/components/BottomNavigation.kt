package com.prantiux.milktick.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, stringResource(R.string.nav_home)),
        BottomNavItem(Screen.Rate.route, Icons.Default.MonetizationOn, stringResource(R.string.nav_rate)),
        BottomNavItem(Screen.Records.route, Icons.AutoMirrored.Filled.List, stringResource(R.string.nav_records)),
        BottomNavItem(Screen.Summary.route, Icons.Default.Summarize, stringResource(R.string.nav_summary)),
        BottomNavItem(Screen.Settings.route, Icons.Default.Settings, stringResource(R.string.nav_settings))
    )
    
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )),
        containerColor = MaterialTheme.colorScheme.secondary, // Use #121212
        contentColor = MaterialTheme.colorScheme.onSecondary, // White content
        tonalElevation = 0.dp // Remove elevation that might cause color shifts
    ) {
        items.forEach { item ->
            val isSelected = item.route == currentRoute
            
            // Smooth color transition for icons and text
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "iconColor"
            )
            
            val textColor by animateColorAsState(
                targetValue = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "textColor"
            )
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { 
                    if (!isSelected) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp) // Increased gap between icon and text
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor // Animated color
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor // Animated color
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Transparent, // Let our custom animation handle colors
                    selectedTextColor = Color.Transparent,
                    unselectedIconColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color(0xFF00090F) // Custom dark background for active tab
                )
            )
        }
    }
} 