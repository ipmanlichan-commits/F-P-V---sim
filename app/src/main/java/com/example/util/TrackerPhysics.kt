package com.example.util

import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.model.EarthCalcData
import com.example.model.TelemetryData
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object TrackerPhysics {
    const val R_EARTH = 6378000.0 // Earth radius in meters

    fun calculateTelemetry(ant: AntennaState, drone: DroneState): TelemetryData {
        val rAnt = R_EARTH + ant.height
        val rObj = R_EARTH + drone.alt
        val theta = drone.dist / R_EARTH

        val dh = sqrt(rAnt.pow(2) + rObj.pow(2) - 2.0 * rAnt * rObj * cos(theta))

        val targetEl = if (dh > 0.0) {
            val sinBeta = (rObj.pow(2) - rAnt.pow(2) - dh.pow(2)) / (2.0 * rAnt * dh)
            val clampedSinBeta = max(-1.0, min(1.0, sinBeta))
            asin(clampedSinBeta) * (180.0 / Math.PI)
        } else {
            0.0
        }.toFloat()

        val targetAz = drone.az

        // Azimuth misalignment in range -180..180
        val azError = ((drone.az - ant.az + 540.0) % 360.0 - 180.0).toFloat()
        val elError = targetEl - ant.el

        // Spatial 3D misalignment angle (in radians)
        val aAzRad = Math.toRadians(ant.az.toDouble())
        val aElRad = Math.toRadians(ant.el.toDouble())
        val tAzRad = Math.toRadians(targetAz.toDouble())
        val tElRad = Math.toRadians(targetEl.toDouble())

        val vAntX = cos(aElRad) * sin(aAzRad)
        val vAntY = sin(aElRad)
        val vAntZ = cos(aElRad) * cos(aAzRad)

        val vTarX = cos(tElRad) * sin(tAzRad)
        val vTarY = sin(tElRad)
        val vTarZ = cos(tElRad) * cos(tAzRad)

        val dot = max(-1.0, min(1.0, vAntX * vTarX + vAntY * vTarY + vAntZ * vTarZ))
        val totalErrDeg = (acos(dot) * (180.0 / Math.PI)).toFloat()

        // Antenna radiation pattern beam calculation (~30 degree cone)
        val beamFactor = max(0.0, cos(Math.toRadians(totalErrDeg.toDouble() * 2.5)))
        val distFactor = max(0.0, 1.0 - (dh / 30000.0))
        val rssi = (beamFactor.pow(3) * distFactor * 100.0).roundToInt().coerceIn(0, 100)

        // Radio horizon
        val horizonDist = sqrt(2.0 * R_EARTH * ant.height) + sqrt(2.0 * R_EARTH * drone.alt)
        val isHorizonVisible = dh <= horizonDist

        return TelemetryData(
            antAz = ant.az,
            antEl = ant.el,
            targetAz = targetAz,
            targetEl = targetEl,
            azError = azError,
            elError = elError,
            totalAngularError = totalErrDeg,
            slantDistanceM = dh.toFloat(),
            droneAltitudeM = drone.alt,
            rssiPercent = rssi,
            isRadioHorizonVisible = isHorizonVisible,
            radioHorizonDistM = horizonDist.toFloat()
        )
    }

    fun calculateEarthGeometry(groundDist: Double, objHeight: Double, antHeight: Double): EarthCalcData {
        val rAnt = R_EARTH + antHeight
        val rObj = R_EARTH + objHeight
        val theta = groundDist / R_EARTH

        val dh = sqrt(rAnt.pow(2) + rObj.pow(2) - 2.0 * rAnt * rObj * cos(theta))

        val angleDeg = if (groundDist == 0.0 && antHeight == objHeight) {
            0.0
        } else if (dh > 0.0) {
            val sinBeta = (rObj.pow(2) - rAnt.pow(2) - dh.pow(2)) / (2.0 * rAnt * dh)
            val clamped = max(-1.0, min(1.0, sinBeta))
            asin(clamped) * (180.0 / Math.PI)
        } else {
            0.0
        }

        val horizonDist = sqrt(2.0 * R_EARTH * antHeight) + sqrt(2.0 * R_EARTH * objHeight)
        val isVisible = dh <= horizonDist

        return EarthCalcData(
            groundDistM = groundDist,
            objectHeightM = objHeight,
            antennaHeightM = antHeight,
            elevationAngleDeg = angleDeg,
            slantDistanceM = dh,
            horizonDistM = horizonDist,
            isVisible = isVisible
        )
    }
}
