package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.WaterLogEntity

@Database(entities = [WaterLogEntity::class], version = 1, exportSchema = false)
abstract class HydroDatabase : RoomDatabase() {

    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile
        private var INSTANCE: HydroDatabase? = null

        fun getDatabase(context: Context): HydroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydroDatabase::class.java,
                    "hydrotrack_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
