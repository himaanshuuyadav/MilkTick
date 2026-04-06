package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.prantiux.milktick.ui.components.MilkTickFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickSystemBarsGradient
import com.prantiux.milktick.ui.components.ModernLogoutDialog
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.AppViewModel

private data class SettingsEntry(
    val icon: Int,
    val title: String,
    val subtitle: String,
    val isDanger: Boolean = false,
    val onClick: () -> Unit
)

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
    val listState = rememberLazyListState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val settingsEntries = remember(navController) {
        listOf(
            SettingsEntry(
                icon = R.drawable.ic_fa_user,
                title = "Edit Profile",
                subtitle = "Update your personal information",
                onClick = { navController.navigate(Screen.EditProfile.route) }
            ),
            SettingsEntry(
                icon = R.drawable.ic_fa_lock,
                title = "Change Password",
                subtitle = "Update your account password",
                onClick = { navController.navigate(Screen.ChangePassword.route) }
            ),
            SettingsEntry(
                icon = R.drawable.ic_fa_bell,
                title = "Notifications",
                subtitle = "Manage your notification preferences",
                onClick = { navController.navigate(Screen.NotificationSettings.route) }
            ),
            SettingsEntry(
                icon = R.drawable.ic_fa_circle_info,
                title = "Theme",
                subtitle = "Customize appearance and colors",
                onClick = { navController.navigate(Screen.Theme.route) }
            ),
            SettingsEntry(
                icon = R.drawable.ic_fa_circle_info,
                title = "About MilkTick",
                subtitle = "App version and information",
                onClick = { navController.navigate(Screen.About.route) }
            ),
            SettingsEntry(
                icon = R.drawable.ic_fa_right_from_bracket,
                title = "Sign Out",
                subtitle = "Sign out of your account",
                isDanger = true,
                onClick = { showLogoutDialog = true }
            )
        )
    }
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    .padding(horizontal = 24.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 144.dp, bottom = 120.dp)
            ) {
                item {
                    // Profile Section - Without Edit Button
                    SimpleProfileCard(
                        userEmail = currentUser?.email ?: "No user"
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        settingsEntries.forEachIndexed { index, entry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = groupedSettingsCardShape(index = index, lastIndex = settingsEntries.lastIndex),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                SettingsItem(
                                    icon = entry.icon,
                                    title = entry.title,
                                    subtitle = entry.subtitle,
                                    isDanger = entry.isDanger,
                                    onClick = entry.onClick
                                )
                            }
                        }
                    }
                }
            }

            MilkTickSystemBarsGradient()

            MilkTickFloatingHeader(
                title = "Settings",
                scrollState = listState
            )
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
            .clip(RoundedCornerShape(24.dp)),
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

private fun groupedSettingsCardShape(index: Int, lastIndex: Int): RoundedCornerShape {
    return when {
        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        index == lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(8.dp)
    }
}
