package com.prantiux.milktick.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.SkeletonHomeWelcomeCard
import com.prantiux.milktick.ui.components.SkeletonHomeMilkCard
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.HomeViewModel
import com.prantiux.milktick.viewmodel.AppViewModel
import com.prantiux.milktick.viewmodel.SaveButtonState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAppInitialized by appViewModel.isAppInitialized.collectAsState()
    
    // Only load data once when app initializes
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
            if (!isAppInitialized) {
                homeViewModel.loadDefaultQuantity(userId)
                appViewModel.initializeApp(userId)
            }
        }
    }
    
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            homeViewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Welcome header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fa_mug_hot),
                                contentDescription = "Milk",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE")),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Milk tracking card
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
                                text = "Today's Milk Entry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Edit button - only show when data exists and not in edit mode
                            AnimatedVisibility(
                                visible = uiState.hasEntryToday && !uiState.isEditMode,
                                enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
                                exit = scaleOut(animationSpec = tween(300)) + fadeOut()
                            ) {
                                IconButton(
                                    onClick = { homeViewModel.toggleEditMode() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_pen_to_square),
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // Content based on loading state
                        if (uiState.isCheckingEntry) {
                            // Show skeleton loading content inside the card
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Toggle row skeleton
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                            modifier = Modifier.width(150.dp),
                                            height = 16.dp
                                        )
                                        com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                            modifier = Modifier.width(120.dp),
                                            height = 14.dp
                                        )
                                    }
                                    com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                        modifier = Modifier.size(48.dp, 24.dp),
                                        height = 24.dp,
                                        cornerRadius = 12.dp
                                    )
                                }
                                
                                // Input fields skeleton
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 56.dp,
                                        cornerRadius = 8.dp
                                    )
                                    com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 80.dp,
                                        cornerRadius = 8.dp
                                    )
                                }
                                
                                // Save button skeleton
                                com.prantiux.milktick.ui.components.SkeletonLoadingBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 56.dp,
                                    cornerRadius = 16.dp
                                )
                            }
                        } else {
                            // Did you bring milk toggle - always visible
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.did_you_bring_milk),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (uiState.brought) {
                                            if (uiState.hasEntryToday && !uiState.isEditMode) "Entry completed for today" 
                                            else "Great! Let's log the details"
                                        } else "No worries, maybe tomorrow",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.hasEntryToday && !uiState.isEditMode) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.brought,
                                    onCheckedChange = { homeViewModel.updateBrought(it) },
                                    enabled = !uiState.hasEntryToday || uiState.isEditMode,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                            
                            // Quantity and Note - animated between input and display modes (only if brought)
                            if (uiState.brought) {
                                AnimatedContent(
                                    targetState = uiState.isEditMode || !uiState.hasEntryToday,
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
                                    label = "entry_content"
                                ) { isEditMode ->
                                    if (isEditMode) {
                                        // Edit Mode - Input Fields
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = uiState.quantityText,
                                                onValueChange = { homeViewModel.updateQuantity(it) },
                                                label = { Text(stringResource(R.string.quantity)) },
                                                placeholder = { Text(stringResource(R.string.quantity_hint)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                ),
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_fa_glass_water),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            )
                                            
                                            OutlinedTextField(
                                                value = uiState.note,
                                                onValueChange = { homeViewModel.updateNote(it) },
                                                label = { Text(stringResource(R.string.note)) },
                                                placeholder = { Text(stringResource(R.string.note_hint)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 3,
                                                shape = RoundedCornerShape(16.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                ),
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(R.drawable.ic_fa_note_sticky),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            )
                                        }
                                    } else {
                                        // Display Mode - Show saved values (like RateScreen)
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
                                                        painter = painterResource(R.drawable.ic_fa_glass_water),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = "Quantity",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = "${uiState.quantity} Liters",
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
                                                        painter = painterResource(R.drawable.ic_fa_note_sticky),
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = "Note",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = if (uiState.note.isNotBlank()) uiState.note else "No note added",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (uiState.note.isNotBlank()) 
                                                                MaterialTheme.colorScheme.onSurface 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Save button - show only when needed
                when (uiState.saveButtonState) {
                    SaveButtonState.SAVE -> {
                        // Debug logging for button visibility
                        android.util.Log.d("HomeScreen", "Button conditions - brought: ${uiState.brought}, isEditMode: ${uiState.isEditMode}, hasEntryToday: ${uiState.hasEntryToday}")
                        
                        val shouldShowButton = (uiState.brought && (uiState.isEditMode || !uiState.hasEntryToday)) || 
                                             (!uiState.brought && uiState.isEditMode && uiState.hasEntryToday)
                        
                        android.util.Log.d("HomeScreen", "Should show button: $shouldShowButton")
                        
                        if (shouldShowButton) {
                            Button(
                                onClick = { 
                                    android.util.Log.d("HomeScreen", "Button clicked - will call saveMilkEntry")
                                    currentUser?.uid?.let { userId ->
                                        homeViewModel.saveMilkEntry(userId)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = if (uiState.brought) uiState.quantity > 0 else true,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!uiState.brought && uiState.isEditMode) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (!uiState.brought && uiState.isEditMode && uiState.hasEntryToday) {
                                        "Remove Entry"
                                    } else if (uiState.isEditMode) {
                                        "Update"
                                    } else {
                                        stringResource(R.string.save)
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    SaveButtonState.LOADING -> {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    SaveButtonState.SAVED -> {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text(
                                text = "Saved ✓",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    SaveButtonState.REMOVED -> {
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Entry Removed ✓",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    SaveButtonState.HIDDEN -> {
                        // No button shown
                    }
                }
            }
        }
    }
} 