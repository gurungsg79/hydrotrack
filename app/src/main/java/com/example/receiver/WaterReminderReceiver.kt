package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.HydroDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.BeverageType
import com.example.data.model.WaterLogEntity
import com.example.notification.NotificationHelper
import com.example.notification.ReminderScheduler
import com.example.widget.WaterTrackerWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WaterReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_WATER_REMINDER = "com.example.ACTION_WATER_REMINDER"
        const val ACTION_QUICK_ADD_WATER = "com.example.ACTION_QUICK_ADD_WATER"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"

        const val EXTRA_AMOUNT_ML = "EXTRA_AMOUNT_ML"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            ACTION_WATER_REMINDER -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = HydroDatabase.getDatabase(context)
                    val todayLogs = db.waterLogDao().getLogsForDateDirect(WaterLogEntity.getCurrentDateKey())
                    val currentMl = todayLogs.sumOf { it.effectiveHydrationMl }

                    val prefs = UserPreferencesRepository(context)
                    val settings = prefs.userSettingsFlow.firstOrNull()
                    val goalMl = settings?.dailyGoalMl ?: 2500

                    val reminderNum = intent.getIntExtra("REMINDER_INDEX", 1)
                    val totalReminders = intent.getIntExtra("TOTAL_REMINDERS", 4)
                    val isSnooze = intent.getBooleanExtra("IS_SNOOZE", false)

                    val title = if (isSnooze) {
                        "⏰ Hydration Reminder (Snoozed)"
                    } else {
                        "💧 Hydration Check-in ($reminderNum of $totalReminders)"
                    }

                    val message = when {
                        currentMl >= goalMl -> "You've reached your daily goal! Keep sipping to stay refreshed."
                        currentMl == 0 -> "Start your day fresh with a refreshing glass of water!"
                        else -> "Drink a glass now to maintain your healthy streak."
                    }

                    NotificationHelper.showHydrationReminder(
                        context = context,
                        title = title,
                        message = message,
                        currentMl = currentMl,
                        goalMl = goalMl
                    )
                }
            }

            ACTION_QUICK_ADD_WATER -> {
                val amount = intent.getIntExtra(EXTRA_AMOUNT_ML, 250)
                CoroutineScope(Dispatchers.IO).launch {
                    val db = HydroDatabase.getDatabase(context)
                    val effective = BeverageType.WATER.calculateEffectiveHydration(amount)
                    val entity = WaterLogEntity(
                        amountMl = amount,
                        effectiveHydrationMl = effective,
                        beverageType = BeverageType.WATER.name,
                        syncedToCloud = false,
                        dateKey = WaterLogEntity.getCurrentDateKey()
                    )
                    db.waterLogDao().insertLog(entity)

                    val todayLogs = db.waterLogDao().getLogsForDateDirect(WaterLogEntity.getCurrentDateKey())
                    val currentMl = todayLogs.sumOf { it.effectiveHydrationMl }

                    val prefs = UserPreferencesRepository(context)
                    val settings = prefs.userSettingsFlow.firstOrNull()
                    val goalMl = settings?.dailyGoalMl ?: 2500

                    // Dismiss reminder notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(NotificationHelper.NOTIFICATION_ID_REMINDER)

                    // Update Lock screen progress notification
                    val lockscreenEnabled = settings?.lockScreenProgressEnabled ?: true
                    NotificationHelper.updateLockScreenProgress(context, currentMl, goalMl, lockscreenEnabled)

                    // Celebrate if just reached goal
                    if (currentMl >= goalMl && (currentMl - effective) < goalMl) {
                        NotificationHelper.showGoalReachedCelebration(context, currentMl)
                    }

                    // Update Home Screen Widget
                    WaterTrackerWidgetProvider.updateAllWidgets(context)
                }
            }

            ACTION_SNOOZE -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NotificationHelper.NOTIFICATION_ID_REMINDER)
                ReminderScheduler(context).snoozeReminder(30)
                Toast.makeText(context, "Reminder snoozed for 30 minutes 💧", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
