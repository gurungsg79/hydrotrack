package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.UserPreferencesRepository
import com.example.notification.NotificationHelper
import com.example.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            NotificationHelper.createNotificationChannels(context)

            CoroutineScope(Dispatchers.IO).launch {
                val prefs = UserPreferencesRepository(context)
                val settings = prefs.userSettingsFlow.firstOrNull()
                if (settings != null) {
                    val scheduler = ReminderScheduler(context)
                    scheduler.scheduleDailyReminders(settings)
                }
            }
        }
    }
}
