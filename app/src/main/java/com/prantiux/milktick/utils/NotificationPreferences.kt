package com.prantiux.milktick.utils

import android.content.Context
import android.content.SharedPreferences

class NotificationPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_EVENING_REMINDER_ENABLED = "evening_reminder_enabled"
        private const val KEY_MONTHLY_RATE_REMINDER_ENABLED = "monthly_rate_reminder_enabled"
        
        private const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        private const val KEY_EVENING_REMINDER_HOUR = "evening_reminder_hour"
        private const val KEY_EVENING_REMINDER_MINUTE = "evening_reminder_minute"
        private const val KEY_MONTHLY_RATE_HOUR = "monthly_rate_hour"
        private const val KEY_MONTHLY_RATE_MINUTE = "monthly_rate_minute"
        
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SNOOZE_DURATION = "snooze_duration"
        
        // Default values
        const val DEFAULT_DAILY_HOUR = 13 // 1:30 PM
        const val DEFAULT_DAILY_MINUTE = 30
        const val DEFAULT_EVENING_HOUR = 20 // 8:00 PM
        const val DEFAULT_EVENING_MINUTE = 0
        const val DEFAULT_MONTHLY_HOUR = 11 // 11:00 AM
        const val DEFAULT_MONTHLY_MINUTE = 0
        const val DEFAULT_SNOOZE_DURATION = 10 // 10 minutes
    }
    
    // Notification enabled/disabled
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    var dailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, value).apply()
    
    var eveningReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_EVENING_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_EVENING_REMINDER_ENABLED, value).apply()
    
    var monthlyRateReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONTHLY_RATE_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MONTHLY_RATE_REMINDER_ENABLED, value).apply()
    
    // Notification times
    var dailyReminderHour: Int
        get() = prefs.getInt(KEY_DAILY_REMINDER_HOUR, DEFAULT_DAILY_HOUR)
        set(value) = prefs.edit().putInt(KEY_DAILY_REMINDER_HOUR, value).apply()
    
    var dailyReminderMinute: Int
        get() = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, DEFAULT_DAILY_MINUTE)
        set(value) = prefs.edit().putInt(KEY_DAILY_REMINDER_MINUTE, value).apply()
    
    var eveningReminderHour: Int
        get() = prefs.getInt(KEY_EVENING_REMINDER_HOUR, DEFAULT_EVENING_HOUR)
        set(value) = prefs.edit().putInt(KEY_EVENING_REMINDER_HOUR, value).apply()
    
    var eveningReminderMinute: Int
        get() = prefs.getInt(KEY_EVENING_REMINDER_MINUTE, DEFAULT_EVENING_MINUTE)
        set(value) = prefs.edit().putInt(KEY_EVENING_REMINDER_MINUTE, value).apply()
    
    var monthlyRateHour: Int
        get() = prefs.getInt(KEY_MONTHLY_RATE_HOUR, DEFAULT_MONTHLY_HOUR)
        set(value) = prefs.edit().putInt(KEY_MONTHLY_RATE_HOUR, value).apply()
    
    var monthlyRateMinute: Int
        get() = prefs.getInt(KEY_MONTHLY_RATE_MINUTE, DEFAULT_MONTHLY_MINUTE)
        set(value) = prefs.edit().putInt(KEY_MONTHLY_RATE_MINUTE, value).apply()
    
    // Sound and vibration
    var notificationSound: String
        get() = prefs.getString(KEY_NOTIFICATION_SOUND, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_NOTIFICATION_SOUND, value).apply()
    
    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
    
    // Snooze duration in minutes
    var snoozeDuration: Int
        get() = prefs.getInt(KEY_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION)
        set(value) = prefs.edit().putInt(KEY_SNOOZE_DURATION, value).apply()
}
