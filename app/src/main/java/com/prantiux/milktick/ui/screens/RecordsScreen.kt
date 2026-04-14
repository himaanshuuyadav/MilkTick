package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.MilkTickExportPreviewSkeleton
import com.prantiux.milktick.ui.components.MilkTickExpressiveLoader
import com.prantiux.milktick.ui.components.MilkTickFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickRecordsMonthListSkeleton
import com.prantiux.milktick.ui.components.MilkTickSystemBarsGradient
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.RecordsViewModel
import com.prantiux.milktick.viewmodel.AppViewModel
import java.time.Month
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    recordsViewModel: RecordsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val uiState by recordsViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId by appViewModel.currentUserId.collectAsState()
    val dataRefreshTrigger by appViewModel.dataRefreshTrigger.collectAsState()
    val listState = rememberLazyListState()
    
    var showYearDropdown by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            appViewModel.updateCurrentUser(userId)
        }
    }
    
    // Only set user ID when app is initialized, avoid reloading data
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            recordsViewModel.setCurrentUserId(userId)
        }
    }
    
    // Listen for data refresh triggers from other screens
    LaunchedEffect(dataRefreshTrigger) {
        if (dataRefreshTrigger > 0 && currentUserId != null) {
            recordsViewModel.refreshData()
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
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(top = 144.dp, bottom = 120.dp)
            ) {
                item {
                // Year selector card
                val yearArrowRotation by animateFloatAsState(
                    targetValue = if (showYearDropdown) 180f else 0f,
                    animationSpec = tween(durationMillis = 220),
                    label = "yearSelectorArrowRotation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Year",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box {
                                val yearSelectInteraction = remember { MutableInteractionSource() }
                                Card(
                                    modifier = Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = yearSelectInteraction
                                        ) { showYearDropdown = !showYearDropdown },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = uiState.selectedYear.toString(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            painter = painterResource(R.drawable.ic_fa_chevron_down),
                                            contentDescription = "Select Year",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .rotate(yearArrowRotation)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }

                item {
                    AnimatedVisibility(
                        visible = showYearDropdown,
                        enter = expandVertically(animationSpec = tween(240)) + fadeIn(animationSpec = tween(180)),
                        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(160))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.availableYears.forEach { year ->
                                    val yearOptionInteraction = remember(year) { MutableInteractionSource() }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                indication = null,
                                                interactionSource = yearOptionInteraction
                                            ) {
                                                recordsViewModel.setSelectedYear(year)
                                                showYearDropdown = false
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (year == uiState.selectedYear)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = year.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (year == uiState.selectedYear) FontWeight.Bold else FontWeight.Medium,
                                                color = if (year == uiState.selectedYear)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )

                                            if (year == uiState.selectedYear) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_fa_circle_check),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MilkTickExpressiveLoader(message = "Loading records...")
                            }
                        }
                    }
                    item {
                        MilkTickRecordsMonthListSkeleton(count = 12)
                    }
                } else {
                    val months = Month.values().toList()
                    itemsIndexed(months) { index, month ->
                        MonthCard(
                            month = month,
                            year = uiState.selectedYear,
                            cardShape = groupedCardShape(index = index, lastIndex = months.lastIndex),
                            summaryData = uiState.monthlySummaries.find { summary ->
                                summary.yearMonth.monthValue == month.value
                            },
                            onMonthClick = { selectedMonth ->
                                navController.navigate(
                                    Screen.Calendar.createRoute(
                                        year = uiState.selectedYear,
                                        month = selectedMonth.value
                                    )
                                )
                            }
                        )
                    }
                }
            }

            MilkTickSystemBarsGradient()

            MilkTickFloatingHeader(
                title = stringResource(R.string.records_title),
                scrollState = listState,
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fa_ellipsis_vertical),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false },
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Export ${uiState.selectedYear} Data",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showExportMenu = false
                                    showExportDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_download),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
        
        // Export Year Data Dialog
        if (showExportDialog) {
            currentUserId?.let { userId ->
                YearExportDialog(
                    year = uiState.selectedYear,
                    userId = userId,
                    onDismiss = { showExportDialog = false },
                    onExportComplete = { message ->
                        exportMessage = message
                        showExportDialog = false
                    }
                )
            }
        }
        
        // Show export result message
        exportMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                exportMessage = null
            }
            
            androidx.compose.material3.Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { exportMessage = null }) {
                        Text("OK")
                    }
                }
            ) {
                Text(message)
            }
        }
    }
}

@Composable
fun MonthCard(
    month: Month,
    @Suppress("UNUSED_PARAMETER") year: Int,
    cardShape: RoundedCornerShape,
    summaryData: com.prantiux.milktick.viewmodel.MonthlySummaryData?,
    onMonthClick: (Month) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable { onMonthClick(month) },
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = month.name.lowercase().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${summaryData?.totalDays ?: 0} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1fL", summaryData?.totalLiters ?: 0.0f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "₹${String.format("%.0f", summaryData?.totalCost ?: 0.0f)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun groupedCardShape(index: Int, lastIndex: Int): RoundedCornerShape {
    return when {
        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        index == lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(8.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearExportDialog(
    year: Int,
    userId: String,
    onDismiss: () -> Unit,
    onExportComplete: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.prantiux.milktick.repository.AppGraph.initialize(context)
    }
    val repository = remember { com.prantiux.milktick.repository.AppGraph.mainRepository }
    
    var isExporting by remember { mutableStateOf(false) }
    var entriesData by remember { mutableStateOf<List<com.prantiux.milktick.data.MilkEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load year data
    LaunchedEffect(year, userId) {
        repository.getMilkEntriesForYear(userId, year).collect { entries ->
            entriesData = entries
            isLoading = false
        }
    }
    
    val fileName = "MilkTick_${year}.csv"
    val entryCount = entriesData.size
    val estimatedSize = ((entryCount * 100) + 500) / 1024.0
    val fileSizeText = if (estimatedSize < 1) {
        "${(estimatedSize * 1024).toInt()} bytes"
    } else {
        String.format("%.1f KB", estimatedSize)
    }
    
    val documentsPath = "${android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS).absolutePath}/MilkTick"
    
    var exportCompleted by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }
    var shouldExport by remember { mutableStateOf(false) }
    
    // Handle export in coroutine
    LaunchedEffect(shouldExport) {
        if (shouldExport) {
            isExporting = true
            exportMessage = exportYearData(
                year = year,
                userId = userId,
                entries = entriesData
            )
            isExporting = false
            exportCompleted = true
            shouldExport = false
        }
    }
    
    LaunchedEffect(exportCompleted) {
        if (exportCompleted) {
            kotlinx.coroutines.delay(1500)
            onExportComplete(exportMessage)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isExporting) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_download),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Export Year Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MilkTickExportPreviewSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Export all data for $year",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "File Name:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Entries:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$entryCount records",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estimated Size:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = fileSizeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Save Location:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$documentsPath/$fileName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fa_circle_info),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "The file will contain all $year delivery records with dates, quantities, and notes in CSV format.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (exportCompleted) {
                        onDismiss()
                    } else {
                        shouldExport = true
                    }
                },
                enabled = !isExporting && !isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (exportCompleted) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = if (isExporting) "exporting" else if (exportCompleted) "completed" else "ready",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "export_animation"
                ) { state ->
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        when (state) {
                            "exporting" -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Exporting...", fontWeight = FontWeight.SemiBold)
                            }

                            "completed" -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_circle_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Exported", fontWeight = FontWeight.SemiBold)
                            }

                            else -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Export", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (!exportCompleted) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

suspend fun exportYearData(
    year: Int,
    userId: String,
    entries: List<com.prantiux.milktick.data.MilkEntry>
): String {
    return try {
        val repository = com.prantiux.milktick.repository.AppGraph.mainRepository
        
        // Create MilkTick directory in Documents folder
        val documentsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
        val milkTickDir = java.io.File(documentsDir, "MilkTick")
        if (!milkTickDir.exists()) {
            milkTickDir.mkdirs()
        }
        
        // Create CSV file
        val fileName = "MilkTick_${year}.csv"
        val csvFile = java.io.File(milkTickDir, fileName)
        
        java.io.FileWriter(csvFile).use { writer ->
            // Write CSV header
            writer.append("Month,Days Delivered,Total Liters,Payment Status,Amount to Pay\n")
            
            var totalDaysAllMonths = 0
            var totalLitersAllMonths = 0.0
            var totalAmountAllMonths = 0.0
            
            // Process each month
            for (month in 1..12) {
                val yearMonth = java.time.YearMonth.of(year, month)
                val monthName = yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
                
                // Get entries for this month
                val monthEntries = entries.filter { entry ->
                    entry.date.year == year && entry.date.monthValue == month
                }
                
                // Calculate monthly totals
                val daysDelivered = monthEntries.count { it.brought }
                val totalLiters = monthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() }
                
                // Get rate and payment status
                val rate = repository.getMonthlyRate(userId, yearMonth)
                val payment = repository.getMonthlyPayment(userId, yearMonth)
                val ratePerLiter = rate?.ratePerLiter ?: 0f
                val amountToPay = totalLiters * ratePerLiter
                val paymentStatus = if (payment?.isPaid == true) "Paid" else "Unpaid"
                
                // Write month data
                writer.append("$monthName,$daysDelivered,${String.format("%.2f", totalLiters)},$paymentStatus,${String.format("%.2f", amountToPay)}\n")
                
                // Add to totals
                totalDaysAllMonths += daysDelivered
                totalLitersAllMonths += totalLiters
                totalAmountAllMonths += amountToPay
            }
            
            // Add grand totals
            writer.append("\n")
            writer.append("TOTAL,$totalDaysAllMonths,${String.format("%.2f", totalLitersAllMonths)},,${String.format("%.2f", totalAmountAllMonths)}\n")
        }
        
        "Exported successfully to: ${csvFile.absolutePath}"
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}
