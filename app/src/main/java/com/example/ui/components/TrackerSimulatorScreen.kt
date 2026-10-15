package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.model.FlightMode
import com.example.model.TelemetryData
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TacticalCardBg
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalDarkBg

@Composable
fun TrackerSimulatorScreen(
    antenna: AntennaState,
    drone: DroneState,
    telemetry: TelemetryData,
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
            .fillMaxSize()
            .background(TacticalDarkBg)
            .verticalScroll(rememberScrollState())
            .testTag("simulator_screen")
    ) {
        // Upper Viewport Container: 3D Viewport + 2D Radar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(TacticalCardBg)
                .border(1.dp, TacticalCardBorder)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: 3D Viewport
            Tactical3DViewport(
                antenna = antenna,
                drone = drone,
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Right: 2D Radar Panel
            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TacticalCardBg)
                    .border(1.dp, TacticalCardBorder, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "2D РАДАР АЗИМУТА",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = com.example.ui.theme.NeonCyan,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TacticalRadarView(
                        antenna = antenna,
                        drone = drone,
                        modifier = Modifier
                            .fillMaxHeight(0.95f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Middle: HUD Board (RSSI + 4 Telemetry Cards)
        HudBoard(
            telemetry = telemetry,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom: Tracker & Drone Controls
        TrackerControls(
            antenna = antenna,
            drone = drone,
            isAutoTrack = isAutoTrack,
            isSimRunning = isSimRunning,
            simSpeed = simSpeed,
            onAdjustAzimuth = onAdjustAzimuth,
            onSetAzimuth = onSetAzimuth,
            onAdjustElevation = onAdjustElevation,
            onSetElevation = onSetElevation,
            onSetFlightMode = onSetFlightMode,
            onAdjustDroneAlt = onAdjustDroneAlt,
            onToggleAutoTrack = onToggleAutoTrack,
            onLockOnTarget = onLockOnTarget,
            onToggleSimulation = onToggleSimulation,
            onSetSimSpeed = onSetSimSpeed,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
