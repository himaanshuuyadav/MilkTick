package com.prantiux.milktick.repository

import android.util.Log
import com.prantiux.milktick.data.MilkEntry
import com.prantiux.milktick.data.MonthlyRate
import com.prantiux.milktick.data.PaymentRecord
import com.prantiux.milktick.data.PaymentRecordType
import com.prantiux.milktick.data.local.SyncState
import java.time.LocalDateTime
import java.time.ZoneOffset
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
            if (entity.syncState == SyncState.PENDING_DELETE) {
                localRepository.markPaymentPendingDelete(entity.id)
            } else {
                localRepository.markPaymentFailed(entity.id)
            }
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
                if (entity.syncState == SyncState.PENDING_DELETE) {
                    val result = firestoreRepository.deletePaymentRecord(entity.userId, entity.id)
                    if (result.isSuccess) {
                        localRepository.hardDeletePaymentRecord(entity.id)
                    } else {
                        // Keep delete intent so next sync retries the same operation.
                        localRepository.markPaymentPendingDelete(entity.id)
                    }
                    return@forEach
                }

                val payment = PaymentRecord(
                    id = entity.id,
                    userId = entity.userId,
                    amount = entity.amount,
                    note = entity.note,
                    recordedAt = LocalDateTime.ofEpochSecond(entity.recordedAt / 1000, 0, ZoneOffset.UTC),
                    appliedYearMonth = YearMonth.parse(entity.appliedYearMonth),
                    type = PaymentRecordType.valueOf(entity.type)
                )
                val result = firestoreRepository.savePaymentRecord(payment)
                if (result.isSuccess) {
                    localRepository.markPaymentSynced(entity.id)
                } else {
                    localRepository.markPaymentFailed(entity.id)
                }
            } catch (e: Exception) {
                if (entity.syncState == SyncState.PENDING_DELETE) {
                    localRepository.markPaymentPendingDelete(entity.id)
                } else {
                    localRepository.markPaymentFailed(entity.id)
                }
                Log.e(tag, "pushPendingPayments failed for ${entity.userId}/${entity.id}", e)
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
        }
        reconcilePaymentRecords(userId)
        
        localRepository.updateLastSync("rates:$userId", if (lastSyncedAt > 0) System.currentTimeMillis() else System.currentTimeMillis())
    }

    suspend fun pullAllRates(userId: String) {
        firestoreRepository.getAllMonthlyRatesSync(userId).forEach { localRepository.saveMonthlyRate(it, SyncState.SYNCED) }
        reconcilePaymentRecords(userId)

        localRepository.updateLastSync("rates:$userId", System.currentTimeMillis())
    }

    private suspend fun reconcilePaymentRecords(userId: String) {
        try {
            val remotePayments = firestoreRepository.getAllPaymentRecordsSync(userId)
            val localPaymentEntities = localRepository.getAllPaymentRecordEntitiesForUser(userId)

            val localPendingDeleteIds = localPaymentEntities
                .filter { it.syncState == SyncState.PENDING_DELETE }
                .map { it.id }
                .toSet()

            remotePayments
                .filter { it.id !in localPendingDeleteIds }
                .forEach { localRepository.savePaymentRecord(it, SyncState.SYNCED) }

            val remoteIds = remotePayments.map { it.id }.toSet()
            localPaymentEntities.forEach { localRecord ->
                if (localRecord.syncState != SyncState.SYNCED) return@forEach
                if (localRecord.id !in remoteIds) {
                    Log.d(tag, "Deleting orphaned local payment record: ${localRecord.id}")
                    localRepository.hardDeletePaymentRecord(localRecord.id)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Payment reconciliation failed for user $userId", e)
        }
    }

    suspend fun pullAllUserData(userId: String) {
        pullAllEntries(userId)
        pullAllRates(userId)
    }
}
