package com.prantiux.milktick.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prantiux.milktick.R
import com.prantiux.milktick.ui.components.MilkTickRateEditorSkeleton
import com.prantiux.milktick.viewmodel.AppViewModel
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.RateViewModel
import com.prantiux.milktick.viewmodel.SaveButtonState
import java.time.format.DateTimeFormatter

@Composable
fun RateEditorContent(
    modifier: Modifier = Modifier,
    bottomSpacing: Dp = 16.dp,
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

    Column(
        modifier = modifier
            .padding(bottom = bottomSpacing),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(24.dp),
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
                            painter = painterResource(R.drawable.ic_fa_arrow_trend_up),
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

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                MilkTickRateEditorSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .animateContentSize(animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)),
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

                        AnimatedVisibility(
                            visible = uiState.hasData,
                            enter = scaleIn(animationSpec = tween(300)) + fadeIn(),
                            exit = scaleOut(animationSpec = tween(300)) + fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    if (uiState.isEditMode) {
                                        rateViewModel.cancelEdit()
                                    } else {
                                        rateViewModel.enableEditMode()
                                    }
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                AnimatedContent(
                                    targetState = uiState.isEditMode,
                                    transitionSpec = {
                                        (scaleIn(animationSpec = tween(220)) + fadeIn(animationSpec = tween(220))) togetherWith
                                            (scaleOut(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)))
                                    },
                                    label = "rateEditCancelIcon"
                                ) { isEditMode ->
                                    Icon(
                                        painter = painterResource(
                                            if (isEditMode) R.drawable.ic_fa_circle_xmark else R.drawable.ic_fa_pen_to_square
                                        ),
                                        contentDescription = if (isEditMode) "Cancel edit" else "Edit",
                                        tint = if (isEditMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

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
                                painter = painterResource(R.drawable.ic_fa_calendar_day),
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
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    )
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
                                    )
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        }


        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val buttonModifier = Modifier
            .fillMaxWidth(0.72f)
            .align(Alignment.CenterHorizontally)
            .height(56.dp)

        AnimatedVisibility(
            visible = !uiState.isLoading && uiState.saveButtonState != SaveButtonState.HIDDEN,
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
                targetValue = if (uiState.saveButtonState == SaveButtonState.SAVED) 1.05f else 1f,
                animationSpec = tween(200),
                label = "button_scale"
            )
            val pressScale by animateFloatAsState(
                targetValue = if (isPressed) 0.97f else 1f,
                animationSpec = tween(140, easing = FastOutSlowInEasing),
                label = "rateButtonPress"
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        currentUser?.uid?.let { userId ->
                            rateViewModel.saveRate(userId)
                        }
                    },
                    modifier = buttonModifier
                        .graphicsLayer {
                            scaleX = buttonScale * pressScale
                            scaleY = buttonScale * pressScale
                        },
                    interactionSource = interactionSource,
                    enabled = when (uiState.saveButtonState) {
                        SaveButtonState.SAVE -> uiState.rate > 0 && uiState.defaultQuantity > 0
                        SaveButtonState.UPDATE -> uiState.rate > 0 && uiState.defaultQuantity > 0
                        SaveButtonState.REMOVE -> false
                        SaveButtonState.LOADING -> false
                        SaveButtonState.LOADING_UPDATE -> false
                        SaveButtonState.LOADING_REMOVE -> false
                        SaveButtonState.SAVED -> false
                        SaveButtonState.UPDATED -> false
                        SaveButtonState.REMOVED -> false
                        SaveButtonState.HIDDEN -> false
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (uiState.saveButtonState) {
                                SaveButtonState.SAVED, SaveButtonState.UPDATED -> MaterialTheme.colorScheme.tertiary
                                SaveButtonState.REMOVED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        disabledContainerColor = when (uiState.saveButtonState) {
                                SaveButtonState.SAVED, SaveButtonState.UPDATED -> MaterialTheme.colorScheme.tertiary
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
                            SaveButtonState.SAVE -> Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            SaveButtonState.UPDATE -> Text(
                                text = "Update",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            SaveButtonState.LOADING -> Text(
                                text = "Saving...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            SaveButtonState.LOADING_UPDATE -> Text(
                                text = "Updating...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            SaveButtonState.SAVED -> Text(
                                text = "Saved!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )

                            SaveButtonState.UPDATED -> Text(
                                text = "Updated!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )

                            SaveButtonState.REMOVED -> Text(
                                text = "Removed!",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onError
                            )

                            SaveButtonState.REMOVE -> Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            SaveButtonState.LOADING_REMOVE -> Text(
                                text = "Removing...",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onError
                            )

                            SaveButtonState.HIDDEN -> Unit
                        }
                    }
                }
            }
        }

    }
}
