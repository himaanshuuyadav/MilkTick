package com.prantiux.milktick.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prantiux.milktick.repository.AppGraph

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            AppGraph.initialize(applicationContext)
            val repo = AppGraph.mainRepository
            repo.runBackgroundSync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
