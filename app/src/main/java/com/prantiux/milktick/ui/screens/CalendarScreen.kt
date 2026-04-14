package com.prantiux.milktick.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt

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
    val calendarCellSize = 40.dp
    val calendarCellSpacing = 4.dp
    val calendarGridEdgePadding = 1.dp
    val firstDayOfWeek = remember(yearMonth) { yearMonth.atDay(1).dayOfWeek.value % 7 }
    val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }
    val calendarRowCount = remember(yearMonth) { (firstDayOfWeek + daysInMonth + 6) / 7 }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDateDialog by remember { mutableStateOf(false) }
    val calendarGridHeight = remember(calendarRowCount) {
        (calendarCellSize * calendarRowCount) +
            (calendarCellSpacing * (calendarRowCount - 1)) +
            (calendarGridEdgePadding * 2)
    }
    val calendarBottomInset by animateDpAsState(
        targetValue = if (showDateDialog && selectedDate != null) 440.dp else 24.dp,
        label = "calendarBottomInset"
    )
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var showExportMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }
    var showUpdateRateDialog by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    val dayCellBounds = remember { mutableStateMapOf<LocalDate, Rect>() }
    var showSkeleton by remember(yearMonth) { mutableStateOf(false) }
    var loadingStartedAtMs by remember(yearMonth) { mutableStateOf(System.currentTimeMillis()) }
    val isTargetMonthReady = uiState.loadedYearMonth == yearMonth
    val shouldRenderSkeleton = showSkeleton || !isTargetMonthReady

    LaunchedEffect(yearMonth) {
        dayCellBounds.clear()
    }

    BackHandler(enabled = showDateDialog && selectedDate != null) {
        showDateDialog = false
    }
    
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
                MilkTickCalendarScreenSkeleton(
                    message = "Loading calendar...",
                    calendarRowCount = calendarRowCount
                )
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
                                shape = RoundedCornerShape(24.dp),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
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
                        bottom = calendarBottomInset
                    )
                ) {
                    // Calendar Grid
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(24.dp)
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
                            modifier = Modifier.height(calendarGridHeight),
                            contentPadding = PaddingValues(vertical = calendarGridEdgePadding)
                        ) {
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
                                    isSelected = selectedDate == date && showDateDialog,
                                    modifier = Modifier.onGloballyPositioned { coordinates ->
                                        dayCellBounds[date] = coordinates.boundsInRoot()
                                    },
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(24.dp)
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
                
                    // Payment Ledger Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Payments",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Cleared",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "₹${String.format("%.2f", uiState.amountPaid)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Open Balance",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "₹${String.format("%.2f", uiState.carryDueAmount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (uiState.carryDueAmount > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                val clearedIn = uiState.dueClearedIn
                                if (clearedIn != null) {
                                    Text(
                                        text = "Cleared in ${clearedIn.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${clearedIn.year}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                val manageButtonInteraction = remember { MutableInteractionSource() }
                                val managePressed by manageButtonInteraction.collectIsPressedAsState()
                                val manageCorner by animateDpAsState(
                                    targetValue = if (managePressed) 18.dp else 28.dp,
                                    label = "manageButtonCorner"
                                )

                                Button(
                                    onClick = { showPaymentSheet = true },
                                    interactionSource = manageButtonInteraction,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(manageCorner),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Text(
                                        text = "Manage Payments",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
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
        
        if (showDateDialog && selectedDate != null) {
            DateSheetScrimOverlay(
                holeRects = dayCellBounds.values.toList(),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Date Detail Sheet
        AnimatedVisibility(
            visible = showDateDialog && selectedDate != null,
            enter = fadeIn(animationSpec = tween(180)) + slideInVertically(animationSpec = tween(260)) { it / 2 },
            exit = fadeOut(animationSpec = tween(140)) + slideOutVertically(animationSpec = tween(220)) { it / 2 }
        ) {
            selectedDate?.let { activeDate ->
                DateDetailDialog(
                    date = activeDate,
                    uiState = uiState,
                    onDismiss = {
                        showDateDialog = false
                    },
                    calendarViewModel = calendarViewModel
                )
            }
        }

        if (showPaymentSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPaymentSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                PaymentLedgerSheet(
                    uiState = uiState,
                    onAmountChange = calendarViewModel::updatePaymentInput,
                    onNoteChange = calendarViewModel::updatePaymentNote,
                    onSave = {
                        calendarViewModel.savePaymentRecord {
                            showPaymentSheet = false
                        }
                    },
                    onAddPreviousDue = {
                        calendarViewModel.savePreviousDueAdjustment {
                            showPaymentSheet = false
                        }
                    },
                    onDeleteRecord = { recordId ->
                        calendarViewModel.deletePaymentRecord(recordId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun DateSheetScrimOverlay(
    holeRects: List<Rect>,
    modifier: Modifier = Modifier
) {
    val holeCornerRadius = with(LocalDensity.current) { 14.dp.toPx() }
    Box(
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawRect(Color.Black.copy(alpha = 0.34f))
                holeRects.forEach { rect ->
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        cornerRadius = CornerRadius(holeCornerRadius, holeCornerRadius),
                        blendMode = BlendMode.Clear
                    )
                }
            }
    )
}

@Composable
private fun PaymentLedgerSheet(
    uiState: CalendarUiState,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onAddPreviousDue: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedRecordId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Record Payment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Settled: ₹${String.format("%.2f", uiState.amountPaid)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Open Balance: ₹${String.format("%.2f", uiState.carryDueAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.carryDueAmount > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
        }

        val sheetClearedIn = uiState.dueClearedIn
        if (sheetClearedIn != null) {
            Text(
                text = "Due cleared in ${sheetClearedIn.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${sheetClearedIn.year}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32)
            )
        }

        if (uiState.advanceCredit > 0) {
            Text(
                text = "Advance Credit: ₹${String.format("%.2f", uiState.advanceCredit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E7D32)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.paymentInputAmount,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                placeholder = { Text("Enter payment amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )

            OutlinedTextField(
                value = uiState.paymentInputNote,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                placeholder = { Text("UPI / cash / previous due details") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                minLines = 2,
                maxLines = 4
            )
        }

        Text(
            text = "Tip: Add Payment reduces due. Add Previous Due increases carry due.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAddPaymentLoading = uiState.activePaymentAction == com.prantiux.milktick.viewmodel.PaymentAction.ADD_PAYMENT && uiState.isSavingPayment
            val isAddDueLoading = uiState.activePaymentAction == com.prantiux.milktick.viewmodel.PaymentAction.ADD_PREVIOUS_DUE && uiState.isSavingPayment

            val addPaymentInteraction = remember { MutableInteractionSource() }
            val addPaymentPressed by addPaymentInteraction.collectIsPressedAsState()
            val addPaymentInnerCorner by animateDpAsState(
                targetValue = if (addPaymentPressed || isAddPaymentLoading) 28.dp else 4.dp,
                label = "addPaymentInnerCorner"
            )

            val addDueInteraction = remember { MutableInteractionSource() }
            val addDuePressed by addDueInteraction.collectIsPressedAsState()
            val addDueInnerCorner by animateDpAsState(
                targetValue = if (addDuePressed || isAddDueLoading) 28.dp else 4.dp,
                label = "addDueInnerCorner"
            )

            val leftButtonShape = RoundedCornerShape(
                topStart = 28.dp,
                bottomStart = 28.dp,
                topEnd = addPaymentInnerCorner,
                bottomEnd = addPaymentInnerCorner
            )
            val rightButtonShape = RoundedCornerShape(
                topStart = addDueInnerCorner,
                bottomStart = addDueInnerCorner,
                topEnd = 28.dp,
                bottomEnd = 28.dp
            )

            Button(
                onClick = onSave,
                interactionSource = addPaymentInteraction,
                enabled = !uiState.isSavingPayment && (uiState.paymentInputAmount.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = leftButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (isAddPaymentLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Add Payment",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            OutlinedButton(
                onClick = onAddPreviousDue,
                interactionSource = addDueInteraction,
                enabled = !uiState.isSavingPayment && (uiState.paymentInputAmount.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = rightButtonShape
            ) {
                if (isAddDueLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Add Previous Due",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (uiState.paymentRecords.isNotEmpty()) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        val recentTransactions = uiState.paymentRecords.take(5)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            recentTransactions.forEachIndexed { index, record ->
                val isExpanded = expandedRecordId == record.id
                val toggleExpanded = {
                    expandedRecordId = if (isExpanded) null else record.id
                }
                val transactionCardInteraction = remember { MutableInteractionSource() }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !uiState.isSavingPayment,
                            indication = null,
                            interactionSource = transactionCardInteraction
                        ) { toggleExpanded() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = groupedTransactionCardShape(index = index, lastIndex = recentTransactions.lastIndex),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.recordedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (record.note.isNotBlank()) {
                                Text(
                                    text = record.note,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (record.type != com.prantiux.milktick.data.PaymentRecordType.PAYMENT) {
                                Text(
                                    text = record.type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val amountColor = if (record.amount < 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        val amountText = if (record.amount < 0) "-₹${String.format("%.2f", kotlin.math.abs(record.amount))}" else "₹${String.format("%.2f", record.amount)}"

                        Text(
                            text = amountText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = amountColor
                        )

                        val arrowRotation by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            animationSpec = tween(durationMillis = 220),
                            label = "transactionArrowRotation"
                        )
                        IconButton(
                            onClick = toggleExpanded,
                            enabled = !uiState.isSavingPayment,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fa_chevron_down),
                                contentDescription = if (isExpanded) "Collapse transaction details" else "Expand transaction details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(arrowRotation)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = expandedRecordId == record.id,
                        enter = expandVertically(animationSpec = tween(240)) + fadeIn(animationSpec = tween(180)),
                        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(animationSpec = tween(160))
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDeleteRecord(record.id) },
                                enabled = !uiState.isSavingPayment,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun DayCell(
    day: Int,
    hasEntry: Boolean,
    hasNoDelivery: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .size(40.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
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

private fun groupedTransactionCardShape(index: Int, lastIndex: Int): RoundedCornerShape {
    return when {
        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        index == lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(8.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    
    var isEditing by remember(date) { mutableStateOf(false) }
    var editedQuantity by remember(date) { mutableStateOf(entryDetails?.quantity?.toString() ?: "") }
    var editedNote by remember(date) { mutableStateOf(entryDetails?.note ?: "") }
    var didBringMilk by remember(date) { mutableStateOf(hasEntry) }
    var isSaving by remember { mutableStateOf(false) }
    var showSavedState by remember { mutableStateOf(false) }
    val sheetDragOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val dismissDragThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }
    val dismissTravelPx = with(LocalDensity.current) { 220.dp.toPx() }

    LaunchedEffect(date) {
        isSaving = false
        showSavedState = false
        sheetDragOffset.snapTo(0f)
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, sheetDragOffset.value.roundToInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            sheetDragOffset.snapTo((sheetDragOffset.value + delta).coerceAtLeast(0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        scope.launch {
                            if (sheetDragOffset.value > dismissDragThresholdPx || velocity > 1200f) {
                                sheetDragOffset.animateTo(
                                    targetValue = sheetDragOffset.value + dismissTravelPx,
                                    animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
                                )
                                onDismiss()
                            } else {
                                sheetDragOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                    }
                )
                .navigationBarsPadding()
                .padding(vertical = 0.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing))
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BottomSheetDefaults.DragHandle()
                }

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
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                isEditing = false
                                editedQuantity = entryDetails?.quantity?.toString() ?: ""
                                editedNote = entryDetails?.note ?: ""
                                didBringMilk = hasEntry
                                isSaving = false
                                showSavedState = false
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        AnimatedContent(
                            targetState = isEditing,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith
                                    fadeOut(animationSpec = tween(140))
                            },
                            label = "dateSheetEditCancelIcon"
                        ) { editing ->
                            Icon(
                                painter = painterResource(
                                    if (editing) R.drawable.ic_fa_circle_xmark else R.drawable.ic_fa_pen_to_square
                                ),
                                contentDescription = if (editing) "Cancel edit" else "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

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
                        shape = RoundedCornerShape(24.dp)
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
                                shape = RoundedCornerShape(20.dp)
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
                                    shape = RoundedCornerShape(20.dp)
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
                            shape = RoundedCornerShape(24.dp)
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Did you bring milk toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(24.dp)
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                    shape = RoundedCornerShape(20.dp)
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
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    minLines = 1,
                                    maxLines = 6
                                )
                            }
                        }
                    }
                    }
                }

                if (isEditing) {
                    val saveInteractionSource = remember { MutableInteractionSource() }
                    val savePressed by saveInteractionSource.collectIsPressedAsState()
                    val saveScale by animateFloatAsState(
                        targetValue = if (savePressed) 0.97f else 1f,
                        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
                        label = "dateSaveButtonPress"
                    )

                    Button(
                        onClick = {
                            isSaving = true
                            showSavedState = false
                            calendarViewModel.updateMilkEntry(
                                date = date,
                                quantity = if (didBringMilk) editedQuantity.toFloatOrNull() ?: 0f else 0f,
                                brought = didBringMilk,
                                note = if (didBringMilk) editedNote else "",
                                onComplete = {
                                    isSaving = false
                                    showSavedState = true
                                }
                            )
                        },
                        enabled = !isSaving && (!didBringMilk || (editedQuantity.toFloatOrNull() ?: 0f) > 0),
                        interactionSource = saveInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(56.dp)
                            .align(Alignment.CenterHorizontally)
                            .graphicsLayer {
                                scaleX = saveScale
                                scaleY = saveScale
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showSavedState) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        AnimatedContent(
                            targetState = when {
                                isSaving -> "saving"
                                showSavedState -> "saved"
                                else -> "save"
                            },
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
                            label = "dateSaveButtonContent"
                        ) { state ->
                            Text(
                                text = when (state) {
                                    "saving" -> "Saving..."
                                    "saved" -> "Saved!"
                                    else -> "Save"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val entryCount = uiState.entryDetailsMap.size
    val estimatedSize = ((entryCount * 100) + 500) / 1024.0
    val fileSizeText = if (estimatedSize < 1) "${(estimatedSize * 1024).toInt()} bytes" else String.format("%.1f KB", estimatedSize)
    val documentsPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/MilkTick"

    var isExporting by remember { mutableStateOf(false) }
    var exportCompleted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

            Text(
                text = "Export data for $monthName $yearMonth",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text("File Name:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Size:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(fileSizeText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Save Location:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
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

            Button(
                onClick = {
                    if (exportCompleted) {
                        onDismiss()
                    } else {
                        isExporting = true
                        val message = exportMonthData(
                            yearMonth = yearMonth,
                            entryDetailsMap = uiState.entryDetailsMap,
                            amountPaid = uiState.amountPaid,
                            outstandingAmount = uiState.outstandingAmount,
                            latestPaymentNote = uiState.paymentRecords.firstOrNull { it.note.isNotBlank() }?.note ?: ""
                        )
                        isExporting = false
                        exportCompleted = true
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
                    containerColor = if (exportCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = if (isExporting) "exporting" else if (exportCompleted) "completed" else "ready",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
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

            if (!isExporting && !exportCompleted) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// Export month data to CSV
fun exportMonthData(
    yearMonth: YearMonth,
    entryDetailsMap: Map<LocalDate, EntryDetail>,
    amountPaid: Double,
    outstandingAmount: Double,
    latestPaymentNote: String
): String {
    return try {
        // Create MilkTick directory in Documents folder
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val milkTickDir = File(documentsDir, "MilkTick")
        if (!milkTickDir.exists()) {
            milkTickDir.mkdirs()
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
            writer.append("Total Paid,${String.format("%.2f", amountPaid)},,\n")
            writer.append("Outstanding,${String.format("%.2f", outstandingAmount)},,\n")
            if (latestPaymentNote.isNotEmpty()) {
                writer.append("Latest Payment Note,\"${latestPaymentNote.replace(",", ";").replace("\n", " ")}\",,\n")
            }
        }

        "Exported successfully to: ${csvFile.absolutePath}"
    } catch (e: Exception) {
        "Export failed: ${e.message}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

        ModalBottomSheet(
            onDismissRequest = { if (!isSaving) onDismiss() },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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

                if (!isSaving) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
