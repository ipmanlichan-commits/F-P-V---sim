package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadarGreen
import com.example.ui.theme.RadarGreenGlow
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderLight
import com.example.ui.theme.TacticalDeepDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalRadarView(
    antenna: AntennaState,
    drone: DroneState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    Box(
        modifier = modifier
            .testTag("radar_viewport")
            .clip(CircleShape)
            .background(TacticalDeepDark)
            .border(1.5.dp, NeonCyan.copy(alpha = 0.85f), CircleShape)
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 8f

            // Radar background concentric rings
            val ringCount = 3
            for (i in 1..ringCount) {
                val r = radius * (i.toFloat() / ringCount)
                drawCircle(
                    color = TacticalCardBorderLight.copy(alpha = 0.6f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }

            // Crosshairs with clean dashed effect
            drawLine(
                color = TacticalCardBorderLight.copy(alpha = 0.8f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1f
            )
            drawLine(
                color = TacticalCardBorderLight.copy(alpha = 0.8f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1f
            )

            // Compass Cardinal Points (N, E, S, W)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#00F0FF")
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
                isFakeBoldText = true
            }

            drawContext.canvas.nativeCanvas.drawText("С 0°", center.x, center.y - radius + 20f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("В 90°", center.x + radius - 26f, center.y + 7f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("Ю 180°", center.x, center.y + radius - 8f, textPaint)
            drawContext.canvas.nativeCanvas.drawText("З 270°", center.x - radius + 26f, center.y + 7f, textPaint)

            // Dynamic Sweeping Line
            val sweepRad = Math.toRadians((sweepAngle - 90f).toDouble())
            val sweepEnd = Offset(
                x = center.x + radius * cos(sweepRad).toFloat(),
                y = center.y + radius * sin(sweepRad).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, RadarGreenGlow.copy(alpha = 0.65f)),
                    start = center,
                    end = sweepEnd
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.5f
            )

            // Antenna Beam Sector (Width ~30 deg, green cone)
            val antAzRad = Math.toRadians((antenna.az - 90f).toDouble())
            val halfBeam = Math.toRadians(15.0)
            val beamPath = Path().apply {
                moveTo(center.x, center.y)
                val steps = 18
                for (step in 0..steps) {
                    val angle = (antAzRad - halfBeam) + (2 * halfBeam * step / steps)
                    val px = center.x + radius * cos(angle).toFloat()
                    val py = center.y + radius * sin(angle).toFloat()
                    lineTo(px, py)
                }
                close()
            }
            drawPath(
                path = beamPath,
                color = NeonEmerald.copy(alpha = 0.22f)
            )
            drawPath(
                path = beamPath,
                color = NeonEmerald.copy(alpha = 0.7f),
                style = Stroke(width = 1.5f)
            )

            // Antenna pointing direction needle with glowing tip
            val antNeedleEnd = Offset(
                x = center.x + radius * cos(antAzRad).toFloat(),
                y = center.y + radius * sin(antAzRad).toFloat()
            )
            drawLine(
                color = NeonEmerald,
                start = center,
                end = antNeedleEnd,
                strokeWidth = 2.5f
            )

            // Drone position on Radar (scale distance: max ~2800m)
            val droneNormDist = (drone.dist / 2800f).coerceIn(0.15f, 0.95f)
            val droneDistPx = radius * droneNormDist
            val droneAzRad = Math.toRadians((drone.az - 90f).toDouble())
            val dronePos = Offset(
                x = center.x + droneDistPx * cos(droneAzRad).toFloat(),
                y = center.y + droneDistPx * sin(droneAzRad).toFloat()
            )

            // Dotted trail line from antenna to drone
            drawLine(
                color = NeonCyan.copy(alpha = 0.4f),
                start = center,
                end = dronePos,
                strokeWidth = 1f
            )

            // Drone blip with pulsating halo
            drawCircle(
                color = NeonCyan.copy(alpha = 0.25f),
                radius = 13f,
                center = dronePos
            )
            drawCircle(
                color = NeonCyan,
                radius = 5.5f,
                center = dronePos
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = dronePos
            )

            // Center Ground Control Station (GCS) icon
            drawCircle(
                color = Color(0xFFFF3366),
                radius = 5f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 2f,
                center = center
            )
        }
    }
}
