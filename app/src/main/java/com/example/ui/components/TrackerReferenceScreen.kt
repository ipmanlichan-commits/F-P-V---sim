package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.LinkLineYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TacticalCardBg
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderLight
import com.example.ui.theme.TacticalCardElevated
import com.example.ui.theme.TacticalDarkBg
import com.example.ui.theme.TacticalDeepDark

@Composable
fun TrackerReferenceScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("tracker_reference_screen")
    ) {
        Text(
            text = "ТЕОРЕТИЧЕСКИЕ ОСНОВЫ И ФОРМУЛЫ НСУ",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Физико-математическая модель наземной станции управления поворотным трекером антенны FPV-дрона",
            fontSize = 11.sp,
            color = SlateTextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        FormulaCard(
            title = "1. Расчет наклонной дальности на сферической Земле",
            description = "Учитывается кривизна земной поверхности с экваториальным радиусом R = 6 378 000 м.",
            formula = "r_ant = R + h_ant\n" +
                    "r_obj = R + h_obj\n" +
                    "θ = d / R (центральный угол в радианах)\n" +
                    "Dн = √( r_ant² + r_obj² - 2 · r_ant · r_obj · cos(θ) )"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FormulaCard(
            title = "2. Идеальный угол места (Elevation Angle β)",
            description = "Угол наклона антенны относительно местного горизонта антенного поста:",
            formula = "sin(β) = ( r_obj² - r_ant² - Dн² ) / ( 2 · r_ant · Dн )\n" +
                    "β = arcsin( clamp(sin(β), -1, 1) ) · (180° / π)"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FormulaCard(
            title = "3. Пространственное рассогласование и RSSI",
            description = "Скалярное произведение единичных векторов ориентации луча антенны и истинного направления на дрон:",
            formula = "v_ant = (cos β · sin α, sin β, cos β · cos α)\n" +
                    "v_tar = (cos β_ц · sin α_ц, sin β_ц, cos β_ц · cos α_ц)\n" +
                    "cos(ΔΩ) = v_ant · v_tar\n" +
                    "BeamFactor = max(0, cos(ΔΩ · 2.5))\n" +
                    "RSSI = round( BeamFactor³ · (1 - Dн/30000) · 100% )"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FormulaCard(
            title = "4. Предел прямой радиовидимости (Радиогоризонт)",
            description = "Геометрическая видимость с учетом подъема антенны и высоты аппарата:",
            formula = "D_горизонт = √(2 · R · h_ant) + √(2 · R · h_obj)\n" +
                    "Связь возможна только при: Dн ≤ D_горизонт"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = TacticalCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ИНСТРУКЦИЯ ОПЕРАТОРА ТРЕКЕРА",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeonEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• В режиме 'Симулятор' поворачивайте антенну кнопками шага или ползунком, удерживая индикатор RSSI в зеленой зоне (>60%).\n" +
                            "• Кнопка 'АВТОТРЕКЕР' включает имитацию работы сервоприводов автоматического слежения по координатам телеметрии.\n" +
                            "• 'НАВЕСТИ НА ЦЕЛЬ' мгновенно совмещает азимут и элевацию с текущим положением FPV-дрона.\n" +
                            "• Во вкладке '3D Калькулятор' можно проверить расчетные параметры наведения антенны для любой дальности и высоты.",
                    fontSize = 11.sp,
                    color = SlateTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FormulaCard(
    title: String,
    description: String,
    formula: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TacticalCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCardBorderLight)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = SlateTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formula,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = LinkLineYellow,
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalDeepDark, RoundedCornerShape(4.dp))
                    .border(1.dp, TacticalCardBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )
        }
    }
}
