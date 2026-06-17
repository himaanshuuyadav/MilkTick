package com.prantiux.milktick.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import androidx.hilt.work.HiltWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.prantiux.milktick.repository.MainRepository
import java.time.LocalDate
import java.time.YearMonth

@HiltWorker
class EveningReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository
) : CoroutineWorker(appContext, workerParams) {
    
    private val auth = FirebaseAuth.getInstance()
    
    override suspend fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid
            
            if (userId != null) {
                // Check if milk entry exists for today
                val hasEntry = checkIfEntryExistsForToday(userId)
                
                // Only send notification if no entry exists
                if (!hasEntry) {
                    NotificationHelper.showEveningReminder(applicationContext)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun checkIfEntryExistsForToday(userId: String): Boolean {
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)
        val entries = repository.getMilkEntriesForMonthSync(userId, yearMonth)
        return entries.any { it.date == today }
    }
}
