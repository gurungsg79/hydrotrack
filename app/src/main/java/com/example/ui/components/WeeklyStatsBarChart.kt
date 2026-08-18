package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.repository.DaySummary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WeeklyStatsBarChart(
    weeklySummaries: List<DaySummary>,
    unitSystem: UnitSystem = UnitSystem.METRIC,
    goalMl: Int = 2500,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(32.dp))
            .padding(18.dp)
            .testTag("weekly_stats_card")
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Stats",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    val activeSummary = if (selectedDayIndex != null && selectedDayIndex in weeklySummaries.indices) {
                        weeklySummaries[selectedDayIndex!!]
                    } else null

                    if (activeSummary != null) {
                        Text(
                            text = "${activeSummary.dateKey}: ${unitSystem.format(activeSummary.totalAmountMl)} (${activeSummary.percentage}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HydroBlue
                        )
                    } else {
                        Text(
                            text = "Last 7 Days Progress",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                val avg = if (weeklySummaries.isNotEmpty()) {
                    weeklySummaries.map { it.totalAmountMl }.average().toInt()
                } else 0

                Text(
                    text = "Avg: ${unitSystem.format(avg)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = HydroTeal
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bars Chart Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklySummaries.forEachIndexed { index, summary ->
                    val isSelected = selectedDayIndex == index
                    val fraction = if (goalMl > 0) {
                        (summary.totalAmountMl.toFloat() / goalMl).coerceIn(0.08f, 1.0f)
                    } else 0.08f

                    val animatedHeight by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(durationMillis = 600 + (index * 60)),
                        label = "barHeight$index"
                    )

                    val barColor = when {
                        summary.isToday -> HydroBlue
                        summary.totalAmountMl >= goalMl -> HydroTeal
                        isSelected -> Color(0xFF5A6E7C)
                        summary.totalAmountMl > 0 -> Color(0xFF3B4854)
                        else -> DarkSurfaceVariant
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Labels (M, T, W, T, F, S, S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklySummaries.forEachIndexed { index, summary ->
                    val isSelected = selectedDayIndex == index
                    Text(
                        text = summary.dayLabel,
                        fontSize = 11.sp,
                        fontWeight = if (summary.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (summary.isToday) HydroBlue else if (isSelected) HydroTeal else TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
