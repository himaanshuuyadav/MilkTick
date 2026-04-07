package com.prantiux.milktick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncMetadataDao {
    @Query("SELECT lastSyncedAt FROM sync_metadata WHERE `key` = :key LIMIT 1")
    suspend fun getLastSync(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLastSync(metadata: SyncMetadataEntity)

    suspend fun updateLastSync(key: String, timestamp: Long) {
        updateLastSync(SyncMetadataEntity(key = key, lastSyncedAt = timestamp))
    }
}
