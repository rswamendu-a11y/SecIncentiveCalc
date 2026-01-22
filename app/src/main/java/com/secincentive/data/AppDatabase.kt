package com.secincentive.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SmartphoneSlab::class,
        GlobalConfig::class,
        WearableRule::class,
        TabletRule::class,
        PcTier::class,
        CarePlusSlab::class,
        BundleRule::class,
        AccessoryBracket::class,
        AdditionalBooster::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun incentiveDao(): IncentiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sec_incentive_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
