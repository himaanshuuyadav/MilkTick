package com.prantiux.milktick.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.repository.MainRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth

class MonthlyRateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    private val repository: MainRepository by lazy {
        AppGraph.initialize(applicationContext)
        AppGraph.mainRepository
    }
    private val auth = FirebaseAuth.getInstance()
    
    override fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val userId = auth.currentUser?.uid
            
            if (userId != null) {
                val yearMonth = YearMonth.from(today)
                
                // Check if monthly rate is set for current month
                val rateExists = runBlocking {
                    val rate = repository.getMonthlyRate(userId, yearMonth)
                    rate != null && rate.ratePerLiter > 0
                }
                
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
