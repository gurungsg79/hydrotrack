package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.UnitSystem
import com.example.data.model.UserSettings
import com.example.data.repository.DaySummary
import com.example.data.repository.HydrationStats
import com.example.ui.components.WeeklyStatsBarChart
import com.example.ui.theme.DarkBackground
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
import com.example.ui.theme.TextTitle

@Composable
fun InsightsScreen(
    weeklySummaries: List<DaySummary>,
    monthlySummaries: List<DaySummary>,
    hydrationStats: HydrationStats,
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(0) } // 0: Weekly, 1: Monthly

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 28.dp)
            .testTag("insights_screen_content")
    ) {
        // Header
        Text(
            text = "HYDRATION INSIGHTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Progress & Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextTitle
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Streak and Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Streak Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Text(
                            text = "Current Streak",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${hydrationStats.currentStreakDays} Days",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroAmber
                    )
                    Text(
                        text = "Best: ${hydrationStats.bestStreakDays} days",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Goal Rate Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🎯", fontSize = 16.sp)
                        Text(
                            text = "Goal Reach Rate",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${hydrationStats.goalCompletionRatePercent}%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroMint
                    )
                    Text(
                        text = "${hydrationStats.totalDrinksLogged} total logs",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Timeframe Pill Selector (Weekly / Monthly)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTimeframe == 0) HydroBlue else Color.Transparent)
                    .clickable { selectedTimeframe = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Weekly Chart",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTimeframe == 0) Color(0xFF00344D) else TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTimeframe == 1) HydroBlue else Color.Transparent)
                    .clickable { selectedTimeframe = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Monthly Overview (30 Days)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTimeframe == 1) Color(0xFF00344D) else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTimeframe == 0) {
            // Weekly Breakdown
            WeeklyStatsBarChart(
                weeklySummaries = weeklySummaries,
                unitSystem = userSettings.unitSystem,
                goalMl = userSettings.dailyGoalMl
            )
        } else {
            // Monthly 30 Days Breakdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(28.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "30-Day Trend",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Avg: ${userSettings.unitSystem.format(hydrationStats.monthlyAverageMl)}",
                            fontSize = 11.sp,
                            color = HydroTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monthly 30 bars representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        monthlySummaries.forEach { day ->
                            val heightFraction = if (userSettings.dailyGoalMl > 0) {
                                (day.totalAmountMl.toFloat() / userSettings.dailyGoalMl).coerceIn(0.05f, 1f)
                            } else 0.05f

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(
                                        when {
                                            day.isToday -> HydroCyan
                                            day.totalAmountMl >= userSettings.dailyGoalMl -> HydroTeal
                                            day.totalAmountMl > 0 -> Color(0xFF3B4854)
                                            else -> DarkSurfaceVariant
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "30 days ago", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "Today", fontSize = 10.sp, color = HydroBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Favorite Beverage & Stats Highlights
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Hydration Quality",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                            Text(text = hydrationStats.topBeverage.iconEmoji, fontSize = 20.sp)
                        }

                        Column {
                            Text(
                                text = "Top Source: ${hydrationStats.topBeverage.displayName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hydration factor: ${(hydrationStats.topBeverage.hydrationFactor * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = HydroTeal
                            )
                        }
                    }

                    Text(
                        text = "Optimal 💧",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HydroMint
                    )
                }
            }
        }
    }
}
