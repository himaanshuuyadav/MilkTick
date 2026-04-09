package com.prantiux.milktick.repository

import android.util.Log
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyPayment
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.local.SyncState
import java.time.YearMonth

class RemoteSyncService(
    private val localRepository: LocalRepository,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    private val tag = "RemoteSyncService"

    suspend fun markAllPendingAsFailed() {
        localRepository.getPendingEntryEntities().forEach { entity ->
            localRepository.markEntryFailed(entity.userId, entity.date)
        }
        localRepository.getPendingRateEntities().forEach { entity ->
            localRepository.markRateFailed(entity.userId, entity.yearMonth)
        }
        localRepository.getPendingPaymentEntities().forEach { entity ->
            localRepository.markPaymentFailed(entity.userId, entity.yearMonth)
        }
    }

    suspend fun pushPendingEntries() {
        localRepository.getPendingEntryEntities().forEach { entity ->
            try {
                if (entity.syncState == SyncState.PENDING_DELETE || entity.isDeleted) {
                    val deleteResult = firestoreRepository.deleteMilkEntry(entity.userId, java.time.LocalDate.parse(entity.date))
                    if (deleteResult.isSuccess) {
                        localRepository.hardDeleteEntry(entity.userId, entity.date)
                    } else {
                        localRepository.markEntryFailed(entity.userId, entity.date)
                    }
                } else {
                    val entry = MilkEntry(
                        date = java.time.LocalDate.parse(entity.date),
                        quantity = entity.quantity,
                        brought = entity.brought,
                        note = entity.note,
                        userId = entity.userId
                    )
                    val result = firestoreRepository.saveMilkEntry(entry)
                    if (result.isSuccess) {
                        localRepository.markEntrySynced(entity.userId, entity.date)
                    } else {
                        localRepository.markEntryFailed(entity.userId, entity.date)
                    }
                }
            } catch (e: Exception) {
                localRepository.markEntryFailed(entity.userId, entity.date)
                Log.e(tag, "pushPendingEntries failed for ${entity.userId}/${entity.date}", e)
            }
        }
    }

    suspend fun pushPendingRates() {
        localRepository.getPendingRateEntities().forEach { entity ->
            try {
                val rate = MonthlyRate(
                    yearMonth = YearMonth.parse(entity.yearMonth),
                    ratePerLiter = entity.ratePerLiter,
                    defaultQuantity = entity.defaultQuantity,
                    userId = entity.userId
                )
                val result = firestoreRepository.saveMonthlyRate(rate)
                if (result.isSuccess) {
                    localRepository.markRateSynced(entity.userId, entity.yearMonth)
                } else {
                    localRepository.markRateFailed(entity.userId, entity.yearMonth)
                }
            } catch (e: Exception) {
                localRepository.markRateFailed(entity.userId, entity.yearMonth)
                Log.e(tag, "pushPendingRates failed for ${entity.userId}/${entity.yearMonth}", e)
            }
        }
    }

    suspend fun pushPendingPayments() {
        localRepository.getPendingPaymentEntities().forEach { entity ->
            try {
                val payment = MonthlyPayment(
                    yearMonth = YearMonth.parse(entity.yearMonth),
                    userId = entity.userId,
                    isPaid = entity.isPaid,
                    paymentNote = entity.paymentNote,
                    paidDate = entity.paidDate
                )
                val result = firestoreRepository.saveMonthlyPayment(payment)
                if (result.isSuccess) {
                    localRepository.markPaymentSynced(entity.userId, entity.yearMonth)
                } else {
                    localRepository.markPaymentFailed(entity.userId, entity.yearMonth)
                }
            } catch (e: Exception) {
                localRepository.markPaymentFailed(entity.userId, entity.yearMonth)
                Log.e(tag, "pushPendingPayments failed for ${entity.userId}/${entity.yearMonth}", e)
            }
        }
    }

    suspend fun pullLatestEntries(userId: String, lastSyncedAt: Long, monthsBack: Int = 3) {
        val now = YearMonth.now()
        repeat(monthsBack) { idx ->
            val month = now.minusMonths(idx.toLong())
            val entries = firestoreRepository.getMilkEntriesForMonthSync(userId, month)
            entries.forEach { localRepository.saveMilkEntry(it, SyncState.SYNCED) }
        }
        localRepository.updateLastSync("entries:$userId", if (lastSyncedAt > 0) System.currentTimeMillis() else System.currentTimeMillis())
    }

    suspend fun pullAllEntries(userId: String) {
        firestoreRepository.getAllMilkEntriesSync(userId).forEach { localRepository.saveMilkEntry(it, SyncState.SYNCED) }
        localRepository.updateLastSync("entries:$userId", System.currentTimeMillis())
    }

    suspend fun pullLatestRates(userId: String, lastSyncedAt: Long, monthsBack: Int = 3) {
        val now = YearMonth.now()
        repeat(monthsBack) { idx ->
            val month = now.minusMonths(idx.toLong())
            val rate = firestoreRepository.getMonthlyRate(userId, month)
            if (rate != null) {
                localRepository.saveMonthlyRate(rate, SyncState.SYNCED)
            }
            val payment = firestoreRepository.getMonthlyPayment(userId, month)
            if (payment != null) {
                localRepository.saveMonthlyPayment(payment, SyncState.SYNCED)
            }
        }
        localRepository.updateLastSync("rates:$userId", if (lastSyncedAt > 0) System.currentTimeMillis() else System.currentTimeMillis())
    }

    suspend fun pullAllRates(userId: String) {
        firestoreRepository.getAllMonthlyRatesSync(userId).forEach { localRepository.saveMonthlyRate(it, SyncState.SYNCED) }
        firestoreRepository.getAllMonthlyPaymentsSync(userId).forEach { localRepository.saveMonthlyPayment(it, SyncState.SYNCED) }
        localRepository.updateLastSync("rates:$userId", System.currentTimeMillis())
    }

    suspend fun pullAllUserData(userId: String) {
        pullAllEntries(userId)
        pullAllRates(userId)
    }
}
