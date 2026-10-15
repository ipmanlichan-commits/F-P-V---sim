package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.model.EarthCalcData
import com.example.model.FlightMode
import com.example.model.TelemetryData
import com.example.util.TrackerPhysics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class TrackerUiState(
    val antenna: AntennaState = AntennaState(az = 0f, el = 15f, height = 5f),
    val drone: DroneState = DroneState(dist = 1200f, alt = 200f, az = 30f, angleRad = 0.5f, mode = FlightMode.ORBIT),
    val telemetry: TelemetryData = TelemetryData(),
    val earthCalc: EarthCalcData = EarthCalcData(),
    val isAutoTrack: Boolean = false,
    val isSimRunning: Boolean = true,
    val simSpeed: Float = 1.0f,
    val activeTab: Int = 0 // 0: Симулятор, 1: 3D Калькулятор, 2: Инфо/Справка
)

class TrackerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    init {
        updateTelemetry()
        updateEarthCalc()
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(33) // ~30 fps update
                if (_uiState.value.isSimRunning) {
                    stepSimulation()
                }
            }
        }
    }

    private fun stepSimulation() {
        _uiState.update { state ->
            val drone = state.drone
            val speed = state.simSpeed
            var newAngleRad = drone.angleRad
            var newAz = drone.az
            var newDist = drone.dist

            when (drone.mode) {
                FlightMode.ORBIT -> {
                    newAngleRad += (0.006f * speed)
                    newAz = (((Math.toDegrees(newAngleRad.toDouble()).toFloat()) % 360f) + 360f) % 360f
                    newDist = 1200f + sin(newAngleRad * 2f) * 400f
                }
                FlightMode.LINE -> {
                    newDist += (3f * speed)
                    if (newDist > 3000f) {
                        newDist = 300f
                    }
                }
                FlightMode.HOLD -> {
                    // Hovering in place
                }
            }

            val updatedDrone = drone.copy(
                angleRad = newAngleRad,
                az = newAz,
                dist = newDist
            )

            // If auto-track is on, smoothly rotate antenna towards target
            var currentAnt = state.antenna
            if (state.isAutoTrack) {
                val tempTelem = TrackerPhysics.calculateTelemetry(currentAnt, updatedDrone)
                val targetAz = tempTelem.targetAz
                val targetEl = tempTelem.targetEl

                // Smooth interpolation for servos
                val azDiff = ((targetAz - currentAnt.az + 540f) % 360f) - 180f
                val elDiff = targetEl - currentAnt.el

                val stepAz = (azDiff * 0.12f).coerceIn(-15f, 15f)
                val stepEl = (elDiff * 0.12f).coerceIn(-10f, 10f)

                val smoothedAz = (currentAnt.az + stepAz + 360f) % 360f
                val smoothedEl = (currentAnt.el + stepEl).coerceIn(-5f, 90f)

                currentAnt = currentAnt.copy(az = smoothedAz, el = smoothedEl)
            }

            val telem = TrackerPhysics.calculateTelemetry(currentAnt, updatedDrone)

            state.copy(
                drone = updatedDrone,
                antenna = currentAnt,
                telemetry = telem
            )
        }
    }

    fun adjustAntennaAzimuth(delta: Float) {
        _uiState.update { state ->
            val newAz = (state.antenna.az + delta + 360f) % 360f
            val newAnt = state.antenna.copy(az = newAz)
            state.copy(
                antenna = newAnt,
                isAutoTrack = false,
                telemetry = TrackerPhysics.calculateTelemetry(newAnt, state.drone)
            )
        }
    }

    fun setAntennaAzimuth(az: Float) {
        _uiState.update { state ->
            val normalizedAz = (az % 360f + 360f) % 360f
            val newAnt = state.antenna.copy(az = normalizedAz)
            state.copy(
                antenna = newAnt,
                isAutoTrack = false,
                telemetry = TrackerPhysics.calculateTelemetry(newAnt, state.drone)
            )
        }
    }

    fun adjustAntennaElevation(delta: Float) {
        _uiState.update { state ->
            val newEl = (state.antenna.el + delta).coerceIn(-5f, 90f)
            val newAnt = state.antenna.copy(el = newEl)
            state.copy(
                antenna = newAnt,
                isAutoTrack = false,
                telemetry = TrackerPhysics.calculateTelemetry(newAnt, state.drone)
            )
        }
    }

    fun setAntennaElevation(el: Float) {
        _uiState.update { state ->
            val clampedEl = el.coerceIn(-5f, 90f)
            val newAnt = state.antenna.copy(el = clampedEl)
            state.copy(
                antenna = newAnt,
                isAutoTrack = false,
                telemetry = TrackerPhysics.calculateTelemetry(newAnt, state.drone)
            )
        }
    }

    fun setFlightMode(mode: FlightMode) {
        _uiState.update { state ->
            val newDrone = state.drone.copy(mode = mode)
            state.copy(
                drone = newDrone,
                telemetry = TrackerPhysics.calculateTelemetry(state.antenna, newDrone)
            )
        }
    }

    fun adjustDroneAltitude(delta: Float) {
        _uiState.update { state ->
            val newAlt = (state.drone.alt + delta).coerceAtLeast(10f)
            val newDrone = state.drone.copy(alt = newAlt)
            state.copy(
                drone = newDrone,
                telemetry = TrackerPhysics.calculateTelemetry(state.antenna, newDrone)
            )
        }
    }

    fun toggleAutoTrack() {
        _uiState.update { it.copy(isAutoTrack = !it.isAutoTrack) }
    }

    fun lockOnTarget() {
        _uiState.update { state ->
            val telem = TrackerPhysics.calculateTelemetry(state.antenna, state.drone)
            val newAnt = state.antenna.copy(
                az = telem.targetAz,
                el = telem.targetEl.coerceIn(-5f, 90f)
            )
            state.copy(
                antenna = newAnt,
                telemetry = TrackerPhysics.calculateTelemetry(newAnt, state.drone)
            )
        }
    }

    fun toggleSimulation() {
        _uiState.update { it.copy(isSimRunning = !it.isSimRunning) }
    }

    fun setSimSpeed(speed: Float) {
        _uiState.update { it.copy(simSpeed = speed) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // 3D Calculator actions
    fun changeCalcGroundDist(delta: Double) {
        _uiState.update { state ->
            val current = state.earthCalc
            val newDist = (current.groundDistM + delta).coerceAtLeast(0.0)
            val newCalc = TrackerPhysics.calculateEarthGeometry(newDist, current.objectHeightM, current.antennaHeightM)
            state.copy(earthCalc = newCalc)
        }
    }

    fun changeCalcObjHeight(delta: Double) {
        _uiState.update { state ->
            val current = state.earthCalc
            val newHeight = (current.objectHeightM + delta).coerceAtLeast(0.0)
            val newCalc = TrackerPhysics.calculateEarthGeometry(current.groundDistM, newHeight, current.antennaHeightM)
            state.copy(earthCalc = newCalc)
        }
    }

    fun changeCalcAntHeight(delta: Double) {
        _uiState.update { state ->
            val current = state.earthCalc
            val newHeight = (current.antennaHeightM + delta).coerceAtLeast(0.5)
            val newCalc = TrackerPhysics.calculateEarthGeometry(current.groundDistM, current.objectHeightM, newHeight)
            state.copy(earthCalc = newCalc)
        }
    }

    private fun updateTelemetry() {
        _uiState.update { state ->
            state.copy(telemetry = TrackerPhysics.calculateTelemetry(state.antenna, state.drone))
        }
    }

    private fun updateEarthCalc() {
        _uiState.update { state ->
            val c = state.earthCalc
            state.copy(earthCalc = TrackerPhysics.calculateEarthGeometry(c.groundDistM, c.objectHeightM, c.antennaHeightM))
        }
    }
}
