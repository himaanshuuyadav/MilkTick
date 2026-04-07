package com.prantiux.milktick.repository

import android.content.Context
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.sync.SyncWorkScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth

class MainRepository(
    private val context: Context,
    private val localRepository: LocalRepository,
    private val remoteSyncService: RemoteSyncService,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    private val appPrefs by lazy {
        context.getSharedPreferences("offline_sync_prefs", Context.MODE_PRIVATE)
    }

    fun getMilkEntriesForMonth(userId: String, yearMonth: YearMonth): Flow<List<MilkEntry>> {
        return localRepository.getEntriesForMonth(userId, yearMonth)
    }

    suspend fun getMilkEntriesForMonthSync(userId: String, yearMonth: YearMonth): List<MilkEntry> {
        return localRepository.getEntriesForMonthSync(userId, yearMonth)
    }

    fun getMilkEntriesForYear(userId: String, year: Int): Flow<List<MilkEntry>> {
        return localRepository.getEntriesForYear(userId, year)
    }

    suspend fun getMilkEntryForDate(userId: String, date: LocalDate): MilkEntry? {
        return localRepository.getEntryForDate(userId, date)
    }

    suspend fun getAvailableYears(userId: String): List<Int> {
        return localRepository.getAvailableYears(userId)
    }

    fun shouldRunInitialSync(userId: String): Boolean {
        val isInitialSyncDone = appPrefs.getBoolean("initial_sync_done_$userId", false)
        val isRoomEmpty = runBlocking { localRepository.isRoomEmpty(userId) }
        return !isInitialSyncDone || isRoomEmpty
    }

    suspend fun saveMilkEntry(entry: MilkEntry): Result<Unit> {
        val existing = localRepository.getEntryForDate(entry.userId, entry.date)
        val pendingState = if (existing == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
        localRepository.saveMilkEntry(entry, pendingState)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun deleteMilkEntry(userId: String, date: LocalDate): Result<Unit> {
        localRepository.deleteMilkEntry(userId, date)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun saveMonthlyRate(rate: MonthlyRate): Result<Unit> {
        val existing = localRepository.getMonthlyRate(rate.userId, rate.yearMonth)
        val pendingState = if (existing == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
        localRepository.saveMonthlyRate(rate, pendingState)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun getMonthlyRate(userId: String, yearMonth: YearMonth): MonthlyRate? {
        return localRepository.getMonthlyRate(userId, yearMonth)
    }

    suspend fun saveMonthlyPayment(payment: MonthlyPayment): Result<Unit> {
        val existing = localRepository.getMonthlyPayment(payment.userId, payment.yearMonth)
        val pendingState = if (existing == null) SyncState.PENDING_CREATE else SyncState.PENDING_UPDATE
        localRepository.saveMonthlyPayment(payment, pendingState)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun getMonthlyPayment(userId: String, yearMonth: YearMonth): MonthlyPayment? {
        return localRepository.getMonthlyPayment(userId, yearMonth)
            ?: MonthlyPayment(yearMonth = yearMonth, userId = userId, isPaid = false, paymentNote = "")
    }

    suspend fun saveNotificationSettings(userId: String, type: String, hour: Int, minute: Int): Result<Unit> {
        val result = firestoreRepository.saveNotificationSettings(userId, type, hour, minute)
        triggerSync()
        return result
    }

    suspend fun performInitialSyncIfNeeded(userId: String) {
        val key = "initial_sync_done_$userId"
        val isInitialSyncDone = appPrefs.getBoolean(key, false)
        val isRoomEmpty = localRepository.isRoomEmpty(userId)
        if (!isInitialSyncDone || isRoomEmpty) {
            remoteSyncService.pullLatestEntries(userId, 0L, monthsBack = 3)
            remoteSyncService.pullLatestRates(userId, 0L, monthsBack = 3)
            appPrefs.edit().putBoolean(key, true).apply()
            triggerSync()
        }
    }

    fun observeEntrySyncState(userId: String, date: LocalDate): Flow<SyncState?> {
        return localRepository.observeEntrySyncState(userId, date)
    }

    fun observeMonthSyncState(userId: String, yearMonth: YearMonth): Flow<SyncState?> {
        return localRepository.observeMonthSyncState(userId, yearMonth)
    }

    fun observeRateSyncState(userId: String, yearMonth: YearMonth): Flow<SyncState?> {
        return localRepository.observeRateSyncState(userId, yearMonth)
    }

    suspend fun getMonthlyTotalQuantity(userId: String, yearMonth: YearMonth): Float {
        return localRepository.getMonthlyTotalQuantity(userId, yearMonth)
    }

    suspend fun getMonthlyDeliveryDays(userId: String, yearMonth: YearMonth): Int {
        return localRepository.getMonthlyDeliveryDays(userId, yearMonth)
    }

    suspend fun runBackgroundSync() {
        remoteSyncService.pushPendingEntries()
        remoteSyncService.pushPendingRates()
        remoteSyncService.pushPendingPayments()

        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val entriesSyncKey = "entries:$userId"
        val ratesSyncKey = "rates:$userId"

        val entriesLastSync = localRepository.getLastSync(entriesSyncKey) ?: 0L
        val ratesLastSync = localRepository.getLastSync(ratesSyncKey) ?: 0L

        remoteSyncService.pullLatestEntries(userId, entriesLastSync, monthsBack = 3)
        remoteSyncService.pullLatestRates(userId, ratesLastSync, monthsBack = 3)
    }

    fun triggerSync() {
        SyncWorkScheduler.enqueueOneTimeSync(context)
    }

    fun schedulePeriodicSync() {
        SyncWorkScheduler.schedulePeriodicSync(context)
    }
}
