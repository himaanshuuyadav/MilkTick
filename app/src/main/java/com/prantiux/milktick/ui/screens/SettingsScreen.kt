package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.ModernLogoutDialog
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    @Suppress("UNUSED_VARIABLE") val currentUserId by appViewModel.currentUserId.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = 24.dp
                )
            ) {
                item {
                    // Profile Section - Without Edit Button
                    SimpleProfileCard(
                        userEmail = currentUser?.email ?: "No user"
                    )
                }
                
                item {
                    // Settings List
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp)
                        ) {
                            // Edit Profile
                            SettingsItem(
                                icon = R.drawable.ic_fa_user,
                                title = "Edit Profile",
                                subtitle = "Update your personal information"
                            ) {
                                navController.navigate(Screen.EditProfile.route)
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            
                            // Change Password
                            SettingsItem(
                                icon = R.drawable.ic_fa_lock,
                                title = "Change Password",
                                subtitle = "Update your account password"
                            ) {
                                navController.navigate(Screen.ChangePassword.route)
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            
                            // Notifications
                            SettingsItem(
                                icon = R.drawable.ic_fa_bell,
                                title = "Notifications",
                                subtitle = "Manage your notification preferences"
                            ) {
                                navController.navigate(Screen.NotificationSettings.route)
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            
                            // Theme
                            SettingsItem(
                                icon = R.drawable.ic_fa_circle_info,
                                title = "Theme",
                                subtitle = "Customize appearance and colors"
                            ) {
                                navController.navigate(Screen.Theme.route)
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            
                            // About
                            SettingsItem(
                                icon = R.drawable.ic_fa_circle_info,
                                title = "About MilkTick",
                                subtitle = "App version and information"
                            ) {
                                navController.navigate(Screen.About.route)
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                            
                            // Sign Out
                            SettingsItem(
                                icon = R.drawable.ic_fa_right_from_bracket,
                                title = "Sign Out",
                                subtitle = "Sign out of your account",
                                isDanger = true
                            ) {
                                showLogoutDialog = true
                            }
                        }
                    }
                }
            }
        }
        
        // Modern Logout Dialog
        if (showLogoutDialog) {
            ModernLogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    // Cancel all scheduled notifications
                    com.prantiux.milktick.notification.NotificationScheduler.cancelAllNotifications(context)
                    
                    authViewModel.signOut()
                    appViewModel.resetApp()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Simple Profile Card without Edit Button
@Composable
fun SimpleProfileCard(userEmail: String) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Use Firebase Auth displayName if available, otherwise extract from email
    val userName = currentUser?.displayName?.takeIf { it.isNotEmpty() } 
        ?: userEmail.substringBefore("@").takeIf { it.isNotEmpty() } 
        ?: "User"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Default avatar icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_user),
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Profile Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Settings Item Component
@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    subtitle: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (isDanger) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (isDanger)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
    }
}
