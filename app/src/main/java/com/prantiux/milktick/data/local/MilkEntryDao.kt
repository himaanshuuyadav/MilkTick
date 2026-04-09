package com.prantiux.milktick.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkEntryDao {
    @Query("SELECT * FROM milk_entries WHERE userId = :userId AND yearMonth = :yearMonth AND isDeleted = 0 ORDER BY date DESC")
    fun getEntriesForMonth(userId: String, yearMonth: String): Flow<List<MilkEntryEntity>>

    @Query("SELECT * FROM milk_entries WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getEntryForDate(userId: String, date: String): MilkEntryEntity?

    @Query("SELECT * FROM milk_entries WHERE userId = :userId AND date = :date LIMIT 1")
    fun observeEntryForDate(userId: String, date: String): Flow<MilkEntryEntity?>

    @Query("SELECT * FROM milk_entries WHERE userId = :userId AND date LIKE :yearPrefix AND isDeleted = 0 ORDER BY date DESC")
    fun getEntriesForYear(userId: String, yearPrefix: String): Flow<List<MilkEntryEntity>>

    @Query("SELECT DISTINCT CAST(SUBSTR(date, 1, 4) AS INTEGER) FROM milk_entries WHERE userId = :userId AND isDeleted = 0 ORDER BY date DESC")
    suspend fun getAvailableYears(userId: String): List<Int>

    @Query("SELECT COUNT(*) FROM milk_entries WHERE userId = :userId")
    suspend fun getEntryCount(userId: String): Int

    @Query("SELECT syncState FROM milk_entries WHERE userId = :userId AND date = :date LIMIT 1")
    fun observeEntrySyncState(userId: String, date: String): Flow<SyncState?>

    @Query("SELECT COUNT(*) FROM milk_entries WHERE userId = :userId AND syncState = 'FAILED'")
    fun observeFailedCountForUser(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM milk_entries WHERE userId = :userId AND syncState IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE')")
    fun observePendingCountForUser(userId: String): Flow<Int>

    @Query(
        """
        SELECT CASE
            WHEN SUM(CASE WHEN syncState = 'FAILED' THEN 1 ELSE 0 END) > 0 THEN 'FAILED'
            WHEN SUM(CASE WHEN syncState IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE') THEN 1 ELSE 0 END) > 0 THEN 'PENDING_UPDATE'
            ELSE 'SYNCED'
        END
        FROM milk_entries
        WHERE userId = :userId AND yearMonth = :yearMonth AND isDeleted = 0
        """
    )
    fun observeMonthSyncState(userId: String, yearMonth: String): Flow<SyncState?>

    @Query(
        """
        SELECT SUM(quantity)
        FROM milk_entries
        WHERE userId = :userId AND yearMonth = :yearMonth AND isDeleted = 0
        """
    )
    fun getMonthlyTotalQuantity(userId: String, yearMonth: String): Flow<Float?>

    @Query(
        """
        SELECT COUNT(*)
        FROM milk_entries
        WHERE userId = :userId AND yearMonth = :yearMonth AND brought = 1 AND isDeleted = 0
        """
    )
    fun getTotalDeliveryDays(userId: String, yearMonth: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: MilkEntryEntity)

    @Query("SELECT * FROM milk_entries WHERE syncState != 'SYNCED'")
    suspend fun getPendingSyncEntries(): List<MilkEntryEntity>

    @Query("UPDATE milk_entries SET syncState = :state, remoteUpdatedAt = :remoteUpdatedAt WHERE userId = :userId AND date = :date")
    suspend fun markAsSynced(userId: String, date: String, state: SyncState = SyncState.SYNCED, remoteUpdatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE milk_entries SET syncState = :state WHERE userId = :userId AND date = :date")
    suspend fun markAsFailed(userId: String, date: String, state: SyncState = SyncState.FAILED)

    @Query("DELETE FROM milk_entries WHERE userId = :userId AND date = :date")
    suspend fun hardDelete(userId: String, date: String)
}
