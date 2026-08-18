package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClimateProfile
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import com.example.data.sync.SyncState
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
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    currentUser: FirebaseUser?,
    syncState: SyncState,
    onUpdateGoal: (Int) -> Unit,
    onUpdateUnitSystem: (UnitSystem) -> Unit,
    onUpdateClimate: (ClimateProfile) -> Unit,
    onSignInWithGoogle: (Context) -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customGoalValue by remember(userSettings.dailyGoalMl) { mutableStateOf(userSettings.dailyGoalMl.toFloat()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 28.dp)
            .testTag("settings_screen_content")
    ) {
        // Header
        Text(
            text = "PREFERENCES & SYNC",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextTitle
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Firebase Cloud Account & Sync Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
                .testTag("firebase_account_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = HydroBlue)
                        }

                        Column {
                            Text(
                                text = "Cross-Device Sync",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (currentUser != null) currentUser.email ?: "Firebase Account" else "Offline / Local Mode",
                                fontSize = 11.sp,
                                color = if (currentUser != null) HydroTeal else TextMuted
                            )
                        }
                    }

                    if (currentUser != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .clickable(onClick = onSyncNow)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = HydroTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (currentUser == null) {
                    Text(
                        text = "Sign in with Google to sync your hydration logs across your Android phones, tablets, and watches in real-time.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSignInWithGoogle(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HydroTeal,
                            contentColor = HydroTealDark
                        )
                    ) {
                        Text(text = "Sign In with Google (Firebase)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${when (syncState) {
                                is SyncState.Syncing -> "Syncing..."
                                is SyncState.Synced -> "Synced"
                                is SyncState.Error -> "Offline"
                                is SyncState.Idle -> "Connected"
                            }}",
                            fontSize = 11.sp,
                            color = HydroMint
                        )

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Text(text = "Sign Out", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Hydration Goal Slider
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
                            text = "Daily Base Goal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Recommended based on body weight & health standards",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = userSettings.unitSystem.format(customGoalValue.toInt()),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = customGoalValue,
                    onValueChange = { customGoalValue = it },
                    onValueChangeFinished = { onUpdateGoal(customGoalValue.toInt()) },
                    valueRange = 1000f..5000f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = HydroTeal,
                        activeTrackColor = HydroBlue,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1,000 ml", fontSize = 10.sp, color = TextMuted)
                    Text(text = "3,000 ml", fontSize = 10.sp, color = TextMuted)
                    Text(text = "5,000 ml", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Unit System Switcher
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
                Column {
                    Text(
                        text = "Measurement Unit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Choose preferred volume unit",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (userSettings.unitSystem == UnitSystem.METRIC) HydroBlue else Color.Transparent)
                            .clickable { onUpdateUnitSystem(UnitSystem.METRIC) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Metric (ml)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userSettings.unitSystem == UnitSystem.METRIC) Color(0xFF00344D) else TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (userSettings.unitSystem == UnitSystem.IMPERIAL) HydroBlue else Color.Transparent)
                            .clickable { onUpdateUnitSystem(UnitSystem.IMPERIAL) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Imperial (fl oz)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userSettings.unitSystem == UnitSystem.IMPERIAL) Color(0xFF00344D) else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Home Screen Widget & Lock Screen Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = "Widget", tint = HydroTeal)
                }

                Column {
                    Text(
                        text = "Home Screen Widget Included",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Long-press your home screen to add the HydroTrack quick-logging widget with live sync.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
