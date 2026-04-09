package com.prantiux.milktick.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncWorkScheduler {
    private const val ONE_TIME_SYNC_NAME = "milktick_one_time_sync"
    private const val PERIODIC_SYNC_NAME = "milktick_periodic_sync"

    fun enqueueOneTimeSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_SYNC_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun schedulePeriodicSync(context: Context, repeatMinutes: Long = 30) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(repeatMinutes, TimeUnit.MINUTES)
            .setConstraints(networkConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_SYNC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun networkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
