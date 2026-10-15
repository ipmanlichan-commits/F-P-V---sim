package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.model.FlightMode
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
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
fun TrackerControls(
    antenna: AntennaState,
    drone: DroneState,
    isAutoTrack: Boolean,
    isSimRunning: Boolean,
    simSpeed: Float,
    onAdjustAzimuth: (Float) -> Unit,
    onSetAzimuth: (Float) -> Unit,
    onAdjustElevation: (Float) -> Unit,
    onSetElevation: (Float) -> Unit,
    onSetFlightMode: (FlightMode) -> Unit,
    onAdjustDroneAlt: (Float) -> Unit,
    onToggleAutoTrack: () -> Unit,
    onLockOnTarget: () -> Unit,
    onToggleSimulation: () -> Unit,
    onSetSimSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag("controls_container")
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Tracker Quick Actions (Auto-track & Lock)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleAutoTrack,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("auto_track_toggle_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAutoTrack) NeonEmerald else TacticalCardElevated,
                    contentColor = if (isAutoTrack) TacticalDeepDark else NeonCyan
                ),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAutoTrack) Color(0xFF00FF9D) else TacticalCardBorderLight
                )
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Автосопровождение",
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = if (isAutoTrack) "АВТОТРЕКЕР: ВКЛ" else "АВТОТРЕКЕР: ВЫКЛ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Button(
                onClick = onLockOnTarget,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("lock_target_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0369A1),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Захват цели",
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "НАВЕСТИ НА ЦЕЛЬ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 1: Antenna Azimuth & Elevation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group 1: Azimuth
            ControlGroupCard(
                title = "АЗИМУТ АНТЕННЫ",
                currentValue = String.format(Locale.US, "%.0f°", antenna.az),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StepButton(text = "-10°", onClick = { onAdjustAzimuth(-10f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "-2°", onClick = { onAdjustAzimuth(-2f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+2°", onClick = { onAdjustAzimuth(2f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+10°", onClick = { onAdjustAzimuth(10f) }, modifier = Modifier.weight(1f))
                }

                Slider(
                    value = antenna.az,
                    onValueChange = { onSetAzimuth(it) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            // Group 2: Elevation
            ControlGroupCard(
                title = "УГОЛ МЕСТА",
                currentValue = String.format(Locale.US, "%.0f°", antenna.el),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StepButton(text = "-5°", onClick = { onAdjustElevation(-5f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "-1°", onClick = { onAdjustElevation(-1f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+1°", onClick = { onAdjustElevation(1f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+5°", onClick = { onAdjustElevation(5f) }, modifier = Modifier.weight(1f))
                }

                Slider(
                    value = antenna.el,
                    onValueChange = { onSetElevation(it) },
                    valueRange = -5f..90f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Flight Mode & Drone Altitude
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Flight Mode
            ControlGroupCard(
                title = "РЕЖИМ ПОЛЕТА ДРОНА",
                currentValue = drone.mode.title,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FlightModeButton(
                        text = "Орбита",
                        isSelected = drone.mode == FlightMode.ORBIT,
                        onClick = { onSetFlightMode(FlightMode.ORBIT) },
                        modifier = Modifier.weight(1f)
                    )
                    FlightModeButton(
                        text = "Прямая",
                        isSelected = drone.mode == FlightMode.LINE,
                        onClick = { onSetFlightMode(FlightMode.LINE) },
                        modifier = Modifier.weight(1f)
                    )
                    FlightModeButton(
                        text = "Завис.",
                        isSelected = drone.mode == FlightMode.HOLD,
                        onClick = { onSetFlightMode(FlightMode.HOLD) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Drone Altitude
            ControlGroupCard(
                title = "ВЫСОТА ДРОНА",
                currentValue = String.format(Locale.US, "%.0f м", drone.alt),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StepButton(text = "-50м", onClick = { onAdjustDroneAlt(-50f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "-10м", onClick = { onAdjustDroneAlt(-10f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+10м", onClick = { onAdjustDroneAlt(10f) }, modifier = Modifier.weight(1f))
                    StepButton(text = "+50м", onClick = { onAdjustDroneAlt(50f) }, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Simulation Flow bar (Play/Pause & Speed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalCardElevated)
                .border(1.dp, TacticalCardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onToggleSimulation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSimRunning) Color(0xFF1E293B) else NeonEmerald,
                        contentColor = if (isSimRunning) SlateTextPrimary else TacticalDeepDark
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorderLight),
                    modifier = Modifier.height(34.dp).testTag("sim_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isSimRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Пауза/Пуск",
                        tint = if (isSimRunning) NeonCyan else TacticalDeepDark
                    )
                    Text(
                        text = if (isSimRunning) "ПАУЗА" else "СТАРТ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "СКОРОСТЬ:",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SlateTextMuted
                )
                listOf(0.5f, 1.0f, 2.0f, 3.0f).forEach { speed ->
                    val isSel = simSpeed == speed
                    Button(
                        onClick = { onSetSimSpeed(speed) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) NeonCyan else Color(0xFF0F172A),
                            contentColor = if (isSel) TacticalDeepDark else SlateTextSecondary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) NeonCyan else TacticalCardBorder
                        ),
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlGroupCard(
    title: String,
    currentValue: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TacticalCardBg)
            .border(1.dp, TacticalCardBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            Text(
                text = currentValue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = SlateTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun StepButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TacticalCardElevated,
            contentColor = SlateTextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorderLight),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun FlightModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) NeonEmerald else TacticalCardElevated,
            contentColor = if (isSelected) TacticalDeepDark else SlateTextPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF00FF9D) else TacticalCardBorderLight
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
