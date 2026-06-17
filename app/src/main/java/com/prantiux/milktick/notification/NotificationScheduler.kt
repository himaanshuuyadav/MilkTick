package com.prantiux.milktick.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object NotificationScheduler {
    
    private const val MONTHLY_RATE_REQUEST_CODE = 2001
    private const val DAILY_MILK_REQUEST_CODE = 2002
    private const val EVENING_REMINDER_REQUEST_CODE = 2003
    
    const val ACTION_MONTHLY_ALARM = "com.prantiux.milktick.ACTION_MONTHLY_ALARM"
    const val ACTION_DAILY_ALARM = "com.prantiux.milktick.ACTION_DAILY_ALARM"
    const val ACTION_EVENING_ALARM = "com.prantiux.milktick.ACTION_EVENING_ALARM"
    
    fun scheduleAllNotifications(context: Context) {
        val prefs = com.prantiux.milktick.utils.NotificationPreferences(context)
        
        if (prefs.dailyReminderEnabled) {
            scheduleDailyMilkReminder(context, prefs.dailyReminderHour, prefs.dailyReminderMinute)
        } else {
            cancelNotification(context, ACTION_DAILY_ALARM, DAILY_MILK_REQUEST_CODE)
        }
        
        if (prefs.eveningReminderEnabled) {
            scheduleEveningReminder(context, prefs.eveningReminderHour, prefs.eveningReminderMinute)
        } else {
            cancelNotification(context, ACTION_EVENING_ALARM, EVENING_REMINDER_REQUEST_CODE)
        }
        
        if (prefs.monthlyRateReminderEnabled) {
            scheduleMonthlyRateReminder(context, prefs.monthlyRateHour, prefs.monthlyRateMinute)
        } else {
            cancelNotification(context, ACTION_MONTHLY_ALARM, MONTHLY_RATE_REQUEST_CODE)
        }
    }
    
    fun scheduleMonthlyRateReminder(context: Context, hour: Int = 11, minute: Int = 0) {
        // We set it for the 1st of next month if it's already past or not the 1st
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (before(currentTime)) {
                add(Calendar.MONTH, 1)
            }
        }
        setExactAlarm(context, targetTime, ACTION_MONTHLY_ALARM, MONTHLY_RATE_REQUEST_CODE)
    }
    
    fun scheduleDailyMilkReminder(context: Context, hour: Int = 13, minute: Int = 30) {
        val targetTime = getTargetTime(hour, minute)
        setExactAlarm(context, targetTime, ACTION_DAILY_ALARM, DAILY_MILK_REQUEST_CODE)
    }
    
    fun scheduleEveningReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val targetTime = getTargetTime(hour, minute)
        setExactAlarm(context, targetTime, ACTION_EVENING_ALARM, EVENING_REMINDER_REQUEST_CODE)
    }
    
    private fun getTargetTime(hour: Int, minute: Int): Calendar {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        return targetTime
    }
    
    private fun setExactAlarm(context: Context, targetTime: Calendar, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Request exact alarms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetTime.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        // Fallback to inexact
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetTime.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTime.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationScheduler", "Scheduled $action for ${targetTime.time}")
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "Exact alarm permission revoked", e)
            // Fallback to inexact
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        }
    }
    
    private fun cancelNotification(context: Context, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("NotificationScheduler", "Cancelled $action")
    }
    
    fun cancelAllNotifications(context: Context) {
        cancelNotification(context, ACTION_MONTHLY_ALARM, MONTHLY_RATE_REQUEST_CODE)
        cancelNotification(context, ACTION_DAILY_ALARM, DAILY_MILK_REQUEST_CODE)
        cancelNotification(context, ACTION_EVENING_ALARM, EVENING_REMINDER_REQUEST_CODE)
    }
}
