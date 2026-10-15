package com.example.model

enum class FlightMode(val title: String) {
    ORBIT("Орбита"),
    LINE("Прямая"),
    HOLD("Зависание")
}

data class AntennaState(
    val az: Float = 0f,      // degrees 0..360
    val el: Float = 15f,     // degrees -5..90
    val height: Float = 5f   // mast height in meters
)

data class DroneState(
    val dist: Float = 1200f,  // distance from GCS in meters
    val alt: Float = 200f,    // altitude in meters
    val az: Float = 30f,      // azimuth in degrees
    val angleRad: Float = 0.5f,
    val mode: FlightMode = FlightMode.ORBIT
)

data class TelemetryData(
    val antAz: Float = 0f,
    val antEl: Float = 15f,
    val targetAz: Float = 30f,
    val targetEl: Float = 9.5f,
    val azError: Float = 30f,
    val elError: Float = -5.5f,
    val totalAngularError: Float = 30.5f,
    val slantDistanceM: Float = 1200f,
    val droneAltitudeM: Float = 200f,
    val rssiPercent: Int = 0,
    val isRadioHorizonVisible: Boolean = true,
    val radioHorizonDistM: Float = 58000f
)

data class EarthCalcData(
    val groundDistM: Double = 2500.0,
    val objectHeightM: Double = 150.0,
    val antennaHeightM: Double = 10.0,
    val elevationAngleDeg: Double = 3.2,
    val slantDistanceM: Double = 2504.0,
    val horizonDistM: Double = 54800.0,
    val isVisible: Boolean = true
)
