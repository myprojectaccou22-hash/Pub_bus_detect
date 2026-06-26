package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        BusEntity::class,
        RouteEntity::class,
        ComplaintEntity::class,
        NoticeEntity::class,
        MaintenanceLogEntity::class,
        LostFoundEntity::class,
        TripHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun busDao(): BusDao
    abstract fun routeDao(): RouteDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun noticeDao(): NoticeDao
    abstract fun maintenanceLogDao(): MaintenanceLogDao
    abstract fun lostFoundDao(): LostFoundDao
    abstract fun tripHistoryDao(): TripHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pundra_bus_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
