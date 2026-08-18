package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.HydroDatabase
import com.example.data.model.BeverageType
import com.example.data.model.WaterLogEntity
import com.example.notification.NotificationHelper
import com.example.receiver.WaterReminderReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaterTrackerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_ADD_WATER = "com.example.ACTION_WIDGET_ADD_WATER"
        const val EXTRA_AMOUNT_ML = "EXTRA_AMOUNT_ML"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WaterTrackerWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WaterTrackerWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = HydroDatabase.getDatabase(context)
            val todayLogs = db.waterLogDao().getLogsForDateDirect(WaterLogEntity.getCurrentDateKey())
            val currentMl = todayLogs.sumOf { it.effectiveHydrationMl }
            val goalMl = 2500 // default or load from prefs

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, currentMl, goalMl)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_ADD_WATER) {
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
                val goalMl = 2500

                // Update lock screen notification
                NotificationHelper.updateLockScreenProgress(context, currentMl, goalMl, true)

                // Update widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WaterTrackerWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                for (id in ids) {
                    updateAppWidget(context, appWidgetManager, id, currentMl, goalMl)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        currentMl: Int,
        goalMl: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_water_tracker)

        val pct = if (goalMl > 0) ((currentMl.toFloat() / goalMl) * 100).toInt() else 0
        views.setTextViewText(R.id.widget_text_percent, "$pct%")
        views.setTextViewText(R.id.widget_text_amount, "$currentMl / $goalMl ml")
        views.setProgressBar(R.id.widget_progress_bar, goalMl, currentMl.coerceAtMost(goalMl), false)

        // Launch App on tap root
        val appIntent = Intent(context, MainActivity::class.java)
        val appPending = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, appPending)

        // Quick Add 250ml
        val add250Intent = Intent(context, WaterTrackerWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_ADD_WATER
            putExtra(EXTRA_AMOUNT_ML, 250)
        }
        val add250Pending = PendingIntent.getBroadcast(
            context, 250 + appWidgetId, add250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_add_250, add250Pending)

        // Quick Add 500ml
        val add500Intent = Intent(context, WaterTrackerWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_ADD_WATER
            putExtra(EXTRA_AMOUNT_ML, 500)
        }
        val add500Pending = PendingIntent.getBroadcast(
            context, 500 + appWidgetId, add500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_add_500, add500Pending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
