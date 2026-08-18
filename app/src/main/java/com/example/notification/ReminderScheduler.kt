package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.UserSettings
import com.example.receiver.WaterReminderReceiver
import java.util.Calendar

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules dynamic daily reminders (at least 4 times a day)
     * distributed evenly between startHour and endHour.
     */
    fun scheduleDailyReminders(settings: UserSettings, detectedStepsToday: Int = 0) {
        cancelAllReminders()

        val count = settings.calculateAdaptiveReminderCount(detectedStepsToday).coerceAtLeast(4)
        val startHour = settings.reminderStartHour.coerceIn(5, 12)
        val endHour = settings.reminderEndHour.coerceIn(startHour + 4, 23)

        val activeMinutes = (endHour - startHour) * 60
        val intervalMinutes = activeMinutes / count

        for (i in 0 until count) {
            val targetMinutesFromStart = i * intervalMinutes
            val targetHour = startHour + (targetMinutesFromStart / 60)
            val targetMinute = targetMinutesFromStart % 60

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If time has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = WaterReminderReceiver.ACTION_WATER_REMINDER
                putExtra("REMINDER_INDEX", i + 1)
                putExtra("TOTAL_REMINDERS", count)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100 + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun snoozeReminder(minutes: Int = 30) {
        val triggerAt = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_WATER_REMINDER
            putExtra("IS_SNOOZE", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun triggerImmediateTestReminder() {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_WATER_REMINDER
            putExtra("IS_TEST", true)
        }
        context.sendBroadcast(intent)
    }

    fun cancelAllReminders() {
        for (i in 0 until 12) {
            val intent = Intent(context, WaterReminderReceiver::class.java).apply {
                action = WaterReminderReceiver.ACTION_WATER_REMINDER
            }
            val pending = PendingIntent.getBroadcast(
                context,
                100 + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
            }
        }
    }
}
