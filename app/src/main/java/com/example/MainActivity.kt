package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroCyan
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.HydroTealDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.WaterTrackerViewModel

enum class NavigationTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    HOME("Today", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop),
    INSIGHTS("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    REMINDERS("Reminders", Icons.Filled.Alarm, Icons.Outlined.Alarm),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: WaterTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: WaterTrackerViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showGoalEditDialog by remember { mutableStateOf(false) }

    // Request notification and activity recognition permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val userSettings by viewModel.userSettings.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val todayEffectiveHydration by viewModel.todayEffectiveHydration.collectAsState()
    val weeklySummaries by viewModel.weeklySummaries.collectAsState()
    val monthlySummaries by viewModel.monthlySummaries.collectAsState()
    val hydrationStats by viewModel.hydrationStats.collectAsState()
    val detectedSteps by viewModel.detectedSteps.collectAsState()
    val activityLevel by viewModel.activityLevel.collectAsState()
    val sensorAvailable by viewModel.sensorAvailable.collectAsState()
    val selectedBeverage by viewModel.selectedBeverage.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            // Natural Tones Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .border(1.dp, DarkBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("bottom_nav_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { currentTab = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("nav_tab_${tab.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) HydroTeal else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) HydroTeal else TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        currentMl = todayEffectiveHydration,
                        userSettings = userSettings,
                        todayLogs = todayLogs,
                        weeklySummaries = weeklySummaries,
                        detectedSteps = detectedSteps,
                        activityLevel = activityLevel,
                        sensorAvailable = sensorAvailable,
                        selectedBeverage = selectedBeverage,
                        syncState = syncState,
                        currentUser = currentUser,
                        onQuickAdd = { amount, bev -> viewModel.addWaterLog(amount, bev) },
                        onAddCustomLog = { amount, bev, note -> viewModel.addWaterLog(amount, bev, note) },
                        onDeleteLog = { viewModel.deleteLog(it) },
                        onSelectBeverage = { viewModel.selectBeverage(it) },
                        onEditGoal = { showGoalEditDialog = true },
                        onOpenProfile = { currentTab = NavigationTab.SETTINGS },
                        onSimulateSteps = { viewModel.simulateSensorSteps(it) },
                        onSelectClimate = { viewModel.updateClimateProfile(it) },
                        onToggleAdaptive = { climate, sensor -> viewModel.updateSmartAdaptation(climate, sensor) }
                    )
                }
                NavigationTab.INSIGHTS -> {
                    InsightsScreen(
                        weeklySummaries = weeklySummaries,
                        monthlySummaries = monthlySummaries,
                        hydrationStats = hydrationStats,
                        userSettings = userSettings
                    )
                }
                NavigationTab.REMINDERS -> {
                    RemindersScreen(
                        userSettings = userSettings,
                        detectedSteps = detectedSteps,
                        onUpdateFrequency = { viewModel.updateReminderFrequency(it) },
                        onToggleLockScreen = { viewModel.updateLockScreenProgress(it) },
                        onToggleSmartAdaptation = { c, s -> viewModel.updateSmartAdaptation(c, s) },
                        onTestNotification = { viewModel.triggerTestNotification() }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        userSettings = userSettings,
                        currentUser = currentUser,
                        syncState = syncState,
                        onUpdateGoal = { viewModel.updateDailyGoal(it) },
                        onUpdateUnitSystem = { viewModel.updateUnitSystem(it) },
                        onUpdateClimate = { viewModel.updateClimateProfile(it) },
                        onSignInWithGoogle = { ctx -> viewModel.signInWithGoogle(ctx) },
                        onSignOut = { viewModel.signOut() },
                        onSyncNow = { viewModel.syncNow() }
                    )
                }
            }
        }
    }

    // Goal Edit Dialog
    if (showGoalEditDialog) {
        var tempGoal by remember(userSettings.dailyGoalMl) { mutableIntStateOf(userSettings.dailyGoalMl) }
        Dialog(onDismissRequest = { showGoalEditDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(28.dp))
                    .padding(22.dp)
                    .testTag("goal_edit_dialog")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Customize Daily Target",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = userSettings.unitSystem.format(tempGoal),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = tempGoal.toFloat(),
                        onValueChange = { tempGoal = it.toInt() },
                        valueRange = 1000f..5000f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = HydroTeal,
                            activeTrackColor = HydroBlue,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGoalEditDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                viewModel.updateDailyGoal(tempGoal)
                                showGoalEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HydroTeal,
                                contentColor = HydroTealDark
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Save Goal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
