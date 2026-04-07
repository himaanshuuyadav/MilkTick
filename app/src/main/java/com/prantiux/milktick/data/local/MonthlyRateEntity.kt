package com.prantiux.milktick.data.local

import androidx.room.Entity

@Entity(
    tableName = "monthly_rates",
    primaryKeys = ["userId", "yearMonth"]
)
data class MonthlyRateEntity(
    val userId: String,
    val yearMonth: String,
    val ratePerLiter: Float,
    val defaultQuantity: Float,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long?,
    val syncState: SyncState
)
