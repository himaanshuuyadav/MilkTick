package com.prantiux.milktick.utils

import android.content.Context
import androidx.work.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {
    
    companion object {
        private const val DAILY_WORK_TAG = "daily_milk_notification"
        private const val REMINDER_WORK_TAG = "reminder_milk_notification"
        
        private val DAILY_NOTIFICATION_TIME = LocalTime.of(12, 1) // 12:01 PM
        private val REMINDER_NOTIFICATION_TIME = LocalTime.of(21, 0) // 9:00 PM
    }
    
    fun scheduleDailyNotifications(userId: String) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel existing work
        workManager.cancelAllWorkByTag(DAILY_WORK_TAG)
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
        
        // Schedule daily notification for 12:01 PM
        val dailyConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(dailyConstraints)
            .setInputData(workDataOf("userId" to userId))
            .addTag(DAILY_WORK_TAG)
            .setInitialDelay(calculateInitialDelay(DAILY_NOTIFICATION_TIME), TimeUnit.MILLISECONDS)
            .build()
        
        workManager.enqueue(dailyWorkRequest)
        
        // Schedule reminder notification for 9:00 PM
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(dailyConstraints)
            .setInputData(workDataOf("userId" to userId))
            .addTag(REMINDER_WORK_TAG)
            .setInitialDelay(calculateInitialDelay(REMINDER_NOTIFICATION_TIME), TimeUnit.MILLISECONDS)
            .build()
        
        workManager.enqueue(reminderWorkRequest)
    }
    
    fun cancelNotifications() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(DAILY_WORK_TAG)
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
    }
    
    private fun calculateInitialDelay(targetTime: LocalTime): Long {
        val now = LocalDateTime.now()
        val targetToday = LocalDateTime.of(LocalDate.now(), targetTime)
        
        val target = if (now.isAfter(targetToday)) {
            // If target time has passed today, schedule for tomorrow
            targetToday.plusDays(1)
        } else {
            targetToday
        }
        
        return java.time.Duration.between(now, target).toMillis()
    }
    
    // For testing purposes - schedule immediate notifications
    fun scheduleTestNotification(userId: String, isReminder: Boolean = false) {
        val workManager = WorkManager.getInstance(context)
        
        val workRequest = if (isReminder) {
            OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
                .setInputData(workDataOf("userId" to userId))
                .build()
        } else {
            OneTimeWorkRequestBuilder<DailyNotificationWorker>()
                .setInputData(workDataOf("userId" to userId))
                .build()
        }
        
        workManager.enqueue(workRequest)
    }
}

class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            val today = LocalDate.now()
            val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Check if user already logged milk for today
            val repository = com.prantiux.milktick.repository.FirestoreRepository()
            val todayEntry = repository.getMilkEntryForDate(userId, today)
            
            // Only send notification if no entry exists for today
            if (todayEntry == null) {
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showDailyMilkNotification(userId, dateString)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

class ReminderNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString("userId") ?: return Result.failure()
            val today = LocalDate.now()
            val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Check if user already logged milk for today
            val repository = com.prantiux.milktick.repository.FirestoreRepository()
            val todayEntry = repository.getMilkEntryForDate(userId, today)
            
            // Only send reminder if no entry exists for today
            if (todayEntry == null) {
                val notificationHelper = NotificationHelper(applicationContext)
                // Cancel any existing daily notification first
                notificationHelper.cancelDailyNotification()
                // Show reminder notification
                notificationHelper.showReminderNotification(userId, dateString)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
