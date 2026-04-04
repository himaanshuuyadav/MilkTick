package com.prantiux.milktick.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.R
import com.prantiux.milktick.notification.NotificationScheduler
import com.prantiux.milktick.repository.FirestoreRepository
import com.prantiux.milktick.utils.NotificationPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = remember { NotificationPreferences(context) }
    val repository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }
    var dailyReminderEnabled by remember { mutableStateOf(prefs.dailyReminderEnabled) }
    var eveningReminderEnabled by remember { mutableStateOf(prefs.eveningReminderEnabled) }
    var monthlyRateReminderEnabled by remember { mutableStateOf(prefs.monthlyRateReminderEnabled) }
    
    var dailyReminderTime by remember { mutableStateOf(Pair(prefs.dailyReminderHour, prefs.dailyReminderMinute)) }
    var eveningReminderTime by remember { mutableStateOf(Pair(prefs.eveningReminderHour, prefs.eveningReminderMinute)) }
    var monthlyRateTime by remember { mutableStateOf(Pair(prefs.monthlyRateHour, prefs.monthlyRateMinute)) }
    
    // Time picker states
    val dailyTimePickerState = rememberTimePickerState(
        initialHour = dailyReminderTime.first,
        initialMinute = dailyReminderTime.second,
        is24Hour = false
    )
    val eveningTimePickerState = rememberTimePickerState(
        initialHour = eveningReminderTime.first,
        initialMinute = eveningReminderTime.second,
        is24Hour = false
    )
    val monthlyTimePickerState = rememberTimePickerState(
        initialHour = monthlyRateTime.first,
        initialMinute = monthlyRateTime.second,
        is24Hour = false
    )
    
    var showDailyTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }
    var showMonthlyTimePicker by remember { mutableStateOf(false) }
    
    fun saveNotificationTimesToFirebase(hour: Int, minute: Int, type: String) {
        currentUserId?.let { userId ->
            scope.launch {
                repository.saveNotificationSettings(userId, type, hour, minute)
            }
        }
    }
    
    fun rescheduleNotifications() {
        if (notificationsEnabled) {
            NotificationScheduler.scheduleAllNotifications(context)
        } else {
            NotificationScheduler.cancelAllNotifications(context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(R.drawable.ic_fa_arrow_left), contentDescription = "Back")
                    }
                },
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
        }
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(24.dp)
            ) {
                // Master toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_fa_bell),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Enable Notifications",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Master control for all notifications",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    prefs.notificationsEnabled = it
                                    rescheduleNotifications()
                                }
                            )
                        }
                    }
                }
                
                // Daily Reminder Section
                item {
                    Text(
                        text = "Reminder Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item {
                    NotificationTimeCard(
                        iconRes = R.drawable.ic_fa_utensils,
                        title = "Daily Milk Reminder",
                        subtitle = "Remind to log daily milk",
                        time = dailyReminderTime,
                        enabled = dailyReminderEnabled && notificationsEnabled,
                        onEnabledChange = {
                            dailyReminderEnabled = it
                            prefs.dailyReminderEnabled = it
                            rescheduleNotifications()
                        },
                        onTimeClick = {
                            showDailyTimePicker = true
                        }
                    )
                }
                
                item {
                    NotificationTimeCard(
                        iconRes = R.drawable.ic_fa_cloud_sun,
                        title = "Evening Reminder",
                        subtitle = "Evening check-in notification",
                        time = eveningReminderTime,
                        enabled = eveningReminderEnabled && notificationsEnabled,
                        onEnabledChange = {
                            eveningReminderEnabled = it
                            prefs.eveningReminderEnabled = it
                            rescheduleNotifications()
                        },
                        onTimeClick = {
                            showEveningTimePicker = true
                        }
                    )
                }
                
                item {
                    NotificationTimeCard(
                        iconRes = R.drawable.ic_fa_calendar_days,
                        title = "Monthly Rate Reminder",
                        subtitle = "Set rates on 1st of every month",
                        time = monthlyRateTime,
                        enabled = monthlyRateReminderEnabled && notificationsEnabled,
                        onEnabledChange = {
                            monthlyRateReminderEnabled = it
                            prefs.monthlyRateReminderEnabled = it
                            rescheduleNotifications()
                        },
                        onTimeClick = {
                            showMonthlyTimePicker = true
                        }
                    )
                }
            }
        }
        
        // Material 3 Time Pickers
        if (showDailyTimePicker) {
            TimePickerDialog(
                onDismiss = { showDailyTimePicker = false },
                onConfirm = {
                    dailyReminderTime = Pair(dailyTimePickerState.hour, dailyTimePickerState.minute)
                    prefs.dailyReminderHour = dailyTimePickerState.hour
                    prefs.dailyReminderMinute = dailyTimePickerState.minute
                    saveNotificationTimesToFirebase(dailyTimePickerState.hour, dailyTimePickerState.minute, "dailyReminder")
                    rescheduleNotifications()
                    showDailyTimePicker = false
                }
            ) {
                TimePicker(state = dailyTimePickerState)
            }
        }
        
        if (showEveningTimePicker) {
            TimePickerDialog(
                onDismiss = { showEveningTimePicker = false },
                onConfirm = {
                    eveningReminderTime = Pair(eveningTimePickerState.hour, eveningTimePickerState.minute)
                    prefs.eveningReminderHour = eveningTimePickerState.hour
                    prefs.eveningReminderMinute = eveningTimePickerState.minute
                    saveNotificationTimesToFirebase(eveningTimePickerState.hour, eveningTimePickerState.minute, "eveningReminder")
                    rescheduleNotifications()
                    showEveningTimePicker = false
                }
            ) {
                TimePicker(state = eveningTimePickerState)
            }
        }
        
        if (showMonthlyTimePicker) {
            TimePickerDialog(
                onDismiss = { showMonthlyTimePicker = false },
                onConfirm = {
                    monthlyRateTime = Pair(monthlyTimePickerState.hour, monthlyTimePickerState.minute)
                    prefs.monthlyRateHour = monthlyTimePickerState.hour
                    prefs.monthlyRateMinute = monthlyTimePickerState.minute
                    saveNotificationTimesToFirebase(monthlyTimePickerState.hour, monthlyTimePickerState.minute, "monthlyRate")
                    rescheduleNotifications()
                    showMonthlyTimePicker = false
                }
            ) {
                TimePicker(state = monthlyTimePickerState)
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        text = {
            content()
        }
    )
}

@Composable
fun NotificationTimeCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    time: Pair<Int, Int>,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
            
            if (enabled) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onTimeClick)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_fa_circle_info),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = String.format("%02d:%02d %s", 
                            if (time.first > 12) time.first - 12 else if (time.first == 0) 12 else time.first,
                            time.second,
                            if (time.first >= 12) "PM" else "AM"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
