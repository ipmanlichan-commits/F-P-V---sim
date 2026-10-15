package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EarthCurvatureCalcScreen
import com.example.ui.components.TrackerReferenceScreen
import com.example.ui.components.TrackerSimulatorScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSignal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TacticalCardBg
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalDarkBg
import com.example.viewmodel.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "StatusBlink")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeaconAlpha"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalDarkBg),
        topBar = {
            Column(modifier = Modifier.background(TacticalCardBg)) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .alpha(beaconAlpha)
                                    .background(com.example.ui.theme.NeonEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "НСУ FPV // ТРЕКЕР АНТЕННЫ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = SlateTextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "СИМУЛЯТОР ОПЕРАТОРА И 3D КАЛЬКУЛЯТОР",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = com.example.ui.theme.NeonCyan,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TacticalCardBg
                    )
                )

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = state.activeTab,
                    containerColor = TacticalCardBg,
                    contentColor = com.example.ui.theme.NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[state.activeTab]),
                            color = com.example.ui.theme.NeonCyan,
                            height = 2.5.dp
                        )
                    },
                    divider = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(TacticalCardBorder)
                        )
                    }
                ) {
                    Tab(
                        selected = state.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        modifier = Modifier.testTag("tab_simulator"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = "Симулятор",
                                    tint = if (state.activeTab == 0) com.example.ui.theme.NeonCyan else SlateTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Симулятор",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.activeTab == 0) com.example.ui.theme.NeonCyan else SlateTextSecondary
                                )
                            }
                        }
                    )
                    Tab(
                        selected = state.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        modifier = Modifier.testTag("tab_calculator"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Калькулятор",
                                    tint = if (state.activeTab == 1) com.example.ui.theme.NeonCyan else SlateTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3D Калькулятор",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.activeTab == 1) com.example.ui.theme.NeonCyan else SlateTextSecondary
                                )
                            }
                        }
                    )
                    Tab(
                        selected = state.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        modifier = Modifier.testTag("tab_reference"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Теория",
                                    tint = if (state.activeTab == 2) com.example.ui.theme.NeonCyan else SlateTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Теория",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (state.activeTab == 2) com.example.ui.theme.NeonCyan else SlateTextSecondary
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TacticalDarkBg)
        ) {
            when (state.activeTab) {
                0 -> TrackerSimulatorScreen(
                    antenna = state.antenna,
                    drone = state.drone,
                    telemetry = state.telemetry,
                    isAutoTrack = state.isAutoTrack,
                    isSimRunning = state.isSimRunning,
                    simSpeed = state.simSpeed,
                    onAdjustAzimuth = viewModel::adjustAntennaAzimuth,
                    onSetAzimuth = viewModel::setAntennaAzimuth,
                    onAdjustElevation = viewModel::adjustAntennaElevation,
                    onSetElevation = viewModel::setAntennaElevation,
                    onSetFlightMode = viewModel::setFlightMode,
                    onAdjustDroneAlt = viewModel::adjustDroneAltitude,
                    onToggleAutoTrack = viewModel::toggleAutoTrack,
                    onLockOnTarget = viewModel::lockOnTarget,
                    onToggleSimulation = viewModel::toggleSimulation,
                    onSetSimSpeed = viewModel::setSimSpeed
                )
                1 -> EarthCurvatureCalcScreen(
                    data = state.earthCalc,
                    onChangeGroundDist = viewModel::changeCalcGroundDist,
                    onChangeObjHeight = viewModel::changeCalcObjHeight,
                    onChangeAntHeight = viewModel::changeCalcAntHeight
                )
                2 -> TrackerReferenceScreen()
            }
        }
    }
}
