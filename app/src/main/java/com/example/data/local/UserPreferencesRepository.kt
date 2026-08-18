package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ClimateProfile
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hydro_user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val LOCK_SCREEN_PROGRESS = booleanPreferencesKey("lock_screen_progress")
        val SMART_CLIMATE_ADAPTIVE = booleanPreferencesKey("smart_climate_adaptive")
        val SMART_SENSOR_ACTIVITY = booleanPreferencesKey("smart_sensor_activity")
        val CLIMATE_PROFILE = stringPreferencesKey("climate_profile")
        val REMINDER_FREQUENCY = intPreferencesKey("reminder_frequency")
        val REMINDER_START_HOUR = intPreferencesKey("reminder_start_hour")
        val REMINDER_END_HOUR = intPreferencesKey("reminder_end_hour")
        val SOUND_VIBRATION = booleanPreferencesKey("sound_vibration")
        val USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val AUTO_GOAL_CALC = booleanPreferencesKey("auto_goal_calc")
        val LAST_SYNC_TS = longPreferencesKey("last_sync_ts")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val dailyGoal = preferences[Keys.DAILY_GOAL_ML] ?: 2500
        val unitSystemStr = preferences[Keys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
        val unitSystem = try { UnitSystem.valueOf(unitSystemStr) } catch (e: Exception) { UnitSystem.METRIC }
        val lockScreenProgress = preferences[Keys.LOCK_SCREEN_PROGRESS] ?: true
        val smartClimate = preferences[Keys.SMART_CLIMATE_ADAPTIVE] ?: true
        val smartSensor = preferences[Keys.SMART_SENSOR_ACTIVITY] ?: true
        val climateStr = preferences[Keys.CLIMATE_PROFILE] ?: ClimateProfile.WARM.name
        val climate = ClimateProfile.fromName(climateStr)
        val frequency = preferences[Keys.REMINDER_FREQUENCY] ?: 4
        val startHour = preferences[Keys.REMINDER_START_HOUR] ?: 8
        val endHour = preferences[Keys.REMINDER_END_HOUR] ?: 22
        val soundVib = preferences[Keys.SOUND_VIBRATION] ?: true
        val weight = preferences[Keys.USER_WEIGHT_KG] ?: 70.0f
        val autoGoal = preferences[Keys.AUTO_GOAL_CALC] ?: true
        val syncTs = preferences[Keys.LAST_SYNC_TS] ?: 0L
        val email = preferences[Keys.USER_EMAIL] ?: ""

        UserSettings(
            dailyGoalMl = dailyGoal,
            unitSystem = unitSystem,
            lockScreenProgressEnabled = lockScreenProgress,
            smartClimateAdaptiveEnabled = smartClimate,
            smartSensorActivityEnabled = smartSensor,
            climateProfile = climate,
            reminderFrequencyPerDay = frequency,
            reminderStartHour = startHour,
            reminderEndHour = endHour,
            soundVibrationEnabled = soundVib,
            userWeightKg = weight,
            isAutoGoalCalculated = autoGoal,
            lastSyncTimestamp = syncTs,
            userEmail = email
        )
    }

    suspend fun updateSettings(settings: UserSettings) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DAILY_GOAL_ML] = settings.dailyGoalMl
            preferences[Keys.UNIT_SYSTEM] = settings.unitSystem.name
            preferences[Keys.LOCK_SCREEN_PROGRESS] = settings.lockScreenProgressEnabled
            preferences[Keys.SMART_CLIMATE_ADAPTIVE] = settings.smartClimateAdaptiveEnabled
            preferences[Keys.SMART_SENSOR_ACTIVITY] = settings.smartSensorActivityEnabled
            preferences[Keys.CLIMATE_PROFILE] = settings.climateProfile.name
            preferences[Keys.REMINDER_FREQUENCY] = settings.reminderFrequencyPerDay
            preferences[Keys.REMINDER_START_HOUR] = settings.reminderStartHour
            preferences[Keys.REMINDER_END_HOUR] = settings.reminderEndHour
            preferences[Keys.SOUND_VIBRATION] = settings.soundVibrationEnabled
            preferences[Keys.USER_WEIGHT_KG] = settings.userWeightKg
            preferences[Keys.AUTO_GOAL_CALC] = settings.isAutoGoalCalculated
            preferences[Keys.LAST_SYNC_TS] = settings.lastSyncTimestamp
            preferences[Keys.USER_EMAIL] = settings.userEmail
        }
    }

    suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_ML] = goalMl }
    }

    suspend fun updateClimateProfile(profile: ClimateProfile) {
        context.dataStore.edit { it[Keys.CLIMATE_PROFILE] = profile.name }
    }

    suspend fun updateReminderFrequency(frequency: Int) {
        context.dataStore.edit { it[Keys.REMINDER_FREQUENCY] = frequency.coerceAtLeast(4) }
    }

    suspend fun updateUnitSystem(unit: UnitSystem) {
        context.dataStore.edit { it[Keys.UNIT_SYSTEM] = unit.name }
    }

    suspend fun updateLockScreenProgress(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCK_SCREEN_PROGRESS] = enabled }
    }

    suspend fun updateSmartAdaptation(climateEnabled: Boolean, sensorEnabled: Boolean) {
        context.dataStore.edit {
            it[Keys.SMART_CLIMATE_ADAPTIVE] = climateEnabled
            it[Keys.SMART_SENSOR_ACTIVITY] = sensorEnabled
        }
    }

    suspend fun updateSyncInfo(email: String, timestamp: Long) {
        context.dataStore.edit {
            it[Keys.USER_EMAIL] = email
            it[Keys.LAST_SYNC_TS] = timestamp
        }
    }
}
