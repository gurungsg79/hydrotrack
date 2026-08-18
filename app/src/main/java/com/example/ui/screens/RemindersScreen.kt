package com.example.ui.screens

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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSettings
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroMint
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.HydroTealDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTitle

@Composable
fun RemindersScreen(
    userSettings: UserSettings,
    detectedSteps: Int,
    onUpdateFrequency: (Int) -> Unit,
    onToggleLockScreen: (Boolean) -> Unit,
    onToggleSmartAdaptation: (Boolean, Boolean) -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frequencies = listOf(4, 6, 8, 10)
    val adaptiveFrequency = userSettings.calculateAdaptiveReminderCount(detectedSteps)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 28.dp)
            .testTag("reminders_screen_content")
    ) {
        // Header
        Text(
            text = "SMART REMINDERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Intelligent Push System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextTitle
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Daily Reminder Frequency Selector (at least 4 times a day)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Reminder Frequency",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Evenly scheduled between 8:00 AM – 10:00 PM",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(HydroTeal)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${userSettings.reminderFrequencyPerDay}x / day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HydroTealDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    frequencies.forEach { freq ->
                        val isSelected = userSettings.reminderFrequencyPerDay == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) HydroBlue else DarkSurfaceVariant)
                                .clickable { onUpdateFrequency(freq) }
                                .padding(vertical = 10.dp)
                                .testTag("reminder_freq_${freq}_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${freq}x",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF00344D) else TextPrimary
                                )
                                Text(
                                    text = if (freq == 4) "Default" else if (freq == 8) "Intense" else "Active",
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color(0xFF00344D) else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Climate & Sensor Dynamic Boost
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Climate & Sensor Adaptation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Auto-adjusts reminder intervals during hot weather or high physical exertion",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = userSettings.smartClimateAdaptiveEnabled && userSettings.smartSensorActivityEnabled,
                        onCheckedChange = { isChecked ->
                            onToggleSmartAdaptation(isChecked, isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HydroTealDark,
                            checkedTrackColor = HydroTeal,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }

                if (userSettings.smartClimateAdaptiveEnabled || userSettings.smartSensorActivityEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF152A35))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⚡ Active adaptation: Currently delivering $adaptiveFrequency reminders today based on your ${userSettings.climateProfile.title} climate + $detectedSteps detected steps.",
                            fontSize = 11.sp,
                            color = HydroTeal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lock Screen Live Progress Tracker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lock Screen Progress Bar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ongoing progress notification with quick +250ml and +500ml actions on your lock screen",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = userSettings.lockScreenProgressEnabled,
                    onCheckedChange = onToggleLockScreen,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HydroTealDark,
                        checkedTrackColor = HydroTeal,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Test Notification Action Button
        Button(
            onClick = onTestNotification,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("test_reminder_button"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkSurfaceVariant,
                contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Test", tint = HydroBlue)
                Text(text = "Send Test Reminder Notification", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
