package com.prantiux.milktick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MonthlyPaymentDao {
    @Query("SELECT * FROM monthly_payments WHERE userId = :userId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getPayment(userId: String, yearMonth: String): MonthlyPaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(payment: MonthlyPaymentEntity)

    @Query("SELECT * FROM monthly_payments WHERE syncState != 'SYNCED'")
    suspend fun getPendingSyncPayments(): List<MonthlyPaymentEntity>

    @Query("UPDATE monthly_payments SET syncState = :state, remoteUpdatedAt = :remoteUpdatedAt WHERE userId = :userId AND yearMonth = :yearMonth")
    suspend fun markAsSynced(userId: String, yearMonth: String, state: SyncState = SyncState.SYNCED, remoteUpdatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE monthly_payments SET syncState = :state WHERE userId = :userId AND yearMonth = :yearMonth")
    suspend fun markAsFailed(userId: String, yearMonth: String, state: SyncState = SyncState.FAILED)
}
