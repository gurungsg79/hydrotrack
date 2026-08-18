package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLevel
import com.example.data.model.ClimateProfile
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroAmber
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroCyan
import com.example.ui.theme.HydroMint
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun IntelligentSensorCard(
    settings: UserSettings,
    detectedSteps: Int,
    activityLevel: ActivityLevel,
    sensorAvailable: Boolean,
    onSimulateSteps: (Int) -> Unit,
    onSelectClimate: (ClimateProfile) -> Unit,
    onToggleAdaptive: (Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedControls by remember { mutableStateOf(false) }

    val adaptiveGoal = settings.calculateAdaptiveGoal(detectedSteps)
    val adaptiveFrequency = settings.calculateAdaptiveReminderCount(detectedSteps)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(28.dp))
            .padding(16.dp)
            .testTag("intelligent_sensor_card")
    ) {
        Column {
            // Header with AI / Sensor badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧠", fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = "Smart Hydration Engine",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Live Climate & Sensor Adaptive",
                            fontSize = 11.sp,
                            color = HydroTeal
                        )
                    }
                }

                Text(
                    text = if (expandedControls) "Close" else "Tune",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HydroBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedControls = !expandedControls }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two dynamic status pills: Activity & Climate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Activity Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = activityLevel.emoji, fontSize = 14.sp)
                            Text(
                                text = "Activity (${detectedSteps} st)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activityLevel.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activityLevel == ActivityLevel.SEDENTARY) TextPrimary else HydroAmber
                        )
                        if (activityLevel.waterBoostMl > 0) {
                            Text(
                                text = "+${activityLevel.waterBoostMl}ml bonus",
                                fontSize = 10.sp,
                                color = HydroMint
                            )
                        }
                    }
                }

                // Climate Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = settings.climateProfile.emoji, fontSize = 14.sp)
                            Text(
                                text = "Climate (${settings.climateProfile.tempEstimateC}°C)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = settings.climateProfile.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydroTeal
                        )
                        Text(
                            text = "${(settings.climateProfile.waterMultiplier * 100).toInt()}% intake factor",
                            fontSize = 10.sp,
                            color = HydroBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Recommendation Outcome Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF13222B))
                    .border(1.dp, Color(0xFF1F485B), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Adapted Daily Target",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$adaptiveGoal ml",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydroCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Reminders Frequency",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$adaptiveFrequency times today",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydroTeal
                        )
                    }
                }
            }

            // Expanded Tuning Controls
            if (expandedControls) {
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Local Climate",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClimateProfile.entries.forEach { profile ->
                        val isSelected = settings.climateProfile == profile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) HydroBlue else DarkSurfaceVariant)
                                .clickable { onSelectClimate(profile) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = profile.emoji, fontSize = 16.sp)
                                Text(
                                    text = profile.name.take(4),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF00344D) else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Activity Simulation Slider
                Text(
                    text = "Simulate Sensor Activity (${detectedSteps} Steps)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Slider(
                    value = detectedSteps.toFloat(),
                    onValueChange = { onSimulateSteps(it.toInt()) },
                    valueRange = 0f..15000f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroTeal,
                        activeTrackColor = HydroBlue,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }
    }
}
