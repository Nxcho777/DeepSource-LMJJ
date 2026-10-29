package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpandableStatusCard() {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProjectsTheme.CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, ProjectsTheme.CardBorderUnselected, RoundedCornerShape(16.dp))
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ProjectsTheme.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Estados del proyecto",
                    color = ProjectsTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Ocultar" else "Ver",
                    color = ProjectsTheme.TextSecondary,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ProjectsTheme.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = ProjectsTheme.CardBorderUnselected,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem(
                    color = ProjectsTheme.AccentTeal,
                    title = "En curso / Normal (0% - 65%)",
                    description = "Progreso según plan sin variaciones críticas."
                )
                LegendItem(
                    color = ProjectsTheme.AccentYellow,
                    title = "En atención (66% - 89%)",
                    description = "Avance medio, requiere seguimiento continuo."
                )
                LegendItem(
                    color = ProjectsTheme.AccentRed,
                    title = "Cercano al límite (90% - 99%)",
                    description = "Alerta de consumo alto de tiempo asignado."
                )
                LegendItem(
                    color = ProjectsTheme.AccentBlue,
                    title = "Completado / Meta",
                    description = "Horas registradas alcanzan el total presupuestado."
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(
                text = title,
                color = ProjectsTheme.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = ProjectsTheme.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}