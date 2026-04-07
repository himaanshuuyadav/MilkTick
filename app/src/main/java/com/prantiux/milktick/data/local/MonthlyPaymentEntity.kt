package com.prantiux.milktick.data.local

import androidx.room.Entity

@Entity(
    tableName = "monthly_payments",
    primaryKeys = ["userId", "yearMonth"]
)
data class MonthlyPaymentEntity(
    val userId: String,
    val yearMonth: String,
    val isPaid: Boolean,
    val paymentNote: String,
    val paidDate: Long?,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long?,
    val syncState: SyncState
)
