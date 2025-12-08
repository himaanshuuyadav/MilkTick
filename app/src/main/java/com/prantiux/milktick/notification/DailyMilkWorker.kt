package com.prantiux.milktick.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class DailyMilkWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        return try {
            NotificationHelper.showDailyMilkReminder(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
