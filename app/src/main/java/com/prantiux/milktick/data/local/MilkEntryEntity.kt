package com.prantiux.milktick.data.local

import androidx.room.Entity

@Entity(
    tableName = "milk_entries",
    primaryKeys = ["userId", "date"]
)
data class MilkEntryEntity(
    val userId: String,
    val date: String,
    val quantity: Float,
    val brought: Boolean,
    val note: String?,
    val yearMonth: String,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long?,
    val isDeleted: Boolean,
    val syncState: SyncState
)
