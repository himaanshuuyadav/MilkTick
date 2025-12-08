package com.prantiux.milktick.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.repository.FirestoreRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth

class EveningReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()
    
    override fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid
            
            if (userId != null) {
                // Check if milk entry exists for today
                val hasEntry = runBlocking {
                    checkIfEntryExistsForToday(userId)
                }
                
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
