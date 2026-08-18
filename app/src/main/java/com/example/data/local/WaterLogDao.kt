package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    fun getLogsForDate(dateKey: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestamp ASC")
    suspend fun getLogsForDateDirect(dateKey: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :startTs AND timestamp <= :endTs ORDER BY timestamp ASC")
    fun getLogsInRange(startTs: Long, endTs: Long): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :startTs AND timestamp <= :endTs ORDER BY timestamp ASC")
    suspend fun getLogsInRangeDirect(startTs: Long, endTs: Long): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE syncedToCloud = 0")
    suspend fun getUnsyncedLogs(): List<WaterLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<WaterLogEntity>)

    @Update
    suspend fun updateLog(log: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM water_logs")
    suspend fun clearAll()
}
