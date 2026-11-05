package cl.duoc.deepsource.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DeepSourceTab { Projects, Register, Profile }

@Composable
fun DeepSourceBottomNavigation(
    activeTab: DeepSourceTab,
    onProjects: () -> Unit,
    onRegister: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF081327),
                        .35f to Color(0xFF10243A),
                        .70f to Color(0xFF0B2B36),
                        1f to Color(0xFF081A2B)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem("Proyectos", DeepSourceTab.Projects, activeTab, onProjects)
            NavigationItem("Registro", DeepSourceTab.Register, activeTab, onRegister)
            NavigationItem("Perfil", DeepSourceTab.Profile, activeTab, onProfile)
        }
    }
}

@Composable
private fun NavigationItem(
    label: String,
    tab: DeepSourceTab,
    activeTab: DeepSourceTab,
    onClick: () -> Unit
) {
    val active = tab == activeTab
    val tint = if (active) NavigationActive else NavigationInactive
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .width(92.dp)
            .height(52.dp)
            .clip(shape)
            .background(
                if (active) {
                    Brush.verticalGradient(
                        listOf(Color(0x3342C7B8), Color(0x1642C7B8), Color.Transparent)
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NavigationGlyph(tab, tint)
        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun NavigationGlyph(tab: DeepSourceTab, tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
        when (tab) {
            DeepSourceTab.Projects -> {
                drawRoundRect(tint, Offset(size.width * .12f, size.height * .25f), Size(size.width * .76f, size.height * .58f), CornerRadius(3.dp.toPx()), style = stroke)
                drawLine(tint, Offset(size.width * .32f, size.height * .40f), Offset(size.width * .32f, size.height * .65f), 1.8.dp.toPx(), StrokeCap.Round)
                drawLine(tint, Offset(size.width * .62f, size.height * .35f), Offset(size.width * .62f, size.height * .58f), 1.8.dp.toPx(), StrokeCap.Round)
            }
            DeepSourceTab.Register -> {
                drawCircle(tint, size.minDimension * .37f, style = stroke)
                drawLine(tint, Offset(size.width / 2, size.height * .29f), Offset(size.width / 2, size.height * .52f), 1.8.dp.toPx(), StrokeCap.Round)
                drawLine(tint, Offset(size.width / 2, size.height * .52f), Offset(size.width * .67f, size.height * .52f), 1.8.dp.toPx(), StrokeCap.Round)
            }
            DeepSourceTab.Profile -> {
                drawCircle(tint, size.minDimension * .17f, Offset(size.width / 2, size.height * .34f), style = stroke)
                drawCircle(tint, size.minDimension * .36f, Offset(size.width / 2, size.height * .60f), style = stroke)
            }
        }
    }
}

private val NavigationActive = Color(0xFF42D9C5)
private val NavigationInactive = Color(0xFF91A1BA)
