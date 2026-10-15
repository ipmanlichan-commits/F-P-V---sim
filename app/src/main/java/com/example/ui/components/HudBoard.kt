package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryData
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSignal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TacticalCardBg
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderLight
import com.example.ui.theme.TacticalCardElevated
import com.example.ui.theme.TacticalDeepDark
import java.util.Locale

@Composable
fun HudBoard(
    telemetry: TelemetryData,
    modifier: Modifier = Modifier
) {
    val rssi = telemetry.rssiPercent
    val rssiProgress by animateFloatAsState(
        targetValue = rssi / 100f,
        animationSpec = tween(durationMillis = 150),
        label = "RssiProgress"
    )

    val signalColor by animateColorAsState(
        targetValue = when {
            rssi > 60 -> NeonEmerald
            rssi > 20 -> AmberWarning
            else -> RedDanger
        },
        label = "SignalColor"
    )

    // Calculate approximate RF dBm
    val estimatedDbm = if (rssi > 0) {
        -105 + (rssi * 0.65).toInt()
    } else {
        -110
    }

    val signalStatusText = when {
        rssi > 75 -> "LOCK // EXCELLENT"
        rssi > 45 -> "LOCK // NORMAL"
        rssi > 20 -> "MARGINAL // WEAK"
        rssi > 0 -> "CRITICAL RF"
        else -> "NO CARRIER"
    }

    Column(
        modifier = modifier
            .testTag("hud_board")
            .clip(RoundedCornerShape(8.dp))
            .background(TacticalCardBg)
            .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // RSSI Level Box with refined HUD styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalDeepDark)
                .border(1.dp, TacticalCardBorderLight, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(signalColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "УРОВЕНЬ СИГНАЛА RSSI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$estimatedDbm dBm",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateTextMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(signalColor.copy(alpha = 0.15f))
                                .border(1.dp, signalColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = signalStatusText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = signalColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$rssi%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = signalColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(7.dp))

                // RSSI Bar with ticks & glow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF070D1C))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
                ) {
                    // Filled progress
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(rssiProgress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        signalColor.copy(alpha = 0.7f),
                                        signalColor
                                    )
                                )
                            )
                    )

                    // Overlay HUD tick markers
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val numTicks = 10
                        for (i in 1 until numTicks) {
                            val x = size.width * (i.toFloat() / numTicks)
                            drawLine(
                                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Telemetry 4-Card Grid with improved contrast
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card 1: Azimuth
            val azSign = if (telemetry.azError >= 0) "+" else ""
            TelemetryCard(
                title = "АЗИМУТ АНТЕННЫ",
                value = String.format(Locale.US, "%.1f°", telemetry.antAz),
                errorText = String.format(Locale.US, "Δ %s%.1f°", azSign, telemetry.azError),
                errorColor = if (kotlin.math.abs(telemetry.azError) < 15f) NeonEmerald else AmberWarning,
                targetText = String.format(Locale.US, "ЦЕЛЬ %.1f°", telemetry.targetAz),
                modifier = Modifier.weight(1f)
            )

            // Card 2: Elevation
            val elSign = if (telemetry.elError >= 0) "+" else ""
            TelemetryCard(
                title = "УГОЛ МЕСТА",
                value = String.format(Locale.US, "%.1f°", telemetry.antEl),
                errorText = String.format(Locale.US, "Δ %s%.1f°", elSign, telemetry.elError),
                errorColor = if (kotlin.math.abs(telemetry.elError) < 10f) NeonEmerald else AmberWarning,
                targetText = String.format(Locale.US, "ЦЕЛЬ %.1f°", telemetry.targetEl),
                modifier = Modifier.weight(1f)
            )

            // Card 3: Slant Range
            val distText = if (telemetry.slantDistanceM >= 1000f) {
                String.format(Locale.US, "%.2f км", telemetry.slantDistanceM / 1000f)
            } else {
                String.format(Locale.US, "%.0f м", telemetry.slantDistanceM)
            }
            TelemetryCard(
                title = "ДАЛЬНОСТЬ Dн",
                value = distText,
                errorText = if (telemetry.isRadioHorizonVisible) "В ВИДИМОСТИ" else "ЗА ГОРИЗОНТОМ",
                errorColor = if (telemetry.isRadioHorizonVisible) NeonEmerald else RedDanger,
                targetText = String.format(Locale.US, "ГОРИЗ %.0fкм", telemetry.radioHorizonDistM / 1000f),
                modifier = Modifier.weight(1f)
            )

            // Card 4: Drone Altitude
            TelemetryCard(
                title = "ВЫСОТА ДРОНА",
                value = String.format(Locale.US, "%.0f м", telemetry.droneAltitudeM),
                errorText = String.format(Locale.US, "ПРОМАХ %.1f°", telemetry.totalAngularError),
                errorColor = if (telemetry.totalAngularError < 15f) NeonEmerald else AmberWarning,
                targetText = "FPV СТАТУС",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryCard(
    title: String,
    value: String,
    errorText: String,
    errorColor: Color,
    targetText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TacticalCardElevated)
            .border(1.dp, TacticalCardBorderLight, RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 8.sp,
            color = SlateTextSecondary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.4.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(errorColor.copy(alpha = 0.12f))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = errorText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = errorColor,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = targetText,
            fontSize = 7.5.sp,
            color = SlateTextMuted,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
