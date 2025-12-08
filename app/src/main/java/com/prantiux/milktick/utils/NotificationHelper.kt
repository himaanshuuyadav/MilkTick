package com.prantiux.milktick.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prantiux.milktick.MainActivity
import com.prantiux.milktick.R

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "milk_reminder_channel"
        const val DAILY_NOTIFICATION_ID = 1001
        const val REMINDER_NOTIFICATION_ID = 1002
        
        const val ACTION_YES = "com.prantiux.milktick.ACTION_YES"
        const val ACTION_NO = "com.prantiux.milktick.ACTION_NO"
        const val ACTION_REMINDER_YES = "com.prantiux.milktick.ACTION_REMINDER_YES"
        const val ACTION_REMINDER_NO = "com.prantiux.milktick.ACTION_REMINDER_NO"
        
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_USER_ID = "extra_user_id"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Milk Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily milk logging reminders"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showDailyMilkNotification(userId: String, date: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Yes Action Intent
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_YES
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_USER_ID, userId)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context, 0, yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // No Action Intent
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NO
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_USER_ID, userId)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context, 1, noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Milk Logging")
            .setContentText("Did you get milk delivered today?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,
                "Yes",
                yesPendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "No",
                noPendingIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Did you get milk delivered today? Tap Yes to log with default quantity or No to mark as not delivered.")
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(DAILY_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle notification permission denied
            e.printStackTrace()
        }
    }
    
    fun showReminderNotification(userId: String, date: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Reminder Yes Action Intent
        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REMINDER_YES
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_USER_ID, userId)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context, 2, yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Reminder No Action Intent
        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REMINDER_NO
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_USER_ID, userId)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context, 3, noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🔔 Reminder: Daily Milk Logging")
            .setContentText("Don't forget to log your milk delivery!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,
                "Yes",
                yesPendingIntent
            )
            .addAction(
                android.R.drawable.ic_delete,
                "No",
                noPendingIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Reminder: Did you get milk delivered today? This is a follow-up reminder to log your milk delivery.")
            )
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle notification permission denied
            e.printStackTrace()
        }
    }
    
    fun cancelDailyNotification() {
        NotificationManagerCompat.from(context).cancel(DAILY_NOTIFICATION_ID)
    }
    
    fun cancelReminderNotification() {
        NotificationManagerCompat.from(context).cancel(REMINDER_NOTIFICATION_ID)
    }
    
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
