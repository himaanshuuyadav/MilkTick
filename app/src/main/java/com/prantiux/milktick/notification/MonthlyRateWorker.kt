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
class MonthlyRateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MainRepository
) : CoroutineWorker(appContext, workerParams) {
    
    private val auth = FirebaseAuth.getInstance()
    
    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val userId = auth.currentUser?.uid
            
            if (userId != null) {
                val yearMonth = YearMonth.from(today)
                
                // Check if monthly rate is set for current month
                val rate = repository.getMonthlyRate(userId, yearMonth)
                val rateExists = rate != null && rate.ratePerLiter > 0
                
                // Send notification on 1st of month OR daily if rate not set
                if (today.dayOfMonth == 1 || !rateExists) {
                    NotificationHelper.showMonthlyRateReminder(applicationContext)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
