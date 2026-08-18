package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLevel
import com.example.data.model.BeverageType
import com.example.data.model.ClimateProfile
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import com.example.data.model.WaterLogEntity
import com.example.data.repository.DaySummary
import com.example.data.sync.SyncState
import com.example.ui.components.AddCustomWaterDialog
import com.example.ui.components.BeveragePickerSheet
import com.example.ui.components.HydrationWaveGauge
import com.example.ui.components.IntelligentSensorCard
import com.example.ui.components.QuickAddGrid
import com.example.ui.components.TodayLogList
import com.example.ui.components.WeeklyStatsBarChart
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroCyan
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTitle
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(
    currentMl: Int,
    userSettings: UserSettings,
    todayLogs: List<WaterLogEntity>,
    weeklySummaries: List<DaySummary>,
    detectedSteps: Int,
    activityLevel: ActivityLevel,
    sensorAvailable: Boolean,
    selectedBeverage: BeverageType,
    syncState: SyncState,
    currentUser: FirebaseUser?,
    onQuickAdd: (Int, BeverageType) -> Unit,
    onAddCustomLog: (Int, BeverageType, String) -> Unit,
    onDeleteLog: (Long) -> Unit,
    onSelectBeverage: (BeverageType) -> Unit,
    onEditGoal: () -> Unit,
    onOpenProfile: () -> Unit,
    onSimulateSteps: (Int) -> Unit,
    onSelectClimate: (ClimateProfile) -> Unit,
    onToggleAdaptive: (Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var showBeverageSheet by remember { mutableStateOf(false) }

    val adaptiveGoal = userSettings.calculateAdaptiveGoal(detectedSteps)

    // Pulse animation for sync indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 28.dp)
            .testTag("home_screen_content")
    ) {
        // Header (Matches Natural Tones Header)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DAILY HYDRATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "HydroTrack",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable(onClick = onOpenProfile)
                    .testTag("profile_button"),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser != null) {
                    Text(
                        text = (currentUser.displayName?.take(1) ?: currentUser.email?.take(1) ?: "👤").uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroTeal
                    )
                } else {
                    Text(text = "👤", fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Wave Gauge
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HydrationWaveGauge(
                currentMl = currentMl,
                goalMl = adaptiveGoal,
                unitSystem = userSettings.unitSystem,
                size = 250.dp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Firebase Sync Badge (Matches Natural Tones Spec)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("sync_status_badge")
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentUser != null) HydroTeal else Color(0xFF888888))
                        .alpha(if (syncState is SyncState.Syncing) pulseAlpha else 1.0f)
                )

                Text(
                    text = when (syncState) {
                        is SyncState.Syncing -> "Syncing with Firebase..."
                        is SyncState.Synced -> if (currentUser != null) "Synced with Cloud" else "Local mode (Offline ready)"
                        is SyncState.Error -> "Offline (Room Cache)"
                        is SyncState.Idle -> if (currentUser != null) "Cloud Sync Active" else "Saved locally"
                    },
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Quick Add Grid
        QuickAddGrid(
            unitSystem = userSettings.unitSystem,
            selectedBeverage = selectedBeverage,
            onQuickAdd = onQuickAdd,
            onOpenCustomDialog = { showCustomDialog = true },
            onSelectBeverage = { showBeverageSheet = true },
            onEditGoal = onEditGoal
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Intelligent Sensor Card (Climate & Sensor live adjustments)
        IntelligentSensorCard(
            settings = userSettings,
            detectedSteps = detectedSteps,
            activityLevel = activityLevel,
            sensorAvailable = sensorAvailable,
            onSimulateSteps = onSimulateSteps,
            onSelectClimate = onSelectClimate,
            onToggleAdaptive = onToggleAdaptive
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Weekly Stats Overview
        WeeklyStatsBarChart(
            weeklySummaries = weeklySummaries,
            unitSystem = userSettings.unitSystem,
            goalMl = adaptiveGoal
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Today's Detailed Log List
        TodayLogList(
            logs = todayLogs,
            unitSystem = userSettings.unitSystem,
            onDeleteLog = onDeleteLog
        )
    }

    // Dialogs
    if (showCustomDialog) {
        AddCustomWaterDialog(
            unitSystem = userSettings.unitSystem,
            currentBeverage = selectedBeverage,
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount, bev, note ->
                onAddCustomLog(amount, bev, note)
                showCustomDialog = false
            }
        )
    }

    if (showBeverageSheet) {
        BeveragePickerSheet(
            selectedBeverage = selectedBeverage,
            onSelect = onSelectBeverage,
            onDismiss = { showBeverageSheet = false }
        )
    }
}
