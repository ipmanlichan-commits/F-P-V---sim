package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EarthCalcData
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LinkLineYellow
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
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalDeepDark
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EarthCurvatureCalcScreen(
    data: EarthCalcData,
    onChangeGroundDist: (Double) -> Unit,
    onChangeObjHeight: (Double) -> Unit,
    onChangeAntHeight: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .testTag("earth_calc_screen")
    ) {
        // Big Info Display with deeper slate and clean border
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calc_info_board"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TacticalCardBg),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, TacticalCardBorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "СФЕРИЧЕСКАЯ ГЕОМЕТРИЯ ЗЕМЛИ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "УГОЛ МЕСТА: %.2f°", data.elevationAngleDeg),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                val slantKm = data.slantDistanceM / 1000.0
                Text(
                    text = String.format(Locale.US, "НАКЛОННАЯ Dн: %.3f км (%.0f м)", slantKm, data.slantDistanceM),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonEmerald
                )

                Spacer(modifier = Modifier.height(6.dp))
                val horizonKm = data.horizonDistM / 1000.0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (data.isVisible) NeonEmerald.copy(alpha = 0.12f) else RedDanger.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (data.isVisible) NeonEmerald.copy(alpha = 0.4f) else RedDanger.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (data.isVisible) {
                            String.format(Locale.US, "В ЗОНЕ РАДИОВИДИМОСТИ (ГОРИЗОНТ: %.1f км)", horizonKm)
                        } else {
                            String.format(Locale.US, "ЗА ГОРИЗОНТОМ (ПРЕДЕЛ: %.1f км)", horizonKm)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (data.isVisible) NeonEmerald else RedDanger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Curved Earth Visual Canvas (2D Profile)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TacticalDeepDark)
                .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val vRadius = size.height * 2.8f
                val earthCenterY = size.height + vRadius - 40f

                // Draw Earth Spherical Surface Arc
                val earthPath = Path().apply {
                    val steps = 40
                    for (i in 0..steps) {
                        val angle = Math.toRadians(-35.0 + (70.0 * i / steps))
                        val px = cx + (vRadius * sin(angle)).toFloat()
                        val py = earthCenterY - (vRadius * cos(angle)).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                }
                drawPath(earthPath, color = Color(0xFF0D9488), style = Stroke(width = 2.5f))

                // Antenna Base on Earth
                val antBaseY = earthCenterY - vRadius
                val antVisualH = (data.antennaHeightM * 0.8).coerceIn(12.0, 36.0).toFloat()
                val antTopY = antBaseY - antVisualH

                // Antenna Mast
                drawLine(
                    color = RedDanger,
                    start = Offset(cx, antBaseY),
                    end = Offset(cx, antTopY),
                    strokeWidth = 3.5f
                )
                drawCircle(color = RedDanger, radius = 4f, center = Offset(cx, antTopY))

                // Drone / Object on Curved Earth
                val thetaVis = Math.toRadians((data.groundDistM / 10000.0) * 26.0)
                val objVisualAlt = (data.objectHeightM * 0.12).coerceIn(14.0, 55.0).toFloat()
                val objRadius = vRadius + objVisualAlt

                val objX = cx + (objRadius * sin(thetaVis)).toFloat()
                val objY = earthCenterY - (objRadius * cos(thetaVis)).toFloat()

                val objGroundX = cx + (vRadius * sin(thetaVis)).toFloat()
                val objGroundY = earthCenterY - (vRadius * cos(thetaVis)).toFloat()

                // Object altitude line to ground
                drawLine(
                    color = NeonCyan.copy(alpha = 0.5f),
                    start = Offset(objGroundX, objGroundY),
                    end = Offset(objX, objY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )

                // Slant Line of sight (Dн)
                drawLine(
                    color = LinkLineYellow,
                    start = Offset(cx, antTopY),
                    end = Offset(objX, objY),
                    strokeWidth = 2.5f
                )

                // Drone point
                drawCircle(color = NeonCyan, radius = 6f, center = Offset(objX, objY))
                drawCircle(color = Color.White, radius = 2.5f, center = Offset(objX, objY))

                // Tangent Horizon Line from Antenna Top
                val tangentLen = size.width * 0.45f
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(cx, antTopY),
                    end = Offset(cx + tangentLen, antTopY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                )

                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#00F0FF")
                    textSize = 20f
                    typeface = android.graphics.Typeface.MONOSPACE
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                drawContext.canvas.nativeCanvas.drawText("АНТЕННА ${data.antennaHeightM.toInt()}м", cx - 70f, antBaseY + 22f, textPaint)
                drawContext.canvas.nativeCanvas.drawText("ОБЪЕКТ ${data.objectHeightM.toInt()}м", objX - 40f, objY - 12f, textPaint)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Ground distance d (м)
        CalcControlGroup(
            title = "ДАЛЬНОСТЬ ПО ЗЕМЛЕ (М)",
            valueDisplay = String.format(Locale.US, "%.0f", data.groundDistM),
            onMinus100 = { onChangeGroundDist(-100.0) },
            onMinus50 = { onChangeGroundDist(-50.0) },
            onMinus10 = { onChangeGroundDist(-10.0) },
            onPlus10 = { onChangeGroundDist(10.0) },
            onPlus50 = { onChangeGroundDist(50.0) },
            onPlus100 = { onChangeGroundDist(100.0) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Object height h_obj (м)
        CalcControlGroup(
            title = "ВЫСОТА АППАРАТА (М)",
            valueDisplay = String.format(Locale.US, "%.0f", data.objectHeightM),
            onMinus100 = { onChangeObjHeight(-100.0) },
            onMinus50 = { onChangeObjHeight(-50.0) },
            onMinus10 = { onChangeObjHeight(-10.0) },
            onPlus10 = { onChangeObjHeight(10.0) },
            onPlus50 = { onChangeObjHeight(50.0) },
            onPlus100 = { onChangeObjHeight(100.0) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Antenna height h_ant (м)
        CalcControlGroup(
            title = "ВЫСОТА АНТЕННЫ (М)",
            valueDisplay = String.format(Locale.US, "%.0f", data.antennaHeightM),
            onMinus100 = { onChangeAntHeight(-10.0) },
            onMinus50 = { onChangeAntHeight(-5.0) },
            onMinus10 = { onChangeAntHeight(-1.0) },
            onPlus10 = { onChangeAntHeight(1.0) },
            onPlus50 = { onChangeAntHeight(5.0) },
            onPlus100 = { onChangeAntHeight(10.0) },
            labels = listOf("-10", "-5", "-1", "+1", "+5", "+10")
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CalcControlGroup(
    title: String,
    valueDisplay: String,
    onMinus100: () -> Unit,
    onMinus50: () -> Unit,
    onMinus10: () -> Unit,
    onPlus10: () -> Unit,
    onPlus50: () -> Unit,
    onPlus100: () -> Unit,
    labels: List<String> = listOf("-100", "-50", "-10", "+10", "+50", "+100")
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TacticalCardBg)
            .border(1.dp, TacticalCardBorder, RoundedCornerShape(6.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallStepBtn(text = labels[0], onClick = onMinus100, modifier = Modifier.weight(1f))
                SmallStepBtn(text = labels[1], onClick = onMinus50, modifier = Modifier.weight(1f))
                SmallStepBtn(text = labels[2], onClick = onMinus10, modifier = Modifier.weight(1f))
            }

            Text(
                text = valueDisplay,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = SlateTextPrimary,
                modifier = Modifier
                    .width(76.dp)
                    .padding(horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallStepBtn(text = labels[3], onClick = onPlus10, modifier = Modifier.weight(1f))
                SmallStepBtn(text = labels[4], onClick = onPlus50, modifier = Modifier.weight(1f))
                SmallStepBtn(text = labels[5], onClick = onPlus100, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SmallStepBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TacticalCardElevated,
            contentColor = Color.White
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
