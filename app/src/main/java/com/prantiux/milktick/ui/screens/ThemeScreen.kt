package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { com.prantiux.milktick.ui.theme.ThemePreferences(context) }
    
    // Load current preferences
    var selectedTheme by remember { mutableStateOf(themePreferences.getThemeMode().name) }
    var selectedColorScheme by remember { 
        mutableStateOf(
            themePreferences.getAvailableColors().find { 
                it.second == themePreferences.getAccentColor() 
            }?.first ?: "Navy"
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme & Appearance") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.clip(RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Theme Mode Section
                item {
                    ThemeSection(
                        title = "Theme Mode",
                        icon = Icons.Default.DarkMode
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOption(
                                title = "Auto",
                                subtitle = "Follow system setting",
                                selected = selectedTheme == "AUTO",
                                onClick = { 
                                    selectedTheme = "AUTO"
                                    themePreferences.setThemeMode(com.prantiux.milktick.ui.theme.ThemeMode.AUTO)
                                }
                            )
                            ThemeOption(
                                title = "Light",
                                subtitle = "Light theme always",
                                selected = selectedTheme == "LIGHT",
                                onClick = { 
                                    selectedTheme = "LIGHT"
                                    themePreferences.setThemeMode(com.prantiux.milktick.ui.theme.ThemeMode.LIGHT)
                                }
                            )
                            ThemeOption(
                                title = "Dark",
                                subtitle = "Dark theme always",
                                selected = selectedTheme == "DARK",
                                onClick = { 
                                    selectedTheme = "DARK"
                                    themePreferences.setThemeMode(com.prantiux.milktick.ui.theme.ThemeMode.DARK)
                                }
                            )
                        }
                    }
                }
                
                // Color Scheme Section
                item {
                    ThemeSection(
                        title = "Accent Color",
                        icon = Icons.Default.Palette
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ColorSchemeOption(
                                name = "Navy",
                                color = Color(0xFF003153), // Original accent
                                selected = selectedColorScheme == "Navy",
                                onClick = { 
                                    selectedColorScheme = "Navy"
                                    themePreferences.setAccentColor(Color(0xFF003153))
                                }
                            )
                            ColorSchemeOption(
                                name = "Teal",
                                color = Color(0xFF26A69A),
                                selected = selectedColorScheme == "Teal",
                                onClick = { 
                                    selectedColorScheme = "Teal"
                                    themePreferences.setAccentColor(Color(0xFF26A69A))
                                }
                            )
                            ColorSchemeOption(
                                name = "Blue",
                                color = Color(0xFF42A5F5),
                                selected = selectedColorScheme == "Blue",
                                onClick = { 
                                    selectedColorScheme = "Blue"
                                    themePreferences.setAccentColor(Color(0xFF42A5F5))
                                }
                            )
                            ColorSchemeOption(
                                name = "Green",
                                color = Color(0xFF66BB6A),
                                selected = selectedColorScheme == "Green",
                                onClick = { 
                                    selectedColorScheme = "Green"
                                    themePreferences.setAccentColor(Color(0xFF66BB6A))
                                }
                            )
                            ColorSchemeOption(
                                name = "Purple",
                                color = Color(0xFFAB47BC),
                                selected = selectedColorScheme == "Purple",
                                onClick = { 
                                    selectedColorScheme = "Purple"
                                    themePreferences.setAccentColor(Color(0xFFAB47BC))
                                }
                            )
                            ColorSchemeOption(
                                name = "Orange",
                                color = Color(0xFFFF7043),
                                selected = selectedColorScheme == "Orange",
                                onClick = { 
                                    selectedColorScheme = "Orange"
                                    themePreferences.setAccentColor(Color(0xFFFF7043))
                                }
                            )
                            ColorSchemeOption(
                                name = "Pink",
                                color = Color(0xFFEC407A),
                                selected = selectedColorScheme == "Pink",
                                onClick = { 
                                    selectedColorScheme = "Pink"
                                    themePreferences.setAccentColor(Color(0xFFEC407A))
                                }
                            )
                        }
                    }
                }
                
                // Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Theme changes will be applied immediately",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.size(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            content()
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun ColorSchemeOption(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                color.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


