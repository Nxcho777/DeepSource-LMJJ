package cl.duoc.deepsource.ui.screen


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProjectCard(
    project: ProjectItem,
    onClick: () -> Unit
) {
    // 1. Animación suave del progreso de la barra (0.0f a 1.0f)
    val targetProgress = if (project.totalMinutes > 0) {
        (
                project.registeredMinutes.toFloat() /
                        project.totalMinutes.toFloat()
                ).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    // 2. Transición suave del color dinámico
    val animatedColor by animateColorAsState(
        targetValue = project.progressColor,
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )

    // 3. Transición de selección (borde y grosor)
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (project.isSelected) 2.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "BorderWidthAnimation"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (project.isSelected) ProjectsTheme.AccentTeal else ProjectsTheme.CardBorderUnselected,
        animationSpec = tween(durationMillis = 300),
        label = "BorderColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProjectsTheme.CardBackground, RoundedCornerShape(20.dp))
            .border(animatedBorderWidth, animatedBorderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = animatedColor.copy(alpha = 0.18f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = animatedColor.copy(alpha = 0.45f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = project.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = project.name,
                        color = ProjectsTheme.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project.clientAndType,
                        color = ProjectsTheme.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (project.isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Seleccionado",
                        tint = ProjectsTheme.AccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ProjectsTheme.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${project.registeredMinutes / 60} h " +
                            "${project.registeredMinutes % 60} min registradas",
                    color = ProjectsTheme.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "de ${project.totalMinutes / 60} h " +
                            "${project.totalMinutes % 60} min",
                    color = ProjectsTheme.TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Progreso con Ancho y Color Animados
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF233144))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(animatedColor, RoundedCornerShape(10.dp))
                )
            }
        }
    }
}