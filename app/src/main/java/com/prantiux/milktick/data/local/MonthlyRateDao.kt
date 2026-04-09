package com.prantiux.milktick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyRateDao {
    @Query("SELECT * FROM monthly_rates WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getRate(userId: String, yearMonth: String): MonthlyRateEntity?

    @Query("SELECT * FROM monthly_rates WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    fun observeRate(userId: String, yearMonth: String): Flow<MonthlyRateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rate: MonthlyRateEntity)

    @Query("SELECT syncState FROM monthly_rates WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    fun observeRateSyncState(userId: String, yearMonth: String): Flow<SyncState?>

    @Query("SELECT COUNT(*) FROM monthly_rates WHERE userId = :userId AND syncState = 'FAILED'")
    fun observeFailedCountForUser(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM monthly_rates WHERE userId = :userId AND syncState IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    fun observePendingCountForUser(userId: String): Flow<Int>

    @Query("SELECT * FROM monthly_rates WHERE syncState != 'SYNCED'")
    suspend fun getPendingSyncRates(): List<MonthlyRateEntity>

    @Query("UPDATE monthly_rates SET syncState = :state, remoteUpdatedAt = :remoteUpdatedAt WHERE userId = :userId AND yearMonth = :yearMonth")
    suspend fun markAsSynced(userId: String, yearMonth: String, state: SyncState = SyncState.SYNCED, remoteUpdatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE monthly_rates SET syncState = :state WHERE userId = :userId AND yearMonth = :yearMonth")
    suspend fun markAsFailed(userId: String, yearMonth: String, state: SyncState = SyncState.FAILED)
}
