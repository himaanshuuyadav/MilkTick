package com.prantiux.milktick.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.SkeletonRateHeaderCard
import com.prantiux.milktick.ui.components.SkeletonRateFormCard
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.RateViewModel
import com.prantiux.milktick.viewmodel.AppViewModel
import com.prantiux.milktick.viewmodel.SaveButtonState
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RateScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    rateViewModel: RateViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val uiState by rateViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId by appViewModel.currentUserId.collectAsState()
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
        }
    }
    
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            rateViewModel.setCurrentUserId(userId)
        }
    }
    
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            rateViewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rate_title)) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header section - always show
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(48.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = "Set Monthly Rate",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Configure your milk rate per liter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Form section - show loading or content
                if (uiState.isLoading) {
                    // Show skeleton loading for form only
                    SkeletonRateFormCard()
                } else {
                    // Form section
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
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rate Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Edit button - only show when data exists and not in edit mode
                            AnimatedVisibility(
                                visible = uiState.hasData && !uiState.isEditMode,
                                enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
                                exit = scaleOut(animationSpec = tween(300)) + fadeOut()
                            ) {
                                IconButton(
                                    onClick = { rateViewModel.enableEditMode() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // Month/Year display - always visible
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = uiState.selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Rate and Quantity - animated between input and display modes
                        AnimatedContent(
                            targetState = uiState.isEditMode,
                            transitionSpec = {
                                if (targetState) {
                                    slideInVertically(
                                        animationSpec = tween(400),
                                        initialOffsetY = { it / 2 }
                                    ) + fadeIn(animationSpec = tween(400)) togetherWith
                                    slideOutVertically(
                                        animationSpec = tween(400),
                                        targetOffsetY = { -it / 2 }
                                    ) + fadeOut(animationSpec = tween(400))
                                } else {
                                    slideInVertically(
                                        animationSpec = tween(400),
                                        initialOffsetY = { -it / 2 }
                                    ) + fadeIn(animationSpec = tween(400)) togetherWith
                                    slideOutVertically(
                                        animationSpec = tween(400),
                                        targetOffsetY = { it / 2 }
                                    ) + fadeOut(animationSpec = tween(400))
                                }
                            },
                            label = "rate_content"
                        ) { isEditMode ->
                            if (isEditMode) {
                                // Edit Mode - Input Fields
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.rateText,
                                        onValueChange = { rateViewModel.updateRate(it) },
                                        label = { Text(stringResource(R.string.milk_rate_per_liter)) },
                                        placeholder = { Text(stringResource(R.string.rate_hint)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                    
                                    OutlinedTextField(
                                        value = uiState.defaultQuantityText,
                                        onValueChange = { rateViewModel.updateDefaultQuantity(it) },
                                        label = { Text("Default Quantity (Liters)") },
                                        placeholder = { Text("Enter default milk quantity") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.LocalDrink,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                }
                            } else {
                                // Display Mode - Show saved values
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Rate per Liter",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "₹${uiState.rate}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocalDrink,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Default Quantity",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${uiState.defaultQuantity} Liters",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Animated Save Button
                AnimatedVisibility(
                    visible = uiState.saveButtonState != SaveButtonState.HIDDEN,
                    enter = slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    val buttonScale by animateFloatAsState(
                        targetValue = when (uiState.saveButtonState) {
                            SaveButtonState.SAVED -> 1.05f
                            else -> 1f
                        },
                        animationSpec = tween(200),
                        label = "button_scale"
                    )
                    
                    Button(
                        onClick = { 
                            currentUser?.uid?.let { userId ->
                                rateViewModel.saveRate(userId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(buttonScale),
                        enabled = when (uiState.saveButtonState) {
                            SaveButtonState.SAVE -> uiState.rate > 0 && uiState.defaultQuantity > 0
                            SaveButtonState.LOADING -> false
                            SaveButtonState.SAVED -> false
                            SaveButtonState.REMOVED -> false
                            SaveButtonState.HIDDEN -> false
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (uiState.saveButtonState) {
                                SaveButtonState.SAVED -> MaterialTheme.colorScheme.tertiary
                                SaveButtonState.REMOVED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            disabledContainerColor = when (uiState.saveButtonState) {
                                SaveButtonState.SAVED -> MaterialTheme.colorScheme.tertiary
                                SaveButtonState.REMOVED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            }
                        )
                    ) {
                        AnimatedContent(
                            targetState = uiState.saveButtonState,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                            },
                            label = "button_content"
                        ) { state ->
                            when (state) {
                                SaveButtonState.SAVE -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.save),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                SaveButtonState.LOADING -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Saving...",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                SaveButtonState.SAVED -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onTertiary
                                        )
                                        Text(
                                            text = "Saved!",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiary
                                        )
                                    }
                                }
                                SaveButtonState.REMOVED -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                        Text(
                                            text = "Removed!",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                                SaveButtonState.HIDDEN -> {
                                    // This state should not be visible due to AnimatedVisibility
                                }
                            }
                        }
                    }
                }
                
                // Cancel button when in edit mode (for existing data)
                AnimatedVisibility(
                    visible = uiState.isEditMode && uiState.hasData,
                    enter = slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { rateViewModel.cancelEdit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                } // End of else block
            }
        }
    }
} 