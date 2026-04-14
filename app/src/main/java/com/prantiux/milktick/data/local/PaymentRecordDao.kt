package com.prantiux.milktick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaymentRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PaymentRecordEntity)

    @Query("SELECT * FROM payment_records WHERE userId = :userId AND appliedYearMonth = :yearMonth ORDER BY recordedAt DESC")
    suspend fun getRecordsForMonth(userId: String, yearMonth: String): List<PaymentRecordEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payment_records WHERE userId = :userId AND appliedYearMonth <= :endYearMonth")
    suspend fun getTotalUntil(userId: String, endYearMonth: String): Double

    @Query("SELECT * FROM payment_records WHERE userId = :userId ORDER BY recordedAt ASC")
    suspend fun getAllRecordsForUser(userId: String): List<PaymentRecordEntity>

    @Query("SELECT * FROM payment_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PaymentRecordEntity?

    @Query("SELECT * FROM payment_records WHERE syncState != 'SYNCED'")
    suspend fun getPendingSyncRecords(): List<PaymentRecordEntity>

    @Query("SELECT COUNT(*) FROM payment_records WHERE userId = :userId AND syncState = 'FAILED'")
    fun observeFailedCountForUser(userId: String): kotlinx.coroutines.flow.Flow<Int>

    @Query("SELECT COUNT(*) FROM payment_records WHERE userId = :userId AND syncState IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    fun observePendingCountForUser(userId: String): kotlinx.coroutines.flow.Flow<Int>

    @Query("UPDATE payment_records SET syncState = :state, remoteUpdatedAt = :remoteUpdatedAt WHERE id = :id")
    suspend fun markAsSynced(id: String, state: SyncState = SyncState.SYNCED, remoteUpdatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE payment_records SET syncState = :state WHERE id = :id")
    suspend fun markAsFailed(id: String, state: SyncState = SyncState.FAILED)

    @Query("UPDATE payment_records SET syncState = :state, localUpdatedAt = :localUpdatedAt WHERE id = :id")
    suspend fun updateSyncState(id: String, state: SyncState, localUpdatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM payment_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM payment_records WHERE userId = :userId")
    suspend fun deleteByUserIdHard(userId: String)
}
