package com.prantiux.milktick.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: MainRepository
    private val auth = FirebaseAuth.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Log.d("AlarmReceiver", "No user logged in, skipping notification")
                    return@launch
                }

                when (intent.action) {
                    NotificationScheduler.ACTION_DAILY_ALARM -> handleDailyMilkAlarm(context, userId)
                    NotificationScheduler.ACTION_EVENING_ALARM -> handleEveningAlarm(context, userId)
                    NotificationScheduler.ACTION_MONTHLY_ALARM -> handleMonthlyAlarm(context)
                }
                
                // Reschedule for next occurrence
                NotificationScheduler.scheduleAllNotifications(context)

            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleDailyMilkAlarm(context: Context, userId: String) {
        val today = LocalDate.now()
        val entry = repository.getMilkEntryForDate(userId, today)
        
        // Only show if there's absolutely no entry for today
        if (entry == null) {
            NotificationHelper.showDailyMilkReminder(context)
        }
    }

    private suspend fun handleEveningAlarm(context: Context, userId: String) {
        val today = LocalDate.now()
        val entry = repository.getMilkEntryForDate(userId, today)

        // Show evening reminder if NO entry exists, OR if an entry exists but brought is false
        if (entry == null || !entry.brought) {
            NotificationHelper.showEveningReminder(context)
        }
    }

    private fun handleMonthlyAlarm(context: Context) {
        // Just show the notification (for the 1st of the month)
        NotificationHelper.showMonthlyRateReminder(context)
    }
}
