package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Int,
    val effectiveHydrationMl: Int = amountMl,
    val beverageType: String = BeverageType.WATER.name,
    val note: String = "",
    val syncedToCloud: Boolean = false,
    val dateKey: String = getCurrentDateKey(timestamp)
) {
    companion object {
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        private val hourFormatter = SimpleDateFormat("HH", Locale.getDefault())

        fun getCurrentDateKey(ts: Long = System.currentTimeMillis()): String {
            return dateFormatter.format(Date(ts))
        }

        fun formatTime(ts: Long): String {
            return timeFormatter.format(Date(ts))
        }

        fun getHourOfDay(ts: Long): Int {
            return hourFormatter.format(Date(ts)).toIntOrNull() ?: 0
        }
    }
}
