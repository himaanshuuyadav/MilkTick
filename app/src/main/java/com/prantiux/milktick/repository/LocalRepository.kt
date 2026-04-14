package com.prantiux.milktick.repository

import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.PaymentRecord
import com.prantiux.milktick.data.PaymentRecordType
import com.prantiux.milktick.data.local.AppDatabase
import com.prantiux.milktick.data.local.MilkEntryEntity
import com.prantiux.milktick.data.local.MonthlyPaymentEntity
import com.prantiux.milktick.data.local.MonthlyRateEntity
import com.prantiux.milktick.data.local.PaymentRecordEntity
import com.prantiux.milktick.data.local.SyncState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class LocalRepository(
    private val database: AppDatabase
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun getEntriesForMonth(userId: String, yearMonth: YearMonth): Flow<List<MilkEntry>> {
        val key = yearMonth.format(yearMonthFormatter)
        return database.milkEntryDao().getEntriesForMonth(userId, key).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    suspend fun getEntriesForMonthSync(userId: String, yearMonth: YearMonth): List<MilkEntry> {
        return getEntriesForMonth(userId, yearMonth).firstOrNull().orEmpty()
    }

    fun getEntriesForYear(userId: String, year: Int): Flow<List<MilkEntry>> {
        return database.milkEntryDao().getEntriesForYear(userId, "$year-%").map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    suspend fun getEntryForDate(userId: String, date: LocalDate): MilkEntry? {
        return database.milkEntryDao().getEntryForDate(userId, date.format(dateFormatter))?.toDomain()
    }

    fun observeEntryForDate(userId: String, date: LocalDate): Flow<MilkEntry?> {
        return database.milkEntryDao().observeEntryForDate(userId, date.format(dateFormatter)).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun getAvailableYears(userId: String): List<Int> {
        val years = database.milkEntryDao().getAvailableYears(userId)
        val currentYear = LocalDate.now().year
        return if (years.contains(currentYear)) years else listOf(currentYear) + years
    }

    suspend fun isRoomEmpty(userId: String): Boolean {
        return database.milkEntryDao().getEntryCount(userId) == 0
    }

    fun observeEntrySyncState(userId: String, date: LocalDate): Flow<SyncState?> {
        return database.milkEntryDao().observeEntrySyncState(userId, date.format(dateFormatter))
    }

    fun observeMonthSyncState(userId: String, yearMonth: YearMonth): Flow<SyncState?> {
        return database.milkEntryDao().observeMonthSyncState(userId, yearMonth.format(yearMonthFormatter))
    }

    fun observeRateSyncState(userId: String, yearMonth: YearMonth): Flow<SyncState?> {
        return database.monthlyRateDao().observeRateSyncState(userId, yearMonth.format(yearMonthFormatter))
    }

    fun observeGlobalSyncState(userId: String): Flow<SyncState> {
        val failedCountFlow = combine(
            database.milkEntryDao().observeFailedCountForUser(userId),
            database.monthlyRateDao().observeFailedCountForUser(userId),
            database.paymentRecordDao().observeFailedCountForUser(userId)
        ) { entryFailed, rateFailed, paymentFailed ->
            entryFailed + rateFailed + paymentFailed
        }

        val pendingCountFlow = combine(
            database.milkEntryDao().observePendingCountForUser(userId),
            database.monthlyRateDao().observePendingCountForUser(userId),
            database.paymentRecordDao().observePendingCountForUser(userId)
        ) { entryPending, ratePending, paymentPending ->
            entryPending + ratePending + paymentPending
        }

        return combine(failedCountFlow, pendingCountFlow) { failedCount, pendingCount ->
            when {
                failedCount > 0 -> SyncState.FAILED
                pendingCount > 0 -> SyncState.PENDING_UPDATE
                else -> SyncState.SYNCED
            }
        }
    }

    suspend fun getMonthlyTotalQuantity(userId: String, yearMonth: YearMonth): Float {
        return database.milkEntryDao().getMonthlyTotalQuantity(userId, yearMonth.format(yearMonthFormatter)).firstOrNull() ?: 0f
    }

    suspend fun getMonthlyDeliveryDays(userId: String, yearMonth: YearMonth): Int {
        return database.milkEntryDao().getTotalDeliveryDays(userId, yearMonth.format(yearMonthFormatter)).firstOrNull() ?: 0
    }

    suspend fun saveMilkEntry(entry: MilkEntry, state: SyncState) {
        val now = System.currentTimeMillis()
        database.milkEntryDao().insertOrUpdate(
            MilkEntryEntity(
                userId = entry.userId,
                date = entry.date.format(dateFormatter),
                quantity = entry.quantity,
                brought = entry.brought,
                note = entry.note,
                yearMonth = entry.date.format(yearMonthFormatter),
                localUpdatedAt = now,
                remoteUpdatedAt = null,
                isDeleted = false,
                syncState = state
            )
        )
    }

    suspend fun deleteMilkEntry(userId: String, date: LocalDate) {
        val now = System.currentTimeMillis()
        val dateKey = date.format(dateFormatter)
        val existing = database.milkEntryDao().getEntryForDate(userId, dateKey)
        val tombstone = MilkEntryEntity(
            userId = userId,
            date = dateKey,
            quantity = existing?.quantity ?: 0f,
            brought = false,
            note = existing?.note,
            yearMonth = date.format(yearMonthFormatter),
            localUpdatedAt = now,
            remoteUpdatedAt = existing?.remoteUpdatedAt,
            isDeleted = true,
            syncState = SyncState.PENDING_DELETE
        )
        database.milkEntryDao().insertOrUpdate(tombstone)
    }

    suspend fun saveMonthlyRate(rate: MonthlyRate, state: SyncState) {
        database.monthlyRateDao().insertOrUpdate(
            MonthlyRateEntity(
                userId = rate.userId,
                yearMonth = rate.yearMonth.format(yearMonthFormatter),
                ratePerLiter = rate.ratePerLiter,
                defaultQuantity = rate.defaultQuantity,
                localUpdatedAt = System.currentTimeMillis(),
                remoteUpdatedAt = null,
                syncState = state
            )
        )
    }

    suspend fun getMonthlyRate(userId: String, yearMonth: YearMonth): MonthlyRate? {
        return database.monthlyRateDao().getRate(userId, yearMonth.format(yearMonthFormatter))?.toDomain()
    }

    fun observeMonthlyRate(userId: String, yearMonth: YearMonth): Flow<MonthlyRate?> {
        return database.monthlyRateDao().observeRate(userId, yearMonth.format(yearMonthFormatter)).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun saveMonthlyPayment(payment: MonthlyPayment, state: SyncState) {
        database.monthlyPaymentDao().insertOrUpdate(
            MonthlyPaymentEntity(
                userId = payment.userId,
                yearMonth = payment.yearMonth.format(yearMonthFormatter),
                isPaid = payment.isPaid,
                paymentNote = payment.paymentNote,
                paidDate = payment.paidDate,
                localUpdatedAt = System.currentTimeMillis(),
                remoteUpdatedAt = null,
                syncState = state
            )
        )
    }

    suspend fun getMonthlyPayment(userId: String, yearMonth: YearMonth): MonthlyPayment? {
        return database.monthlyPaymentDao().getPayment(userId, yearMonth.format(yearMonthFormatter))?.toDomain()
    }

    suspend fun savePaymentRecord(record: PaymentRecord, state: SyncState) {
        database.paymentRecordDao().insertOrUpdate(
            PaymentRecordEntity(
                id = record.id,
                userId = record.userId,
                amount = record.amount,
                note = record.note,
                recordedAt = record.recordedAt.toInstant(ZoneOffset.UTC).toEpochMilli(),
                appliedYearMonth = record.appliedYearMonth.format(yearMonthFormatter),
                type = record.type.name,
                localUpdatedAt = System.currentTimeMillis(),
                remoteUpdatedAt = null,
                syncState = state
            )
        )
    }

    suspend fun getPaymentRecordsForMonth(userId: String, yearMonth: YearMonth): List<PaymentRecord> {
        return database.paymentRecordDao()
            .getRecordsForMonth(userId, yearMonth.format(yearMonthFormatter))
            .filter { it.syncState != SyncState.PENDING_DELETE }
            .map { it.toDomain() }
    }

    suspend fun getTotalPaymentsUntil(userId: String, yearMonth: YearMonth): Double {
        val endKey = yearMonth.format(yearMonthFormatter)
        return database.paymentRecordDao()
            .getAllRecordsForUser(userId)
            .filter { it.syncState != SyncState.PENDING_DELETE && it.appliedYearMonth <= endKey }
            .sumOf { it.amount }
    }

    suspend fun getTotalCostUntil(userId: String, yearMonth: YearMonth): Double {
        val endKey = yearMonth.format(yearMonthFormatter)
        val months = database.milkEntryDao().getYearMonthsUpTo(userId, endKey)
        var totalCost = 0.0

        months.forEach { monthKey ->
            val deliveredQuantity = database.milkEntryDao().getMonthlyDeliveredQuantitySync(userId, monthKey)
            val rate = database.monthlyRateDao().getRate(userId, monthKey)
            totalCost += deliveredQuantity.toDouble() * (rate?.ratePerLiter?.toDouble() ?: 0.0)
        }

        return totalCost
    }

    suspend fun getAllEntryYearMonths(userId: String): List<YearMonth> {
        return database.milkEntryDao().getAllYearMonths(userId).map { YearMonth.parse(it, yearMonthFormatter) }
    }

    suspend fun getMonthlyCharge(userId: String, yearMonth: YearMonth): Double {
        val monthKey = yearMonth.format(yearMonthFormatter)
        val deliveredQuantity = database.milkEntryDao().getMonthlyDeliveredQuantitySync(userId, monthKey)
        val rate = database.monthlyRateDao().getRate(userId, monthKey)
        return deliveredQuantity.toDouble() * (rate?.ratePerLiter?.toDouble() ?: 0.0)
    }

    suspend fun getAllPaymentRecordsForUser(userId: String): List<PaymentRecord> {
        return database.paymentRecordDao()
            .getAllRecordsForUser(userId)
            .filter { it.syncState != SyncState.PENDING_DELETE }
            .map { it.toDomain() }
    }

    suspend fun getAllPaymentRecordEntitiesForUser(userId: String): List<PaymentRecordEntity> {
        return database.paymentRecordDao().getAllRecordsForUser(userId)
    }

    suspend fun deletePaymentRecord(id: String) {
        val existing = database.paymentRecordDao().getById(id) ?: return
        if (existing.syncState == SyncState.PENDING_CREATE) {
            database.paymentRecordDao().deleteById(id)
            return
        }
        database.paymentRecordDao().updateSyncState(id, SyncState.PENDING_DELETE)
    }

    suspend fun getPendingEntryEntities(): List<MilkEntryEntity> = database.milkEntryDao().getPendingSyncEntries()

    suspend fun getPendingRateEntities(): List<MonthlyRateEntity> = database.monthlyRateDao().getPendingSyncRates()

    suspend fun getPendingPaymentEntities(): List<PaymentRecordEntity> = database.paymentRecordDao().getPendingSyncRecords()

    suspend fun markEntrySynced(userId: String, date: String) {
        database.milkEntryDao().markAsSynced(userId, date)
    }

    suspend fun markEntryFailed(userId: String, date: String) {
        database.milkEntryDao().markAsFailed(userId, date)
    }

    suspend fun hardDeleteEntry(userId: String, date: String) {
        database.milkEntryDao().hardDelete(userId, date)
    }

    suspend fun markRateSynced(userId: String, yearMonth: String) {
        database.monthlyRateDao().markAsSynced(userId, yearMonth)
    }

    suspend fun markRateFailed(userId: String, yearMonth: String) {
        database.monthlyRateDao().markAsFailed(userId, yearMonth)
    }

    suspend fun markPaymentSynced(id: String) {
        database.paymentRecordDao().markAsSynced(id)
    }

    suspend fun markPaymentFailed(id: String) {
        database.paymentRecordDao().markAsFailed(id)
    }

    suspend fun hardDeletePaymentRecord(id: String) {
        database.paymentRecordDao().deleteById(id)
    }

    suspend fun markPaymentPendingDelete(id: String) {
        database.paymentRecordDao().updateSyncState(id, SyncState.PENDING_DELETE)
    }

    suspend fun getLastSync(key: String): Long? = database.syncMetadataDao().getLastSync(key)

    suspend fun updateLastSync(key: String, timestamp: Long) {
        database.syncMetadataDao().updateLastSync(key, timestamp)
    }

    private fun MilkEntryEntity.toDomain(): MilkEntry {
        return MilkEntry(
            date = LocalDate.parse(date, dateFormatter),
            quantity = quantity,
            brought = brought,
            note = note,
            userId = userId
        )
    }

    private fun MonthlyRateEntity.toDomain(): MonthlyRate {
        return MonthlyRate(
            yearMonth = YearMonth.parse(yearMonth, yearMonthFormatter),
            ratePerLiter = ratePerLiter,
            defaultQuantity = defaultQuantity,
            userId = userId
        )
    }

    private fun MonthlyPaymentEntity.toDomain(): MonthlyPayment {
        return MonthlyPayment(
            yearMonth = YearMonth.parse(yearMonth, yearMonthFormatter),
            userId = userId,
            isPaid = isPaid,
            paymentNote = paymentNote,
            paidDate = paidDate
        )
    }

    private fun PaymentRecordEntity.toDomain(): PaymentRecord {
        return PaymentRecord(
            id = id,
            userId = userId,
            amount = amount,
            note = note,
            recordedAt = LocalDateTime.ofEpochSecond(recordedAt / 1000, 0, ZoneOffset.UTC),
            appliedYearMonth = YearMonth.parse(appliedYearMonth, yearMonthFormatter),
            type = PaymentRecordType.valueOf(type)
        )
    }
}
