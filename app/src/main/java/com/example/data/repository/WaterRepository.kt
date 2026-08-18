package com.example.data.repository

import com.example.data.local.WaterLogDao
import com.example.data.model.BeverageType
import com.example.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaySummary(
    val dateKey: String,
    val dayLabel: String,     // e.g. "Mon", "Tue", "17 Aug"
    val totalAmountMl: Int,
    val effectiveHydrationMl: Int,
    val targetGoalMl: Int,
    val percentage: Int,
    val isToday: Boolean,
    val logsCount: Int
)

data class HydrationStats(
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val weeklyAverageMl: Int,
    val monthlyAverageMl: Int,
    val goalCompletionRatePercent: Int,
    val topBeverage: BeverageType,
    val totalDrinksLogged: Int
)

class WaterRepository(private val waterLogDao: WaterLogDao) {

    val allLogs: Flow<List<WaterLogEntity>> = waterLogDao.getAllLogs()

    fun getTodayLogsFlow(): Flow<List<WaterLogEntity>> {
        val todayKey = WaterLogEntity.getCurrentDateKey()
        return waterLogDao.getLogsForDate(todayKey)
    }

    suspend fun getTodayLogsDirect(): List<WaterLogEntity> {
        val todayKey = WaterLogEntity.getCurrentDateKey()
        return waterLogDao.getLogsForDateDirect(todayKey)
    }

    suspend fun addWaterLog(
        amountMl: Int,
        beverageType: BeverageType = BeverageType.WATER,
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val effective = beverageType.calculateEffectiveHydration(amountMl)
        val entity = WaterLogEntity(
            timestamp = timestamp,
            amountMl = amountMl,
            effectiveHydrationMl = effective,
            beverageType = beverageType.name,
            note = note,
            syncedToCloud = false,
            dateKey = WaterLogEntity.getCurrentDateKey(timestamp)
        )
        return waterLogDao.insertLog(entity)
    }

    suspend fun deleteLog(id: Long) {
        waterLogDao.deleteById(id)
    }

    /**
     * Gets past 7 days summary including today for the weekly chart
     */
    fun getPast7DaysFlow(goalMl: Int): Flow<List<DaySummary>> {
        val calendar = Calendar.getInstance()
        val endTs = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTs = calendar.timeInMillis

        val dayLabelFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = WaterLogEntity.getCurrentDateKey()

        return waterLogDao.getLogsInRange(startTs, endTs).map { logs ->
            val logsByDate = logs.groupBy { it.dateKey }
            val summaries = mutableListOf<DaySummary>()

            val iterCal = Calendar.getInstance()
            iterCal.timeInMillis = startTs
            for (i in 0..6) {
                val dKey = dateKeyFormat.format(iterCal.time)
                val dayLogs = logsByDate[dKey] ?: emptyList()
                val totalAmount = dayLogs.sumOf { it.amountMl }
                val effectiveHydration = dayLogs.sumOf { it.effectiveHydrationMl }
                val pct = if (goalMl > 0) ((effectiveHydration.toFloat() / goalMl) * 100).toInt() else 0

                summaries.add(
                    DaySummary(
                        dateKey = dKey,
                        dayLabel = dayLabelFormat.format(iterCal.time).take(1),
                        totalAmountMl = totalAmount,
                        effectiveHydrationMl = effectiveHydration,
                        targetGoalMl = goalMl,
                        percentage = pct.coerceIn(0, 200),
                        isToday = dKey == todayKey,
                        logsCount = dayLogs.size
                    )
                )
                iterCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            summaries
        }
    }

    /**
     * Gets past 30 days summary for monthly insights
     */
    fun getPast30DaysFlow(goalMl: Int): Flow<List<DaySummary>> {
        val calendar = Calendar.getInstance()
        val endTs = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTs = calendar.timeInMillis

        val dayLabelFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayKey = WaterLogEntity.getCurrentDateKey()

        return waterLogDao.getLogsInRange(startTs, endTs).map { logs ->
            val logsByDate = logs.groupBy { it.dateKey }
            val summaries = mutableListOf<DaySummary>()

            val iterCal = Calendar.getInstance()
            iterCal.timeInMillis = startTs
            for (i in 0..29) {
                val dKey = dateKeyFormat.format(iterCal.time)
                val dayLogs = logsByDate[dKey] ?: emptyList()
                val totalAmount = dayLogs.sumOf { it.amountMl }
                val effectiveHydration = dayLogs.sumOf { it.effectiveHydrationMl }
                val pct = if (goalMl > 0) ((effectiveHydration.toFloat() / goalMl) * 100).toInt() else 0

                summaries.add(
                    DaySummary(
                        dateKey = dKey,
                        dayLabel = dayLabelFormat.format(iterCal.time),
                        totalAmountMl = totalAmount,
                        effectiveHydrationMl = effectiveHydration,
                        targetGoalMl = goalMl,
                        percentage = pct.coerceIn(0, 200),
                        isToday = dKey == todayKey,
                        logsCount = dayLogs.size
                    )
                )
                iterCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            summaries
        }
    }

    /**
     * Computes streak & stats from all recorded logs
     */
    fun getStatsFlow(goalMl: Int): Flow<HydrationStats> {
        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return waterLogDao.getAllLogs().map { logs ->
            if (logs.isEmpty()) {
                HydrationStats(0, 0, 0, 0, 0, BeverageType.WATER, 0)
            } else {
                val grouped = logs.groupBy { it.dateKey }
                val daysMeetingGoal = grouped.count { (_, dayLogs) ->
                    dayLogs.sumOf { it.effectiveHydrationMl } >= goalMl
                }
                val goalRate = if (grouped.isNotEmpty()) (daysMeetingGoal * 100) / grouped.size else 0

                // Calculate current streak
                var currentStreak = 0
                val cal = Calendar.getInstance()
                val todayKey = dateKeyFormat.format(cal.time)
                val todayDrank = grouped[todayKey]?.sumOf { it.effectiveHydrationMl } ?: 0
                if (todayDrank >= goalMl) {
                    currentStreak++
                }
                // Check backwards
                cal.add(Calendar.DAY_OF_YEAR, -1)
                while (true) {
                    val key = dateKeyFormat.format(cal.time)
                    val drank = grouped[key]?.sumOf { it.effectiveHydrationMl } ?: 0
                    if (drank >= goalMl) {
                        currentStreak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }

                // Beverage breakdown
                val bevCounts = logs.groupingBy { it.beverageType }.eachCount()
                val topBevName = bevCounts.maxByOrNull { it.value }?.key ?: BeverageType.WATER.name
                val topBeverage = BeverageType.fromName(topBevName)

                val weeklyAvg = grouped.values.take(7).map { it.sumOf { l -> l.amountMl } }.average().toInt()
                val monthlyAvg = grouped.values.take(30).map { it.sumOf { l -> l.amountMl } }.average().toInt()

                HydrationStats(
                    currentStreakDays = currentStreak,
                    bestStreakDays = maxOf(currentStreak, daysMeetingGoal),
                    weeklyAverageMl = if (weeklyAvg > 0) weeklyAvg else 0,
                    monthlyAverageMl = if (monthlyAvg > 0) monthlyAvg else 0,
                    goalCompletionRatePercent = goalRate,
                    topBeverage = topBeverage,
                    totalDrinksLogged = logs.size
                )
            }
        }
    }
}
