package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.ui.components.MilkTickCalendarScreenSkeleton
import com.prantiux.milktick.ui.components.MilkTickExpressiveLoader
import com.prantiux.milktick.ui.components.MilkTickSubpageFloatingHeader
import com.prantiux.milktick.ui.components.MilkTickSubpageHeaderActionButton
import com.prantiux.milktick.ui.components.MilkTickSubpageSystemBarsGradient
import com.prantiux.milktick.viewmodel.AuthViewModel
import com.prantiux.milktick.viewmodel.CalendarUiState
import com.prantiux.milktick.viewmodel.CalendarViewModel
import com.prantiux.milktick.viewmodel.EntryDetail
import com.prantiux.milktick.repository.AppGraph
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    year: Int,
    month: Int,
    authViewModel: AuthViewModel = viewModel(),
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by calendarViewModel.uiState.collectAsState()
    val currentUserId = currentUser?.uid
    
    val yearMonth = YearMonth.of(year, month)
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showExportMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }
    var showUpdateRateDialog by remember { mutableStateOf(false) }
    var showSkeleton by remember(yearMonth) { mutableStateOf(false) }
    var loadingStartedAtMs by remember(yearMonth) { mutableStateOf(System.currentTimeMillis()) }
    val isTargetMonthReady = uiState.loadedYearMonth == yearMonth
    val shouldRenderSkeleton = showSkeleton || !isTargetMonthReady
    
    LaunchedEffect(currentUserId, yearMonth) {
        val needsInitialSkeleton = uiState.loadedYearMonth != yearMonth
        showSkeleton = needsInitialSkeleton
        if (needsInitialSkeleton) {
            loadingStartedAtMs = System.currentTimeMillis()
        }
        currentUserId?.let { userId ->
            calendarViewModel.loadMonthData(userId, yearMonth)
        }
    }

    LaunchedEffect(uiState.loadedYearMonth, yearMonth, showSkeleton) {
        if (uiState.loadedYearMonth == yearMonth && showSkeleton) {
            val minSkeletonDurationMs = 350L
            val elapsedMs = System.currentTimeMillis() - loadingStartedAtMs
            if (elapsedMs < minSkeletonDurationMs) {
                delay(minSkeletonDurationMs - elapsedMs)
            }
            showSkeleton = false
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
            if (shouldRenderSkeleton) {
                MilkTickCalendarScreenSkeleton(message = "Loading calendar...")
            } else {
                MilkTickSubpageSystemBarsGradient()

                MilkTickSubpageFloatingHeader(
                    title = "$monthName $year",
                    onBackClick = { navController.popBackStack() },
                    actions = {
                        Box {
                            MilkTickSubpageHeaderActionButton(
                                onClick = { showMonthMenu = true },
                                contentDescription = "Options",
                                painter = painterResource(R.drawable.ic_fa_ellipsis_vertical),
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export Month Data") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_fa_download),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMonthMenu = false
                                        showExportDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Update Monthly Rate") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_fa_pen_to_square),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMonthMenu = false
                                        showUpdateRateDialog = true
                                    }
                                )
                            }
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 144.dp,
                        bottom = 24.dp
                    )
                ) {
                    // Calendar Grid
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Days of week header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                        
                        // Calendar days
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(300.dp)
                        ) {
                            val firstDayOfMonth = yearMonth.atDay(1)
                            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                            val daysInMonth = yearMonth.lengthOfMonth()
                            
                            // Empty cells for days before month starts
                            items(firstDayOfWeek) {
                                Box(modifier = Modifier.size(40.dp))
                            }
                            
                            // Days of the month
                            items(daysInMonth) { dayIndex ->
                                val day = dayIndex + 1
                                val date = yearMonth.atDay(day)
                                val hasEntry = uiState.datesWithEntries.contains(date)
                                val hasNoDelivery = uiState.datesWithNoDelivery.contains(date)
                                val isToday = date == LocalDate.now()
                                
                                DayCell(
                                    day = day,
                                    hasEntry = hasEntry,
                                    hasNoDelivery = hasNoDelivery,
                                    isToday = isToday,
                                    onClick = {
                                        selectedDate = date
                                        showDateDialog = true
                                    }
                                )
                            }
                        }
                            }
                        }
                    }
                
                    // Summary card
                    item {
                        Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Monthly Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${uiState.totalDays}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "Total Liters",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${uiState.totalLiters}L",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "Total Cost",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", uiState.totalCost)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                    // Payment Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header with edit button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_circle_check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Payment Status",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (!uiState.isEditingPayment) {
                                IconButton(
                                    onClick = { calendarViewModel.toggleEditMode() },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_pen_to_square),
                                        contentDescription = "Edit Payment",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        
                        // Payment status toggle buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Paid button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = uiState.isEditingPayment) { 
                                        if (uiState.isEditingPayment) {
                                            calendarViewModel.updatePaymentStatus(true)
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (uiState.isPaid) 
                                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = if (uiState.isPaid) 
                                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4CAF50))
                                else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_circle_check),
                                        contentDescription = null,
                                        tint = if (uiState.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Paid",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (uiState.isPaid) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Unpaid button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = uiState.isEditingPayment) { 
                                        if (uiState.isEditingPayment) {
                                            calendarViewModel.updatePaymentStatus(false)
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!uiState.isPaid) 
                                        Color(0xFFE57373).copy(alpha = 0.2f)
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = if (!uiState.isPaid) 
                                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE57373))
                                else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_circle_xmark),
                                        contentDescription = null,
                                        tint = if (!uiState.isPaid) Color(0xFFE57373) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Unpaid",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (!uiState.isPaid) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!uiState.isPaid) Color(0xFFE57373) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Payment note section
                        if (uiState.isEditingPayment || uiState.paymentNote.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Payment Note",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                if (uiState.isEditingPayment) {
                                OutlinedTextField(
                                    value = uiState.paymentNote,
                                    onValueChange = { calendarViewModel.updatePaymentNote(it) },
                                    placeholder = { Text("Add note about payment...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    minLines = 3,
                                    maxLines = 5
                                )
                            } else {
                                if (uiState.paymentNote.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = uiState.paymentNote,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            }
                        }
                        
                        // Action buttons (only shown when editing)
                        if (uiState.isEditingPayment) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { calendarViewModel.toggleEditMode() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                                }
                                
                                Button(
                                    onClick = { calendarViewModel.savePaymentNote() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_circle_check),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                            }
                        }
                    }
                }
            }
        }
        
        // Export message snackbar
        showExportMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                showExportMessage = null
            }
            
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showExportMessage = null }) {
                        Text("OK")
                    }
                }
            ) {
                Text(message)
            }
        }
        
        // Export Confirmation Dialog
        if (showExportDialog) {
            ExportDialog(
                yearMonth = yearMonth,
                context = context,
                uiState = uiState,
                onDismiss = { showExportDialog = false },
                onExportComplete = { message ->
                    showExportMessage = message
                    showExportDialog = false
                }
            )
        }
        
        // Update Monthly Rate Dialog
        if (showUpdateRateDialog) {
            currentUser?.let { user ->
                UpdateMonthlyRateDialog(
                    yearMonth = yearMonth,
                    userId = user.uid,
                    currentRate = (uiState.totalCost / uiState.totalLiters).toFloat(),
                    currentDefaultQuantity = uiState.defaultQuantity,
                    onDismiss = { showUpdateRateDialog = false },
                    onSave = { _, _ ->
                        showUpdateRateDialog = false
                        // Reload month data to reflect new rates
                        calendarViewModel.loadMonthData(user.uid, yearMonth)
                    }
                )
            }
        }
        
        // Date Detail Dialog
        if (showDateDialog && selectedDate != null) {
            DateDetailDialog(
                date = selectedDate!!,
                uiState = uiState,
                onDismiss = { 
                    showDateDialog = false
                    selectedDate = null
                },
                calendarViewModel = calendarViewModel
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    hasEntry: Boolean,
    hasNoDelivery: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    // Priority: Entry (green) > No Delivery (red) > Today (blue) > Default (gray)
    val backgroundColor = when {
        hasEntry -> Color(0xFF4CAF50) // Solid green for entries
        hasNoDelivery -> Color(0xFFE57373) // Solid red for no delivery
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        hasEntry || hasNoDelivery || isToday -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (hasEntry || hasNoDelivery) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun DateDetailDialog(
    date: LocalDate,
    uiState: CalendarUiState,
    onDismiss: () -> Unit,
    calendarViewModel: CalendarViewModel
) {
    val hasEntry = uiState.datesWithEntries.contains(date)
    val hasNoDelivery = uiState.datesWithNoDelivery.contains(date)
    val entryDetails = uiState.entryDetailsMap[date]
    
    var isEditing by remember { mutableStateOf(false) }
    var editedQuantity by remember { mutableStateOf(entryDetails?.quantity?.toString() ?: "") }
    var editedNote by remember { mutableStateOf(entryDetails?.note ?: "") }
    var didBringMilk by remember { mutableStateOf(hasEntry) }
    var isSaving by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE")),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!isEditing) {
                    IconButton(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
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
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isEditing) {
                    // View mode
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                hasEntry -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                hasNoDelivery -> Color(0xFFE57373).copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    when {
                                        hasEntry -> R.drawable.ic_fa_circle_check
                                        hasNoDelivery -> R.drawable.ic_fa_circle_xmark
                                        else -> R.drawable.ic_fa_circle_info
                                    }
                                ),
                                contentDescription = null,
                                tint = when {
                                    hasEntry -> Color(0xFF4CAF50)
                                    hasNoDelivery -> Color(0xFFE57373)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = when {
                                    hasEntry -> "Milk Delivered"
                                    hasNoDelivery -> "No Delivery"
                                    else -> "No Entry"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    hasEntry -> Color(0xFF4CAF50)
                                    hasNoDelivery -> Color(0xFFE57373)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    
                    if (entryDetails != null) {
                        HorizontalDivider()
                        
                        // Quantity display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantity",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${entryDetails.quantity}L",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        
                        // Note display
                        if (entryDetails.note?.isNotEmpty() == true) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Note",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = entryDetails.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "No details available for this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Edit mode
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Did you bring milk toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Did you bring milk?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (didBringMilk) "Yes, I brought milk" else "No milk today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = didBringMilk,
                                    onCheckedChange = { 
                                        didBringMilk = it
                                        if (it && editedQuantity.isEmpty()) {
                                            editedQuantity = uiState.defaultQuantity.toString()
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Quantity input (only if milk was brought)
                        if (didBringMilk) {
                            OutlinedTextField(
                                value = editedQuantity,
                                onValueChange = { editedQuantity = it },
                                label = { Text("Quantity (Liters)") },
                                placeholder = { Text("Enter quantity") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_glass_water),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            // Note input
                            OutlinedTextField(
                                value = editedNote,
                                onValueChange = { editedNote = it },
                                label = { Text("Note (Optional)") },
                                placeholder = { Text("Add a note...") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_fa_pen_to_square),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Button(
                    onClick = {
                        isSaving = true
                        calendarViewModel.updateMilkEntry(
                            date = date,
                            quantity = if (didBringMilk) editedQuantity.toFloatOrNull() ?: 0f else 0f,
                            brought = didBringMilk,
                            note = if (didBringMilk) editedNote else "",
                            onComplete = {
                                isSaving = false
                                isEditing = false
                            }
                        )
                    },
                    enabled = !isSaving && (!didBringMilk || (editedQuantity.toFloatOrNull() ?: 0f) > 0),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_fa_circle_check),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = if (isEditing) {
            {
                TextButton(
                    onClick = { 
                        isEditing = false
                        editedQuantity = entryDetails?.quantity?.toString() ?: ""
                        editedNote = entryDetails?.note ?: ""
                        didBringMilk = hasEntry
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        } else null
    )
}

@Composable
fun ExportDialog(
    yearMonth: YearMonth,
    @Suppress("UNUSED_PARAMETER") context: Context,
    uiState: CalendarUiState,
    onDismiss: () -> Unit,
    onExportComplete: (String) -> Unit
) {
    val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val fileName = "${monthName}_${yearMonth.year}.csv"
    
    // Calculate estimated file size
    val entryCount = uiState.entryDetailsMap.size
    val estimatedSize = ((entryCount * 100) + 500) / 1024.0 // Rough estimate in KB
    val fileSizeText = if (estimatedSize < 1) {
        "${(estimatedSize * 1024).toInt()} bytes"
    } else {
        String.format("%.1f KB", estimatedSize)
    }
    
    val documentsPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/MilkTick"
    
    var isExporting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fa_file_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Export Month Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Export data for $monthName $yearMonth",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider()
                
                // File details
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // File name
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
                            textAlign = TextAlign.End
                        )
                    }
                    
                    // File size
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
                    
                    // File location
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
                
                // Info card
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
                            text = "The file will be exported as CSV format with all delivery records and payment information.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            var exportCompleted by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            
            Button(
                onClick = {
                    if (exportCompleted) {
                        onDismiss()
                    } else {
                        isExporting = true
                        val message = exportMonthData(
                            yearMonth = yearMonth,
                            entryDetailsMap = uiState.entryDetailsMap,
                            isPaid = uiState.isPaid,
                            paymentNote = uiState.paymentNote
                        )
                        isExporting = false
                        exportCompleted = true
                        // Auto-dismiss after showing success for 1.5 seconds
                        scope.launch {
                            kotlinx.coroutines.delay(1500)
                            onExportComplete(message)
                        }
                    }
                },
                enabled = !isExporting,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (exportCompleted) 
                        MaterialTheme.colorScheme.tertiary 
                    else 
                        MaterialTheme.colorScheme.primary
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
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Exported", fontWeight = FontWeight.SemiBold)
                            }
                            else -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_file_arrow_down),
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
        },
        dismissButton = null
    )
}

// Export month data to CSV
fun exportMonthData(
    yearMonth: YearMonth,
    entryDetailsMap: Map<LocalDate, EntryDetail>,
    isPaid: Boolean,
    paymentNote: String
): String {
    return try {
        // Create MilkTick directory in Documents folder
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val milkTickDir = File(documentsDir, "MilkTick")
        if (!milkTickDir.exists()) {
            }

            // Create CSV file with month name
            val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val fileName = "${monthName}_${yearMonth.year}.csv"
            val csvFile = File(milkTickDir, fileName)

            FileWriter(csvFile).use { writer ->
                // Write CSV header
                writer.append("Date,Quantity (L),Note,Status\n")

                // Write data for each day in the month
                val daysInMonth = yearMonth.lengthOfMonth()
                for (day in 1..daysInMonth) {
                    val date = yearMonth.atDay(day)
                    val entryDetail = entryDetailsMap[date]

                    // Use text format to prevent #### in Excel
                    val dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    val quantity = entryDetail?.quantity?.toString() ?: "0"
                    val note = entryDetail?.note?.replace(",", ";")?.replace("\n", " ") ?: ""
                    val status = if (entryDetail != null) "Delivered" else "Not Delivered"

                    // Wrap date in quotes to force text format in Excel
                    writer.append("\"$dateStr\",$quantity,\"$note\",$status\n")
                }

                // Add payment information at the end
                writer.append("\n")
                writer.append("Payment Status,${if (isPaid) "Paid" else "Unpaid"},,\n")
                if (paymentNote.isNotEmpty()) {
                    writer.append("Payment Note,\"${paymentNote.replace(",", ";").replace("\n", " ") }\",,\n")
                }
            }

            "Exported successfully to: ${csvFile.absolutePath}"
        } catch (e: Exception) {
            "Export failed: ${e.message}"
        }
    }

    @Composable
    fun UpdateMonthlyRateDialog(
        yearMonth: YearMonth,
        userId: String,
        currentRate: Float,
        currentDefaultQuantity: Float,
        onDismiss: () -> Unit,
        onSave: (Float, Float) -> Unit
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(Unit) {
            AppGraph.initialize(context)
        }
        val repository = remember { AppGraph.mainRepository }
        val scope = rememberCoroutineScope()

        var rateText by remember { mutableStateOf(if (currentRate > 0) currentRate.toString() else "") }
        var defaultQtyText by remember { mutableStateOf(if (currentDefaultQuantity > 0) currentDefaultQuantity.toString() else "") }
        var isSaving by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        AlertDialog(
            onDismissRequest = { if (!isSaving) onDismiss() },
            title = {
                Column {
                    Text(
                        "Update Monthly Rate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$monthName ${yearMonth.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = rateText.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                "Milk Rate Per Liter",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = rateText,
                            onValueChange = {
                                rateText = it
                                errorMessage = null
                            },
                            label = { Text("Milk Rate Per Liter") },
                            placeholder = { Text("Enter rate in rupees") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_indian_rupee_sign),
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = defaultQtyText.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                "Default Quantity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedTextField(
                            value = defaultQtyText,
                            onValueChange = {
                                defaultQtyText = it
                                errorMessage = null
                            },
                            label = { Text("Default Quantity") },
                            placeholder = { Text("Enter quantity in liters") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_glass_water),
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = rateText.toFloatOrNull()
                        val defaultQty = defaultQtyText.toFloatOrNull()

                        when {
                            rate == null || rate <= 0 -> {
                                errorMessage = "Please enter a valid rate"
                            }
                            defaultQty == null || defaultQty <= 0 -> {
                                errorMessage = "Please enter a valid quantity"
                            }
                            else -> {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val monthlyRate = com.prantiux.milktick.data.MonthlyRate(
                                            yearMonth = yearMonth,
                                            ratePerLiter = rate,
                                            defaultQuantity = defaultQty,
                                            userId = userId
                                        )
                                        repository.saveMonthlyRate(monthlyRate)
                                        onSave(rate, defaultQty)
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to save: ${e.message}"
                                        isSaving = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        if (isSaving) "Saving..." else "Save",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            dismissButton = null
        )
    }
