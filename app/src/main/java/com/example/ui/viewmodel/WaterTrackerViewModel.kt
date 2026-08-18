package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HydroDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.ActivityLevel
import com.example.data.model.BeverageType
import com.example.data.model.ClimateProfile
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import com.example.data.model.WaterLogEntity
import com.example.data.repository.DaySummary
import com.example.data.repository.HydrationStats
import com.example.data.repository.WaterRepository
import com.example.data.sync.FirebaseSyncManager
import com.example.data.sync.SyncState
import com.example.notification.NotificationHelper
import com.example.notification.ReminderScheduler
import com.example.sensor.ActivitySensorManager
import com.example.widget.WaterTrackerWidgetProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WaterTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HydroDatabase.getDatabase(application)
    private val waterRepository = WaterRepository(db.waterLogDao())
    private val prefsRepo = UserPreferencesRepository(application)
    private val reminderScheduler = ReminderScheduler(application)
    private val sensorManager = ActivitySensorManager(application)
    private val syncManager = FirebaseSyncManager(application, db.waterLogDao())

    val userSettings: StateFlow<UserSettings> = prefsRepo.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val todayLogs: StateFlow<List<WaterLogEntity>> = waterRepository.getTodayLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val detectedSteps: StateFlow<Int> = sensorManager.stepCount
    val activityLevel: StateFlow<ActivityLevel> = sensorManager.activityLevel
    val sensorAvailable: StateFlow<Boolean> = sensorManager.sensorAvailable

    private val _selectedBeverage = MutableStateFlow(BeverageType.WATER)
    val selectedBeverage: StateFlow<BeverageType> = _selectedBeverage.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState
    val currentUser: StateFlow<FirebaseUser?> = syncManager.currentUser

    // Today's total effective hydration
    val todayEffectiveHydration: StateFlow<Int> = todayLogs.combine(userSettings) { logs, _ ->
        logs.sumOf { it.effectiveHydrationMl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayTotalAmount: StateFlow<Int> = todayLogs.combine(userSettings) { logs, _ ->
        logs.sumOf { it.amountMl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Weekly summary
    val weeklySummaries: StateFlow<List<DaySummary>> = userSettings.flatMapLatest { settings ->
        val effectiveGoal = settings.calculateAdaptiveGoal(detectedSteps.value)
        waterRepository.getPast7DaysFlow(effectiveGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly summary
    val monthlySummaries: StateFlow<List<DaySummary>> = userSettings.flatMapLatest { settings ->
        val effectiveGoal = settings.calculateAdaptiveGoal(detectedSteps.value)
        waterRepository.getPast30DaysFlow(effectiveGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall stats
    val hydrationStats: StateFlow<HydrationStats> = userSettings.flatMapLatest { settings ->
        val effectiveGoal = settings.calculateAdaptiveGoal(detectedSteps.value)
        waterRepository.getStatsFlow(effectiveGoal)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HydrationStats(0, 0, 0, 0, 0, BeverageType.WATER, 0)
    )

    init {
        NotificationHelper.createNotificationChannels(application)
        sensorManager.startListening()

        viewModelScope.launch {
            userSettings.collect { settings ->
                reminderScheduler.scheduleDailyReminders(settings, detectedSteps.value)
                updateLockScreenNotification()
            }
        }
    }

    fun selectBeverage(beverage: BeverageType) {
        _selectedBeverage.value = beverage
    }

    fun addWaterLog(
        amountMl: Int,
        beverageType: BeverageType = _selectedBeverage.value,
        note: String = ""
    ) {
        viewModelScope.launch {
            val prevAmount = todayEffectiveHydration.value
            waterRepository.addWaterLog(amountMl, beverageType, note)

            val settings = userSettings.value
            val currentAmount = prevAmount + beverageType.calculateEffectiveHydration(amountMl)
            val goal = settings.calculateAdaptiveGoal(detectedSteps.value)

            // Update Lock Screen progress notification
            NotificationHelper.updateLockScreenProgress(
                context = getApplication(),
                currentMl = currentAmount,
                goalMl = goal,
                enabled = settings.lockScreenProgressEnabled
            )

            // Goal celebration
            if (currentAmount >= goal && prevAmount < goal) {
                NotificationHelper.showGoalReachedCelebration(getApplication(), currentAmount)
            }

            // Update Home Screen widgets
            WaterTrackerWidgetProvider.updateAllWidgets(getApplication())

            // Sync with Firebase if signed in
            if (currentUser.value != null) {
                syncManager.syncDataWithCloud()
            }
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            waterRepository.deleteLog(id)
            updateLockScreenNotification()
            WaterTrackerWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            prefsRepo.updateDailyGoal(goalMl)
            updateLockScreenNotification()
            WaterTrackerWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun updateClimateProfile(profile: ClimateProfile) {
        viewModelScope.launch {
            prefsRepo.updateClimateProfile(profile)
            reminderScheduler.scheduleDailyReminders(userSettings.value.copy(climateProfile = profile), detectedSteps.value)
            updateLockScreenNotification()
        }
    }

    fun updateReminderFrequency(frequency: Int) {
        viewModelScope.launch {
            prefsRepo.updateReminderFrequency(frequency)
            reminderScheduler.scheduleDailyReminders(userSettings.value.copy(reminderFrequencyPerDay = frequency), detectedSteps.value)
        }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            prefsRepo.updateUnitSystem(unitSystem)
        }
    }

    fun updateLockScreenProgress(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateLockScreenProgress(enabled)
            val current = todayEffectiveHydration.value
            val goal = userSettings.value.calculateAdaptiveGoal(detectedSteps.value)
            NotificationHelper.updateLockScreenProgress(getApplication(), current, goal, enabled)
        }
    }

    fun updateSmartAdaptation(climateEnabled: Boolean, sensorEnabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateSmartAdaptation(climateEnabled, sensorEnabled)
            reminderScheduler.scheduleDailyReminders(userSettings.value, detectedSteps.value)
        }
    }

    fun simulateSensorSteps(steps: Int) {
        sensorManager.setSimulatedSteps(steps)
        val settings = userSettings.value
        reminderScheduler.scheduleDailyReminders(settings, steps)
        updateLockScreenNotification()
    }

    fun triggerTestNotification() {
        reminderScheduler.triggerImmediateTestReminder()
    }

    fun signInWithGoogle(activityContext: Context, webClientId: String = "") {
        viewModelScope.launch {
            val result = syncManager.signInWithGoogle(activityContext, webClientId)
            result.onSuccess { user ->
                prefsRepo.updateSyncInfo(user.email ?: "", System.currentTimeMillis())
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            syncManager.signOut()
            prefsRepo.updateSyncInfo("", 0L)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncManager.syncDataWithCloud()
            prefsRepo.updateSyncInfo(currentUser.value?.email ?: "", System.currentTimeMillis())
            WaterTrackerWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    private fun updateLockScreenNotification() {
        val settings = userSettings.value
        val current = todayEffectiveHydration.value
        val goal = settings.calculateAdaptiveGoal(detectedSteps.value)
        NotificationHelper.updateLockScreenProgress(
            getApplication(),
            current,
            goal,
            settings.lockScreenProgressEnabled
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
    }
}
