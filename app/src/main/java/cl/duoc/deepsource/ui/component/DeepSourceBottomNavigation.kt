package cl.duoc.deepsource.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    Row(
        modifier
            .fillMaxWidth()
            .background(NavigationSurface)
            .border(1.dp, NavigationBorder)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavigationItem("Proyectos", DeepSourceTab.Projects, activeTab, onProjects)
        NavigationItem("Registro", DeepSourceTab.Register, activeTab, onRegister)
        NavigationItem("Perfil", DeepSourceTab.Profile, activeTab, onProfile)
    }
}

@Composable
private fun NavigationItem(label: String, tab: DeepSourceTab, activeTab: DeepSourceTab, onClick: () -> Unit) {
    val active = tab == activeTab
    val tint = if (active) NavigationActive else NavigationInactive
    Column(
        modifier = Modifier
            .width(88.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) NavigationActive.copy(alpha = .11f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        NavigationGlyph(tab, tint)
        Text(label, color = tint, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun NavigationGlyph(tab: DeepSourceTab, tint: Color) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
        when (tab) {
            DeepSourceTab.Projects -> {
                drawRoundRect(tint, Offset(size.width * .12f, size.height * .25f), Size(size.width * .76f, size.height * .58f), CornerRadius(3.dp.toPx()), stroke)
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

private val NavigationSurface = Color(0xF20C1529)
private val NavigationBorder = Color(0x26FFFFFF)
private val NavigationActive = Color(0xFF42C7B8)
private val NavigationInactive = Color(0xFF7D8AA7)
