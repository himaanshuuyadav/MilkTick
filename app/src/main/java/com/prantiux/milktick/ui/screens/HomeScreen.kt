package com.prantiux.milktick.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.ui.components.MilkTickFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickSystemBarsGradient
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAppInitialized by appViewModel.isAppInitialized.collectAsState()
    val listState = rememberLazyListState()
    var showRateSheet by remember { mutableStateOf(false) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val shouldOpenRateFromIntent = (context as? ComponentActivity)?.intent?.getStringExtra("navigate_to") == "rates"
    val rateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(shouldOpenRateFromIntent) {
        if (shouldOpenRateFromIntent) {
            showRateSheet = true
            (context as? ComponentActivity)?.intent?.removeExtra("navigate_to")
        }
    }
    
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
                contentPadding = PaddingValues(top = 144.dp, bottom = 220.dp)
            ) {
                item {
                // Welcome header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
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
                }
                
                item {
                // Milk tracking card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
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
                                                minLines = 1,
                                                maxLines = 6,
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
                }
                
                item { Spacer(modifier = Modifier.height(1.dp)) }
            }

            MilkTickSystemBarsGradient()

            HomeSaveActionBar(
                uiState = uiState,
                bottomInset = bottomInset,
                onSave = {
                    currentUser?.uid?.let { userId ->
                        homeViewModel.saveMilkEntry(userId)
                    }
                }
            )

            MilkTickFloatingHeader(
                title = stringResource(R.string.home_title),
                scrollState = listState,
                actions = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                            .clickable { showRateSheet = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_fa_coins),
                            contentDescription = "Monthly Rate",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Monthly Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )

            if (showRateSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showRateSheet = false },
                    sheetState = rateSheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    RateEditorContent(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        bottomSpacing = 16.dp
                    )
                }
            }
        }
    }
} 

@Composable
private fun BoxScope.HomeSaveActionBar(
    uiState: com.prantiux.milktick.viewmodel.HomeUiState,
    bottomInset: Dp,
    onSave: () -> Unit
) {
    val shouldShowSaveButton = (uiState.brought && (uiState.isEditMode || !uiState.hasEntryToday)) ||
        (!uiState.brought && uiState.isEditMode && uiState.hasEntryToday)

    val shouldRender = when (uiState.saveButtonState) {
        SaveButtonState.SAVE, SaveButtonState.UPDATE, SaveButtonState.REMOVE -> shouldShowSaveButton
        SaveButtonState.LOADING, SaveButtonState.LOADING_UPDATE, SaveButtonState.LOADING_REMOVE,
        SaveButtonState.SAVED, SaveButtonState.UPDATED, SaveButtonState.REMOVED -> true
        SaveButtonState.HIDDEN -> false
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "homeSaveButtonPress"
    )
    val buttonBaseModifier = Modifier
        .align(Alignment.Center)
        .fillMaxWidth(0.72f)
        .height(56.dp)
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 24.dp)
            .padding(bottom = bottomInset + 84.dp)
    ) {
        AnimatedVisibility(
            visible = shouldRender,
            enter = slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Button(
                onClick = onSave,
                modifier = buttonBaseModifier,
                interactionSource = interactionSource,
                enabled = when (uiState.saveButtonState) {
                    SaveButtonState.SAVE -> if (uiState.brought) uiState.quantity > 0 else true
                    SaveButtonState.UPDATE -> uiState.quantity > 0
                    SaveButtonState.REMOVE -> true
                    else -> false
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (uiState.saveButtonState) {
                        SaveButtonState.REMOVE, SaveButtonState.LOADING_REMOVE, SaveButtonState.REMOVED -> MaterialTheme.colorScheme.error
                        SaveButtonState.SAVED, SaveButtonState.UPDATED -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    disabledContainerColor = when (uiState.saveButtonState) {
                        SaveButtonState.REMOVE, SaveButtonState.LOADING_REMOVE, SaveButtonState.REMOVED -> MaterialTheme.colorScheme.error
                        SaveButtonState.SAVED, SaveButtonState.UPDATED -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    }
                )
            ) {
                AnimatedContent(
                    targetState = uiState.saveButtonState,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(220),
                            initialOffsetY = { fullHeight -> -fullHeight / 2 }
                        ) + fadeIn(animationSpec = tween(220)) togetherWith
                            slideOutVertically(
                                animationSpec = tween(220),
                                targetOffsetY = { fullHeight -> fullHeight / 2 }
                            ) + fadeOut(animationSpec = tween(220))
                    },
                    label = "home_button_content"
                ) { state ->
                    Text(
                        text = when (state) {
                            SaveButtonState.SAVE -> "Save"
                            SaveButtonState.UPDATE -> "Update"
                            SaveButtonState.REMOVE -> "Remove Entry"
                            SaveButtonState.LOADING -> "Saving..."
                            SaveButtonState.LOADING_UPDATE -> "Updating..."
                            SaveButtonState.LOADING_REMOVE -> "Removing..."
                            SaveButtonState.SAVED -> "Saved!"
                            SaveButtonState.UPDATED -> "Updated!"
                            SaveButtonState.REMOVED -> "Removed!"
                            SaveButtonState.HIDDEN -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when (state) {
                            SaveButtonState.REMOVED, SaveButtonState.LOADING_REMOVE -> MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }
        }
    }
}