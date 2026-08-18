package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.WaterReminderReceiver

object NotificationHelper {

    const val CHANNEL_REMINDERS = "hydro_reminders_channel"
    const val CHANNEL_LOCKSCREEN = "hydro_lockscreen_channel"
    const val CHANNEL_CELEBRATION = "hydro_celebration_channel"

    const val NOTIFICATION_ID_REMINDER = 1001
    const val NOTIFICATION_ID_LOCKSCREEN = 1002
    const val NOTIFICATION_ID_CELEBRATION = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders to drink water and stay healthy"
                enableVibration(true)
                setShowBadge(true)
            }

            val lockscreenChannel = NotificationChannel(
                CHANNEL_LOCKSCREEN,
                "Lock Screen Progress Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live progress bar on your lock screen and status bar"
                setShowBadge(false)
            }

            val celebrationChannel = NotificationChannel(
                CHANNEL_CELEBRATION,
                "Goal Celebrations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Celebrations when reaching your daily hydration goal"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, lockscreenChannel, celebrationChannel))
        }
    }

    fun showHydrationReminder(
        context: Context,
        title: String = "💧 Time to Hydrate!",
        message: String = "A glass of water keeps your energy high. Tap below to log.",
        currentMl: Int = 0,
        goalMl: Int = 2500
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Open App Intent
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Add 250ml
        val add250Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_QUICK_ADD_WATER
            putExtra(WaterReminderReceiver.EXTRA_AMOUNT_ML, 250)
        }
        val add250Pending = PendingIntent.getBroadcast(
            context, 1, add250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Add 500ml
        val add500Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_QUICK_ADD_WATER
            putExtra(WaterReminderReceiver.EXTRA_AMOUNT_ML, 500)
        }
        val add500Pending = PendingIntent.getBroadcast(
            context, 2, add500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 30 mins
        val snoozeIntent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_SNOOZE
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, 3, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pct = if (goalMl > 0) ((currentMl.toFloat() / goalMl) * 100).toInt() else 0

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("$message ($currentMl / $goalMl ml - $pct%)")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nToday: $currentMl / $goalMl ml ($pct% achieved)"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(appPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_input_add, "+250 ml", add250Pending)
            .addAction(android.R.drawable.ic_input_add, "+500 ml", add500Pending)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 30m", snoozePending)

        notificationManager.notify(NOTIFICATION_ID_REMINDER, builder.build())
    }

    fun updateLockScreenProgress(
        context: Context,
        currentMl: Int,
        goalMl: Int,
        enabled: Boolean = true
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!enabled) {
            notificationManager.cancel(NOTIFICATION_ID_LOCKSCREEN)
            return
        }

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val add250Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_QUICK_ADD_WATER
            putExtra(WaterReminderReceiver.EXTRA_AMOUNT_ML, 250)
        }
        val add250Pending = PendingIntent.getBroadcast(
            context, 11, add250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val add500Intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = WaterReminderReceiver.ACTION_QUICK_ADD_WATER
            putExtra(WaterReminderReceiver.EXTRA_AMOUNT_ML, 500)
        }
        val add500Pending = PendingIntent.getBroadcast(
            context, 12, add500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pct = if (goalMl > 0) ((currentMl.toFloat() / goalMl) * 100).toInt() else 0

        val builder = NotificationCompat.Builder(context, CHANNEL_LOCKSCREEN)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("💧 HydroTrack • $pct% Completed")
            .setContentText("$currentMl / $goalMl ml logged today")
            .setProgress(goalMl, currentMl.coerceAtMost(goalMl), false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(appPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+250ml", add250Pending)
            .addAction(android.R.drawable.ic_input_add, "+500ml", add500Pending)

        notificationManager.notify(NOTIFICATION_ID_LOCKSCREEN, builder.build())
    }

    fun showGoalReachedCelebration(context: Context, totalMl: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val appIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_CELEBRATION)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle("🎉 Daily Hydration Goal Reached!")
            .setContentText("Awesome work! You completed $totalMl ml today. Keep up the streak!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID_CELEBRATION, builder.build())
    }
}
