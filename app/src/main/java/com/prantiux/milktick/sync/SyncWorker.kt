package com.prantiux.milktick.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
            if (!isNetworkConnected(applicationContext)) {
                repo.markAllPendingAsFailed()
                return Result.success()
            }
            repo.runBackgroundSync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
