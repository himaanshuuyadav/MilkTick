package com.prantiux.milktick.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    
    private const val MONTHLY_RATE_WORK = "monthly_rate_reminder"
    private const val DAILY_MILK_WORK = "daily_milk_reminder"
    private const val EVENING_REMINDER_WORK = "evening_reminder"
    
    fun scheduleAllNotifications(context: Context) {
        val prefs = com.prantiux.milktick.utils.NotificationPreferences(context)
        
        if (prefs.dailyReminderEnabled) {
            scheduleDailyMilkReminder(context, prefs.dailyReminderHour, prefs.dailyReminderMinute)
        }
        
        if (prefs.eveningReminderEnabled) {
            scheduleEveningReminder(context, prefs.eveningReminderHour, prefs.eveningReminderMinute)
        }
        
        if (prefs.monthlyRateReminderEnabled) {
            scheduleMonthlyRateReminder(context, prefs.monthlyRateHour, prefs.monthlyRateMinute)
        }
    }
    
    fun scheduleMonthlyRateReminder(context: Context, hour: Int = 11, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If it's already past the target time today, schedule for tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<MonthlyRateWorker>(
            1, TimeUnit.DAYS // Check daily
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MONTHLY_RATE_WORK,
            ExistingPeriodicWorkPolicy.REPLACE, // Changed to REPLACE
            workRequest
        )
    }
    
    fun scheduleDailyMilkReminder(context: Context, hour: Int = 13, minute: Int = 30) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If it's already past the target time today, schedule for tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<DailyMilkWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_MILK_WORK,
            ExistingPeriodicWorkPolicy.REPLACE, // Changed to REPLACE
            workRequest
        )
    }
    
    fun scheduleEveningReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If it's already past the target time today, schedule for tomorrow
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val workRequest = PeriodicWorkRequestBuilder<EveningReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENING_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.REPLACE, // Changed to REPLACE
            workRequest
        )
    }
    
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MONTHLY_RATE_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_MILK_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_REMINDER_WORK)
    }
}
