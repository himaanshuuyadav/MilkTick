package com.prantiux.milktick.repository

import android.content.Context
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.PaymentRecord
import com.prantiux.milktick.data.PaymentRecordType
import com.prantiux.milktick.data.local.SyncState
import com.prantiux.milktick.sync.SyncWorkScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

class MainRepository(
    private val context: Context,
    private val localRepository: LocalRepository,
    private val remoteSyncService: RemoteSyncService,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    private companion object {
        const val INITIAL_SYNC_VERSION = 2
    }

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
        val syncVersion = appPrefs.getInt("initial_sync_version_$userId", 0)
        val isInitialSyncDone = appPrefs.getBoolean("initial_sync_done_$userId", false)
        val isRoomEmpty = runBlocking { localRepository.isRoomEmpty(userId) }
        return syncVersion < INITIAL_SYNC_VERSION || !isInitialSyncDone || isRoomEmpty
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

    fun observeMonthlyRate(userId: String, yearMonth: YearMonth): Flow<MonthlyRate?> {
        return localRepository.observeMonthlyRate(userId, yearMonth)
    }

    fun observeEntryForDate(userId: String, date: LocalDate): Flow<MilkEntry?> {
        return localRepository.observeEntryForDate(userId, date)
    }

    suspend fun saveMonthlyPayment(payment: MonthlyPayment): Result<Unit> {
        val monthlyRate = localRepository.getMonthlyRate(payment.userId, payment.yearMonth)
        val monthEntries = localRepository.getEntriesForMonthSync(payment.userId, payment.yearMonth)
        val totalCost = monthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() } * (monthlyRate?.ratePerLiter ?: 0f)
        val paidSoFar = localRepository.getPaymentRecordsForMonth(payment.userId, payment.yearMonth).sumOf { it.amount }

        val delta = if (payment.isPaid) totalCost - paidSoFar else 0.0
        if (delta > 0.0) {
            val record = PaymentRecord(
                id = UUID.randomUUID().toString(),
                userId = payment.userId,
                amount = delta,
                note = payment.paymentNote.ifBlank { "Marked as paid for ${payment.yearMonth}" },
                recordedAt = LocalDateTime.now(),
                appliedYearMonth = payment.yearMonth,
                type = PaymentRecordType.PAYMENT
            )
            localRepository.savePaymentRecord(record, SyncState.PENDING_CREATE)
            triggerSync()
        }
        return Result.success(Unit)
    }

    suspend fun getMonthlyPayment(userId: String, yearMonth: YearMonth): MonthlyPayment? {
        val monthlyRate = localRepository.getMonthlyRate(userId, yearMonth)
        val monthEntries = localRepository.getEntriesForMonthSync(userId, yearMonth)
        val totalCost = monthEntries.filter { it.brought }.sumOf { it.quantity.toDouble() } * (monthlyRate?.ratePerLiter ?: 0f)
        val records = localRepository.getPaymentRecordsForMonth(userId, yearMonth)
        val paidTotal = records.sumOf { it.amount }
        val latestNote = records.firstOrNull { it.note.isNotBlank() }?.note ?: ""

        return MonthlyPayment(
            yearMonth = yearMonth,
            userId = userId,
            isPaid = paidTotal >= totalCost && totalCost > 0.0,
            paymentNote = latestNote,
            paidDate = records.maxOfOrNull { it.recordedAt.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli() }
        )
    }

    suspend fun addPaymentRecord(userId: String, yearMonth: YearMonth, amount: Double, note: String): Result<Unit> {
        val record = PaymentRecord(
            id = UUID.randomUUID().toString(),
            userId = userId,
            amount = amount,
            note = note,
            recordedAt = LocalDateTime.now(),
            appliedYearMonth = yearMonth,
            type = PaymentRecordType.PAYMENT
        )
        localRepository.savePaymentRecord(record, SyncState.PENDING_CREATE)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun addPreviousDueAdjustment(userId: String, yearMonth: YearMonth, amount: Double, note: String): Result<Unit> {
        val record = PaymentRecord(
            id = UUID.randomUUID().toString(),
            userId = userId,
            // Negative value increases payable in running balance calculation.
            amount = -kotlin.math.abs(amount),
            note = note.ifBlank { "Previous due adjustment" },
            recordedAt = LocalDateTime.now(),
            appliedYearMonth = yearMonth,
            type = PaymentRecordType.ADJUSTMENT
        )
        localRepository.savePaymentRecord(record, SyncState.PENDING_CREATE)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun deletePaymentRecord(recordId: String): Result<Unit> {
        localRepository.deletePaymentRecord(recordId)
        triggerSync()
        return Result.success(Unit)
    }

    suspend fun getPaymentRecordsForMonth(userId: String, yearMonth: YearMonth): List<PaymentRecord> {
        return localRepository.getPaymentRecordsForMonth(userId, yearMonth)
    }

    suspend fun getTotalPaymentsUntil(userId: String, yearMonth: YearMonth): Double {
        return localRepository.getTotalPaymentsUntil(userId, yearMonth)
    }

    suspend fun getTotalCostUntil(userId: String, yearMonth: YearMonth): Double {
        return localRepository.getTotalCostUntil(userId, yearMonth)
    }

    suspend fun getAllEntryYearMonths(userId: String): List<YearMonth> {
        return localRepository.getAllEntryYearMonths(userId)
    }

    suspend fun getMonthlyCharge(userId: String, yearMonth: YearMonth): Double {
        return localRepository.getMonthlyCharge(userId, yearMonth)
    }

    suspend fun getAllPaymentRecordsForUser(userId: String): List<PaymentRecord> {
        return localRepository.getAllPaymentRecordsForUser(userId)
    }

    suspend fun saveNotificationSettings(userId: String, type: String, hour: Int, minute: Int): Result<Unit> {
        val result = firestoreRepository.saveNotificationSettings(userId, type, hour, minute)
        triggerSync()
        return result
    }

    suspend fun performInitialSyncIfNeeded(userId: String) {
        val key = "initial_sync_done_$userId"
        val versionKey = "initial_sync_version_$userId"
        val isInitialSyncDone = appPrefs.getBoolean(key, false)
        val syncVersion = appPrefs.getInt(versionKey, 0)
        val isRoomEmpty = localRepository.isRoomEmpty(userId)
        if (syncVersion < INITIAL_SYNC_VERSION || !isInitialSyncDone || isRoomEmpty) {
            remoteSyncService.pullAllUserData(userId)
            appPrefs.edit().putBoolean(key, true).apply()
            appPrefs.edit().putInt(versionKey, INITIAL_SYNC_VERSION).apply()
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

    fun observeGlobalSyncState(userId: String): Flow<SyncState> {
        return localRepository.observeGlobalSyncState(userId)
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

        remoteSyncService.pullLatestEntries(userId, entriesLastSync, monthsBack = 4)
        remoteSyncService.pullLatestRates(userId, ratesLastSync, monthsBack = 4)
    }

    suspend fun markAllPendingAsFailed() {
        remoteSyncService.markAllPendingAsFailed()
    }

    fun triggerSync() {
        SyncWorkScheduler.enqueueOneTimeSync(context)
    }

    fun schedulePeriodicSync() {
        SyncWorkScheduler.schedulePeriodicSync(context)
    }
}
