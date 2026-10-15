package com.example

import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.model.FlightMode
import com.example.util.TrackerPhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPhysicsCalculations() {
        val ant = AntennaState(az = 0f, el = 15f, height = 5f)
        val drone = DroneState(dist = 1200f, alt = 200f, az = 30f, mode = FlightMode.ORBIT)
        val telem = TrackerPhysics.calculateTelemetry(ant, drone)

        // Slant distance must be close to hypotenuse of dist and alt
        assertTrue("Slant distance should be around 1200m", telem.slantDistanceM in 1100f..1300f)
        assertTrue("Target elevation should be positive", telem.targetEl > 0f)
        assertTrue("Azimuth error should be 30 deg", telem.azError in 29.5f..30.5f)
        assertTrue("RSSI should be in valid 0..100 range", telem.rssiPercent in 0..100)
    }

    @Test
    fun testPerfectLockGivesHighRssi() {
        val drone = DroneState(dist = 1000f, alt = 150f, az = 45f)
        val tempTelem = TrackerPhysics.calculateTelemetry(AntennaState(az = 0f, el = 0f, height = 5f), drone)

        // Point antenna directly at target
        val ant = AntennaState(az = tempTelem.targetAz, el = tempTelem.targetEl, height = 5f)
        val telemLocked = TrackerPhysics.calculateTelemetry(ant, drone)

        assertTrue("Direct lock should yield high RSSI (> 80%)", telemLocked.rssiPercent >= 80)
        assertTrue("Angular error should be nearly 0", telemLocked.totalAngularError < 0.5f)
    }

    @Test
    fun testEarthCurvatureCalculation() {
        val calc = TrackerPhysics.calculateEarthGeometry(groundDist = 2500.0, objHeight = 150.0, antHeight = 10.0)
        assertTrue("Slant distance > ground distance", calc.slantDistanceM >= 2500.0)
        assertTrue("Elevation angle should be positive", calc.elevationAngleDeg > 0.0)
        assertTrue("Should be visible at 2.5km", calc.isVisible)
    }
}
