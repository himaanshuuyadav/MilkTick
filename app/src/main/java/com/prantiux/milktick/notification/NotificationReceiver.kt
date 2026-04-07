package com.prantiux.milktick.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class NotificationReceiver : BroadcastReceiver() {
    
    private lateinit var repository: MainRepository
    private val auth = FirebaseAuth.getInstance()
    
    override fun onReceive(context: Context, intent: Intent) {
        AppGraph.initialize(context.applicationContext)
        repository = AppGraph.mainRepository
        when (intent.action) {
            NotificationHelper.ACTION_MILK_YES -> {
                handleMilkYes(context)
            }
            NotificationHelper.ACTION_MILK_NO -> {
                handleMilkNo(context)
            }
        }
    }
    
    private fun handleMilkYes(context: Context) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val today = LocalDate.now()
                    val yearMonth = YearMonth.from(today)
                    
                    // Get the default quantity from monthly rate
                    val rate = repository.getMonthlyRate(userId, yearMonth)
                    
                    // Check if default quantity exists and is valid
                    if (rate == null || rate.defaultQuantity <= 0) {
                        // No default values found - open app to home page for manual entry
                        Log.d("NotificationReceiver", "No default values found, opening app")
                        
                        val openAppIntent = Intent(context, Class.forName("com.prantiux.milktick.MainActivity"))
                        openAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        openAppIntent.putExtra("navigate_to", "home")
                        openAppIntent.putExtra("show_entry_dialog", true)
                        context.startActivity(openAppIntent)
                        
                        // Cancel the notification
                        NotificationHelper.cancelNotification(context, NotificationHelper.NOTIFICATION_ID_DAILY_ENTRY)
                        return@launch
                    }
                    
                    val defaultQuantity = rate.defaultQuantity
                    
                    // Create milk entry with default quantity
                    val entry = MilkEntry(
                        date = today,
                        quantity = defaultQuantity,
                        brought = true,
                        note = "Auto-entered via notification",
                        userId = userId
                    )
                    
                    repository.saveMilkEntry(entry)
                    Log.d("NotificationReceiver", "Milk entry saved: $defaultQuantity L")
                    
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error saving milk entry", e)
                }
            }
        }
        
        // Cancel the notification
        NotificationHelper.cancelNotification(context, NotificationHelper.NOTIFICATION_ID_DAILY_ENTRY)
    }
    
    private fun handleMilkNo(context: Context) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val today = LocalDate.now()
                    val yearMonth = YearMonth.from(today)
                    
                    // Check if entry exists for today
                    val entries = repository.getMilkEntriesForMonthSync(userId, yearMonth)
                    val todayEntry = entries.firstOrNull { it.date == today }
                    
                    // If entry exists, delete it
                    if (todayEntry != null) {
                        repository.deleteMilkEntry(userId, today)
                        Log.d("NotificationReceiver", "Deleted entry for $today")
                    }
                    
                } catch (e: Exception) {
                    Log.e("NotificationReceiver", "Error handling No action", e)
                }
            }
        }
        
        // Cancel the notification
        NotificationHelper.cancelNotification(context, NotificationHelper.NOTIFICATION_ID_DAILY_ENTRY)
    }
}
