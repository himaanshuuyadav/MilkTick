package com.prantiux.milktick.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MilkEntryEntity::class,
        MonthlyRateEntity::class,
        PaymentRecordEntity::class,
        MonthlyPaymentEntity::class,
        SyncMetadataEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun milkEntryDao(): MilkEntryDao
    abstract fun monthlyRateDao(): MonthlyRateDao
    abstract fun paymentRecordDao(): PaymentRecordDao
    abstract fun monthlyPaymentDao(): MonthlyPaymentDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milktick_offline.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
