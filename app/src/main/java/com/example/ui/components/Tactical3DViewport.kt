package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AntennaState
import com.example.model.DroneState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LinkLineYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TacticalCardBg
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderLight
import com.example.ui.theme.TacticalCardElevated
import com.example.ui.theme.TacticalDeepDark
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float)

@Composable
fun Tactical3DViewport(
    antenna: AntennaState,
    drone: DroneState,
    modifier: Modifier = Modifier
) {
    var cameraYaw by remember { mutableFloatStateOf(35f) }
    var cameraPitch by remember { mutableFloatStateOf(28f) }
    var cameraZoom by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = modifier
            .testTag("viewport_3d_container")
            .clip(RoundedCornerShape(8.dp))
            .background(TacticalDeepDark)
            .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        cameraYaw = (cameraYaw + dragAmount.x * 0.4f) % 360f
                        cameraPitch = (cameraPitch - dragAmount.y * 0.3f).coerceIn(5f, 85f)
                    }
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.65f
            val baseScale = (size.minDimension / 320f) * cameraZoom

            val yawRad = Math.toRadians(cameraYaw.toDouble())
            val pitchRad = Math.toRadians(cameraPitch.toDouble())

            val cosY = cos(yawRad).toFloat()
            val sinY = sin(yawRad).toFloat()
            val cosP = cos(pitchRad).toFloat()
            val sinP = sin(pitchRad).toFloat()

            // 3D perspective / isometric projection function
            fun project(p: Point3D): Offset {
                val rx = p.x * cosY - p.z * sinY
                val rz = p.x * sinY + p.z * cosY
                val ry = p.y * cosP - rz * sinP
                val depth = p.y * sinP + rz * cosP

                val perspective = 1.0f / (1.0f + depth * 0.0015f)
                val screenX = cx + rx * baseScale * perspective
                val screenY = cy - ry * baseScale * perspective
                return Offset(screenX, screenY)
            }

            // 1. Draw 3D Earth Ground Grid with neon HUD styling
            val gridSize = 140f
            val gridStep = 28f
            var gx = -gridSize
            while (gx <= gridSize) {
                val start = project(Point3D(gx, 0f, -gridSize))
                val end = project(Point3D(gx, 0f, gridSize))
                val isAxis = kotlin.math.abs(gx) < 1f
                drawLine(
                    color = if (isAxis) NeonCyan.copy(alpha = 0.45f) else Color(0xFF16233B),
                    start = start,
                    end = end,
                    strokeWidth = if (isAxis) 1.5f else 1f
                )
                gx += gridStep
            }

            var gz = -gridSize
            while (gz <= gridSize) {
                val start = project(Point3D(-gridSize, 0f, gz))
                val end = project(Point3D(gridSize, 0f, gz))
                val isAxis = kotlin.math.abs(gz) < 1f
                drawLine(
                    color = if (isAxis) NeonCyan.copy(alpha = 0.45f) else Color(0xFF16233B),
                    start = start,
                    end = end,
                    strokeWidth = if (isAxis) 1.5f else 1f
                )
                gz += gridStep
            }

            // Ground distance concentric circles (500m, 1000m, 2000m)
            val ringSteps = listOf(40f, 80f, 120f)
            ringSteps.forEach { r ->
                val ringPath = Path()
                val segments = 32
                for (i in 0..segments) {
                    val angle = (i.toFloat() / segments) * (2 * Math.PI)
                    val px = (r * cos(angle)).toFloat()
                    val pz = (r * sin(angle)).toFloat()
                    val pt = project(Point3D(px, 0f, pz))
                    if (i == 0) ringPath.moveTo(pt.x, pt.y) else ringPath.lineTo(pt.x, pt.y)
                }
                drawPath(ringPath, color = NeonCyan.copy(alpha = 0.2f), style = Stroke(width = 1f))
            }

            // 2. GCS Antenna Mast & Tracker Base
            val gcsBaseCenter = project(Point3D(0f, 0f, 0f))
            val gcsMastTop = project(Point3D(0f, 24f, 0f))

            val leg1 = project(Point3D(-8f, 0f, -8f))
            val leg2 = project(Point3D(8f, 0f, -8f))
            val leg3 = project(Point3D(8f, 0f, 8f))
            val leg4 = project(Point3D(-8f, 0f, 8f))

            drawLine(color = Color(0xFF94A3B8), start = leg1, end = gcsMastTop, strokeWidth = 2.5f)
            drawLine(color = Color(0xFF94A3B8), start = leg2, end = gcsMastTop, strokeWidth = 2.5f)
            drawLine(color = Color(0xFF94A3B8), start = leg3, end = gcsMastTop, strokeWidth = 2.5f)
            drawLine(color = Color(0xFF94A3B8), start = leg4, end = gcsMastTop, strokeWidth = 2.5f)

            // Red Tracker Rotator Head
            val headCenter = Point3D(0f, 26f, 0f)
            val headProj = project(headCenter)
            drawCircle(color = RedDanger, radius = 6f * baseScale, center = headProj)

            // 3. Antenna Direction & Radiation Cone (3D Beam)
            val antAzRad = Math.toRadians(antenna.az.toDouble())
            val antElRad = Math.toRadians(antenna.el.toDouble())
            val beamLength = 110f
            val beamRadius = 26f

            val bDirX = (cos(antElRad) * sin(antAzRad)).toFloat()
            val bDirY = sin(antElRad).toFloat()
            val bDirZ = (cos(antElRad) * cos(antAzRad)).toFloat()

            val beamApex = headCenter
            val beamCenterEnd = Point3D(
                beamApex.x + bDirX * beamLength,
                beamApex.y + bDirY * beamLength,
                beamApex.z + bDirZ * beamLength
            )

            // Draw Beam Cone outline ribs
            val numRibs = 8
            val upX = 0f; val upY = 1f; val upZ = 0f
            var perp1X = bDirY * upZ - bDirZ * upY
            var perp1Y = bDirZ * upX - bDirX * upZ
            var perp1Z = bDirX * upY - bDirY * upX
            val lenP1 = kotlin.math.sqrt(perp1X * perp1X + perp1Y * perp1Y + perp1Z * perp1Z)
            if (lenP1 > 0.001f) {
                perp1X /= lenP1; perp1Y /= lenP1; perp1Z /= lenP1
            } else {
                perp1X = 1f; perp1Y = 0f; perp1Z = 0f
            }
            val perp2X = bDirY * perp1Z - bDirZ * perp1Y
            val perp2Y = bDirZ * perp1X - bDirX * perp1Z
            val perp2Z = bDirX * perp1Y - bDirY * perp1X

            val coneRingPath = Path()
            for (i in 0..numRibs) {
                val theta = (i.toFloat() / numRibs) * (2 * Math.PI)
                val cCos = cos(theta).toFloat() * beamRadius
                val cSin = sin(theta).toFloat() * beamRadius

                val rimPt = Point3D(
                    beamCenterEnd.x + perp1X * cCos + perp2X * cSin,
                    beamCenterEnd.y + perp1Y * cCos + perp2Y * cSin,
                    beamCenterEnd.z + perp1Z * cCos + perp2Z * cSin
                )

                val projApex = project(beamApex)
                val projRim = project(rimPt)

                drawLine(
                    color = NeonEmerald.copy(alpha = 0.45f),
                    start = projApex,
                    end = projRim,
                    strokeWidth = 1.3f
                )

                if (i == 0) coneRingPath.moveTo(projRim.x, projRim.y) else coneRingPath.lineTo(projRim.x, projRim.y)
            }
            drawPath(
                path = coneRingPath,
                color = NeonEmerald.copy(alpha = 0.6f),
                style = Stroke(width = 1.8f)
            )

            // Antenna central boresight vector
            drawLine(
                color = NeonEmerald,
                start = project(beamApex),
                end = project(beamCenterEnd),
                strokeWidth = 2.2f
            )

            // 4. Drone Position in 3D Space
            val droneScaleFactor = 0.07f
            val dAzRad = Math.toRadians(drone.az.toDouble())
            val drone3dX = (drone.dist * droneScaleFactor * sin(dAzRad)).toFloat()
            val drone3dZ = (drone.dist * droneScaleFactor * cos(dAzRad)).toFloat()
            val drone3dY = (drone.alt * droneScaleFactor * 0.9f).coerceIn(12f, 90f)

            val dronePt = Point3D(drone3dX, drone3dY, drone3dZ)
            val droneShadowPt = Point3D(drone3dX, 0f, drone3dZ)

            val projDrone = project(dronePt)
            val projShadow = project(droneShadowPt)

            // Altitude drop line (dashed)
            drawLine(
                color = NeonCyan.copy(alpha = 0.5f),
                start = projShadow,
                end = projDrone,
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )

            // Ground shadow ellipse
            drawCircle(
                color = Color.Black.copy(alpha = 0.6f),
                radius = 7f * baseScale,
                center = projShadow
            )
            drawCircle(
                color = NeonCyan.copy(alpha = 0.4f),
                radius = 5f * baseScale,
                center = projShadow,
                style = Stroke(width = 1.2f)
            )

            // 5. Line of Sight (Radio Link) from Antenna to Drone
            drawLine(
                color = LinkLineYellow.copy(alpha = 0.9f),
                start = project(headCenter),
                end = projDrone,
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )

            // 6. Draw 3D FPV Drone Quadcopter
            val dArm = 8f
            val dP1 = project(Point3D(drone3dX - dArm, drone3dY, drone3dZ - dArm))
            val dP2 = project(Point3D(drone3dX + dArm, drone3dY, drone3dZ - dArm))
            val dP3 = project(Point3D(drone3dX + dArm, drone3dY, drone3dZ + dArm))
            val dP4 = project(Point3D(drone3dX - dArm, drone3dY, drone3dZ + dArm))

            drawLine(color = NeonCyan, start = dP1, end = dP3, strokeWidth = 2.5f)
            drawLine(color = NeonCyan, start = dP2, end = dP4, strokeWidth = 2.5f)

            listOf(dP1, dP2, dP3, dP4).forEach { rp ->
                drawCircle(color = NeonCyan.copy(alpha = 0.6f), radius = 4f * baseScale, center = rp)
            }
            drawCircle(color = Color.White, radius = 4f * baseScale, center = projDrone)
            drawCircle(color = NeonCyan, radius = 2.5f * baseScale, center = projDrone)

            // Labels overlay on 3D Canvas
            val canvasPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#00F0FF")
                textSize = 21f
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText("НСУ ТРЕКЕР", gcsBaseCenter.x - 45f, gcsBaseCenter.y + 20f, canvasPaint)

            val dronePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#FACC15")
                textSize = 21f
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
                isFakeBoldText = true
            }
            drawContext.canvas.nativeCanvas.drawText("FPV ${drone.alt.toInt()}м", projDrone.x + 12f, projDrone.y - 8f, dronePaint)
        }

        // Camera control buttons overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            ElevatedButton(
                onClick = {
                    cameraYaw = 35f
                    cameraPitch = 28f
                    cameraZoom = 1.0f
                },
                modifier = Modifier.testTag("reset_camera_button"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TacticalCardElevated.copy(alpha = 0.9f),
                    contentColor = NeonCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorderLight)
            ) {
                Text("3D ВИД", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            ElevatedButton(
                onClick = {
                    cameraYaw = 0f
                    cameraPitch = 85f
                },
                modifier = Modifier.padding(start = 4.dp).testTag("top_view_button"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = TacticalCardElevated.copy(alpha = 0.9f),
                    contentColor = NeonCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorderLight)
            ) {
                Text("СВЕРХУ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Gesture hint badge
        Text(
            text = "3D ВЬЮПОРТ: вращайте для обзора",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
