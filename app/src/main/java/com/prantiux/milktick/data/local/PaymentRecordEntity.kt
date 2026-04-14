package com.prantiux.milktick.data.local

import androidx.room.Entity

@Entity(tableName = "payment_records", primaryKeys = ["id"])
data class PaymentRecordEntity(
    val id: String,
    val userId: String,
    val amount: Double,
    val note: String,
    val recordedAt: Long,
    val appliedYearMonth: String,
    val type: String,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long?,
    val syncState: SyncState
)
