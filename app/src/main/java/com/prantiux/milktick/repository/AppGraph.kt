package com.prantiux.milktick.repository

import android.content.Context
import com.prantiux.milktick.data.local.AppDatabase

object AppGraph {
    @Volatile
    private var initialized = false

    lateinit var database: AppDatabase
        private set

    lateinit var localRepository: LocalRepository
        private set

    lateinit var remoteSyncService: RemoteSyncService
        private set

    lateinit var mainRepository: MainRepository
        private set

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            database = AppDatabase.getInstance(context)
            localRepository = LocalRepository(database)
            remoteSyncService = RemoteSyncService(localRepository)
            mainRepository = MainRepository(
                context = context.applicationContext,
                localRepository = localRepository,
                remoteSyncService = remoteSyncService
            )
            initialized = true
        }
    }
}
