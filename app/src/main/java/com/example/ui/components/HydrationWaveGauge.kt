package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitSystem
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HydroBlue
import com.example.ui.theme.HydroCyan
import com.example.ui.theme.HydroTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun HydrationWaveGauge(
    currentMl: Int,
    goalMl: Int,
    unitSystem: UnitSystem = UnitSystem.METRIC,
    size: Dp = 260.dp,
    modifier: Modifier = Modifier
) {
    val progress = if (goalMl > 0) (currentMl.toFloat() / goalMl).coerceIn(0f, 1.5f) else 0f
    val percentDisplay = (progress * 100).toInt()

    // Smooth animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "waterProgress"
    )

    // Continuous wave motion
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("hydration_wave_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.toPx()
            val radius = (diameter - strokeWidth) / 2f
            val center = Offset(diameter / 2f, diameter / 2f)

            // Inner circle path for wave clipping
            val innerRadius = radius - (strokeWidth / 2f) - 6.dp.toPx()
            val clipCirclePath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center.x - innerRadius,
                        center.y - innerRadius,
                        center.x + innerRadius,
                        center.y + innerRadius
                    )
                )
            }

            // Draw wave inside clipped circle
            clipPath(clipCirclePath) {
                // Background behind water inside circle
                drawCircle(
                    color = Color(0xFF161A1E),
                    radius = innerRadius,
                    center = center
                )

                val waterHeightY = center.y + innerRadius - (2f * innerRadius * animatedProgress)
                val waveAmplitude = (6.dp.toPx() * (1f - (animatedProgress - 0.5f) * (animatedProgress - 0.5f) * 2f)).coerceAtLeast(2.dp.toPx())

                // Back subtle wave
                val backWavePath = Path().apply {
                    moveTo(center.x - innerRadius, diameter)
                    lineTo(center.x - innerRadius, waterHeightY)
                    var x = center.x - innerRadius
                    while (x <= center.x + innerRadius) {
                        val relativeX = x - (center.x - innerRadius)
                        val y = waterHeightY + waveAmplitude * sin(relativeX * 0.035f + wavePhase + 1.5f)
                        lineTo(x, y)
                        x += 4f
                    }
                    lineTo(center.x + innerRadius, diameter)
                    close()
                }

                drawPath(
                    path = backWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x3388CEFF),
                            Color(0x221A3B4D)
                        )
                    )
                )

                // Front dynamic wave
                val frontWavePath = Path().apply {
                    moveTo(center.x - innerRadius, diameter)
                    lineTo(center.x - innerRadius, waterHeightY)
                    var x = center.x - innerRadius
                    while (x <= center.x + innerRadius) {
                        val relativeX = x - (center.x - innerRadius)
                        val y = waterHeightY + waveAmplitude * sin(relativeX * 0.035f + wavePhase)
                        lineTo(x, y)
                        x += 4f
                    }
                    lineTo(center.x + innerRadius, diameter)
                    close()
                }

                drawPath(
                    path = frontWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA88CEFF),
                            Color(0x992563EB),
                            Color(0xCC0D3B54)
                        ),
                        startY = waterHeightY - 10f,
                        endY = diameter
                    )
                )
            }

            // 1. Background Ring Track (Color #2F3033)
            drawArc(
                color = DarkSurfaceVariant,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Progress Arc (Color #88CEFF to #67E8F9)
            val sweep = (animatedProgress * 360f).coerceIn(0f, 360f)
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            HydroTeal,
                            HydroBlue,
                            HydroCyan,
                            HydroBlue
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "$percentDisplay%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${unitSystem.format(currentMl)} / ${unitSystem.format(goalMl)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            val remaining = (goalMl - currentMl).coerceAtLeast(0)
            if (remaining > 0) {
                Text(
                    text = "${unitSystem.format(remaining)} remaining",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = HydroBlue.copy(alpha = 0.85f)
                )
            } else {
                Text(
                    text = "Goal Complete! 🌟",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HydroTeal
                )
            }
        }
    }
}
