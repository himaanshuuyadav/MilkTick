package com.prantiux.milktick.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.MilkTickFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickSummaryContentSkeleton
import com.prantiux.milktick.ui.components.MilkTickSystemBarsGradient
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.SummaryViewModel
import com.prantiux.milktick.viewmodel.AppViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    summaryViewModel: SummaryViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val uiState by summaryViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId by appViewModel.currentUserId.collectAsState()
    val dataRefreshTrigger by appViewModel.dataRefreshTrigger.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
        }
    }
    
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            summaryViewModel.setCurrentUserId(userId)
        }
    }
    
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            summaryViewModel.clearExportMessage()
        }
    }
    
    // Listen for data refresh triggers from other screens
    LaunchedEffect(dataRefreshTrigger) {
        android.util.Log.d("SummaryScreen", "Data refresh trigger changed: $dataRefreshTrigger, currentUserId: $currentUserId")
        if (dataRefreshTrigger > 0 && currentUserId != null) {
            android.util.Log.d("SummaryScreen", "Calling summaryViewModel.refreshData()")
            summaryViewModel.refreshData()
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
                contentPadding = PaddingValues(top = 144.dp, bottom = 100.dp)
            ) {
                item {
                    // Month/Year Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fa_calendar_days),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = "Monthly Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.selectedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                if (uiState.isLoading) {
                    item {
                        MilkTickSummaryContentSkeleton(modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    // Main Statistics Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernSummaryCard(
                                icon = R.drawable.ic_fa_circle_info,
                                title = "Days",
                                value = "${uiState.totalDays}",
                                subtitle = "Milk brought",
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModernSummaryCard(
                                icon = R.drawable.ic_fa_bell,
                                title = "Total",
                                value = "${uiState.totalLiters}L",
                                subtitle = "Milk Brought",
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Cost Summary Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_circle_info),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total Amount",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", uiState.totalCost)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    // Quick Stats Section
                    item {
                        Text(
                            text = "Quick Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickStatCard(
                                icon = R.drawable.ic_fa_circle_info,
                                label = "Avg per day",
                                value = if (uiState.totalDays > 0) "${String.format("%.1f", uiState.totalLiters / uiState.totalDays)}L" else "0L",
                                modifier = Modifier.weight(1f)
                            )
                            
                            QuickStatCard(
                                icon = R.drawable.ic_fa_circle_info,
                                label = "Active days",
                                value = "${if (uiState.selectedYearMonth.lengthOfMonth() > 0) (uiState.totalDays * 100 / uiState.selectedYearMonth.lengthOfMonth()) else 0}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Recent Entries Section (if any)
                    if (uiState.entries.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Entries",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        
                        items(uiState.entries.take(5)) { entry ->
                            EntryCard(entry = entry)
                        }
                        
                        if (uiState.entries.size > 5) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "... and ${uiState.entries.size - 5} more entries",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Export Section
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { summaryViewModel.exportToCsv(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !uiState.isExporting && !uiState.isExported && uiState.entries.isNotEmpty(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isExported) 
                                    MaterialTheme.colorScheme.tertiary 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            when {
                                uiState.isExporting -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                }
                                uiState.isExported -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_fa_circle_info),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Exported",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                else -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_fa_download),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.export_data),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            MilkTickSystemBarsGradient()

            MilkTickFloatingHeader(
                title = stringResource(R.string.summary_title),
                scrollState = listState,
                actions = {
                    IconButton(onClick = { summaryViewModel.exportToCsv(context) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_fa_download),
                            contentDescription = stringResource(R.string.export_data),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ModernSummaryCard(
    icon: Int,
    title: String,
    value: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickStatCard(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EntryCard(
    entry: com.prantiux.milktick.data.MilkEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date circle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (entry.brought) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.brought) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Entry details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (entry.brought) "${entry.quantity}L delivered" else "No milk",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
            
            // Status icon
            Icon(
                painter = painterResource(if (entry.brought) R.drawable.ic_fa_circle_check else R.drawable.ic_fa_circle_xmark),
                contentDescription = null,
                tint = if (entry.brought) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}