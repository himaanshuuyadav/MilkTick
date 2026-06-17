package com.prantiux.milktick.utils

import android.content.BroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.prantiux.milktick.repository.MainRepository
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prantiux.milktick.data.MilkEntry
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: MainRepository

    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val date = intent.getStringExtra(NotificationHelper.EXTRA_DATE) ?: return
        val userId = intent.getStringExtra(NotificationHelper.EXTRA_USER_ID) ?: return
        
        val notificationHelper = NotificationHelper(context)
        
        when (action) {
            NotificationHelper.ACTION_YES -> {
                // User clicked Yes on daily notification
                handleYesAction(context, date, userId, false)
                notificationHelper.cancelDailyNotification()
            }
            NotificationHelper.ACTION_NO -> {
                // User clicked No on daily notification
                handleNoAction(context, date, userId, false)
                notificationHelper.cancelDailyNotification()
            }
            NotificationHelper.ACTION_REMINDER_YES -> {
                // User clicked Yes on reminder notification
                handleYesAction(context, date, userId, true)
                notificationHelper.cancelReminderNotification()
            }
            NotificationHelper.ACTION_REMINDER_NO -> {
                // User clicked No on reminder notification
                handleNoAction(context, date, userId, true)
                notificationHelper.cancelReminderNotification()
            }
        }
    }
    
    private fun handleYesAction(context: Context, dateString: String, userId: String, isReminder: Boolean) {
        // Schedule work to add milk entry with default quantity
        androidx.work.OneTimeWorkRequestBuilder<AddMilkEntryWorker>()
            .setInputData(
                androidx.work.workDataOf(
                    "date" to dateString,
                    "userId" to userId,
                    "brought" to true,
                    "isReminder" to isReminder
                )
            )
            .build()
            .let { workRequest ->
                androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
            }
    }
    
    private fun handleNoAction(context: Context, dateString: String, userId: String, isReminder: Boolean) {
        // Schedule work to mark day as no milk delivered
        androidx.work.OneTimeWorkRequestBuilder<AddMilkEntryWorker>()
            .setInputData(
                androidx.work.workDataOf(
                    "date" to dateString,
                    "userId" to userId,
                    "brought" to false,
                    "isReminder" to isReminder
                )
            )
            .build()
            .let { workRequest ->
                androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
            }
    }
}

@androidx.hilt.work.HiltWorker
class AddMilkEntryWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted context: Context,
    @dagger.assisted.Assisted params: WorkerParameters,
    private val repository: MainRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val dateString = inputData.getString("date") ?: return Result.failure()
            val userId = inputData.getString("userId") ?: return Result.failure()
            val brought = inputData.getBoolean("brought", false)
            val isReminder = inputData.getBoolean("isReminder", false)
            
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Get default quantity for current month
            val yearMonth = YearMonth.of(date.year, date.monthValue)
            val monthlyRate = repository.getMonthlyRate(userId, yearMonth)
            val defaultQuantity = monthlyRate?.defaultQuantity ?: 1.0f
            
            // Create milk entry
            val milkEntry = MilkEntry(
                date = date,
                quantity = if (brought) defaultQuantity else 0f,
                brought = brought,
                note = if (isReminder) {
                    if (brought) "Added via reminder notification" else "No delivery - marked via reminder notification"
                } else {
                    if (brought) "Added via daily notification" else "No delivery - marked via daily notification"
                },
                userId = userId
            )
            
            repository.saveMilkEntry(milkEntry)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
