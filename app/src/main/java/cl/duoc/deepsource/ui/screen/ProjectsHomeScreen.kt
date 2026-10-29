
package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.mock.MockData
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProjectsHomeScreen(
    onRegisterClick: (projectId: String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCreateProjectClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Activos") }
    var selectedProjectId by remember { mutableStateOf("project-1") }

    // Lista conectada a MockData: incluye los proyectos nuevos automáticamente.
    val projectsList = MockData.projects.map { project ->
        ProjectItem(
            id = project.id,
            name = project.name,
            clientAndType = "${project.client} · ${project.description}",
            iconRes = project.iconRes ?: R.drawable.ic_icono_1,
            registeredMinutes = project.registeredMinutes,
            totalMinutes = project.totalMinutes
        )
    }

    val activeCount = projectsList.count { !it.isCompleted }
    val completedCount = projectsList.count { it.isCompleted }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ProjectsTheme.DarkGradientTop,
                        ProjectsTheme.DarkGradientCenter,
                        ProjectsTheme.DarkGradientBottom
                    )
                )
            )
    ) { AmbientTideGlow()
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = "Proyectos",
                    onProjectsClick = {},
                    onRegisterClick = {
                        onRegisterClick(selectedProjectId)
                    },
                    onProfileClick = onProfileClick
                )
            }
        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. Barra superior
                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.deepsource_logo
                            ),
                            contentDescription = "Logo DeepSource",
                            modifier = Modifier
                                .height(48.dp)
                                .width(150.dp),
                            contentScale = ContentScale.Fit
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF1E2A3A),
                                        CircleShape
                                    )
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = ProjectsTheme.TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF164E5B),
                                        CircleShape
                                    )
                                    .clickable { onProfileClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MS",
                                    color = ProjectsTheme.AccentTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // 2. Encabezado
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ESPACIO DE MARINA",
                        color = ProjectsTheme.AccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Selecciona un proyecto",
                        color = ProjectsTheme.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "El proyecto activo se usará en tu próximo registro de horario.",
                        color = ProjectsTheme.TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 3. Crear proyecto y filtros en una sola fila
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filtro de proyectos activos
                        Button(
                            onClick = {
                                selectedFilter = "Activos"
                            },
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedFilter == "Activos")
                                        ProjectsTheme.AccentTeal
                                    else Color.Transparent,
                                contentColor =
                                    if (selectedFilter == "Activos")
                                        Color.White
                                    else ProjectsTheme.TextSecondary
                            ),
                            border =
                                if (selectedFilter == "Activos") null
                                else ButtonDefaults.outlinedButtonBorder,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Activos $activeCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Filtro de proyectos completados
                        Button(
                            onClick = {
                                selectedFilter = "Completados"
                            },
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (selectedFilter == "Completados")
                                        ProjectsTheme.AccentTeal
                                    else Color.Transparent,
                                contentColor =
                                    if (selectedFilter == "Completados")
                                        Color.White
                                    else ProjectsTheme.TextSecondary
                            ),
                            border =
                                if (selectedFilter == "Completados") null
                                else ButtonDefaults.outlinedButtonBorder,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Completados $completedCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Botón Crear, a la derecha de Completados
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = onCreateProjectClick,
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProjectsTheme.AccentTeal,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "+ Crear",
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 4. Banner de estados original
                item {
                    ExpandableStatusCard()
                }

                // 5. Tarjetas con transición entre filtros
                item {
                    AnimatedContent(
                        targetState = selectedFilter,
                        transitionSpec = {
                            if (targetState == "Completados") {
                                (slideInHorizontally { it } + fadeIn())
                                    .togetherWith(
                                        slideOutHorizontally { -it } + fadeOut()
                                    )
                            } else {
                                (slideInHorizontally { -it } + fadeIn())
                                    .togetherWith(
                                        slideOutHorizontally { it } + fadeOut()
                                    )
                            }.using(SizeTransform(clip = false))
                        },
                        label = "TabFilterTransition"
                    ) { filterState ->

                        val listToDisplay =
                            if (filterState == "Activos") {
                                projectsList.filter { !it.isCompleted }
                            } else {
                                projectsList.filter { it.isCompleted }
                            }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (listToDisplay.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text =
                                            if (filterState == "Completados")
                                                "No hay proyectos completados aún."
                                            else
                                                "No hay proyectos activos.",
                                        color = ProjectsTheme.TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                listToDisplay.forEach { project ->
                                    ProjectCard(
                                        project = project.copy(
                                            isSelected =
                                                project.id == selectedProjectId
                                        ),
                                        onClick = {
                                            selectedProjectId = project.id
                                            onRegisterClick(project.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

}


@Composable
private fun AmbientTideGlow() {
    val transition = rememberInfiniteTransition(label = "AmbientTide")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 14000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "TidePhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val radius = maxOf(w, h) * 0.42f

        // Marea superior izquierda
        val upperX = w * (
                0.04f + 0.055f * sin(phase)
                )
        val upperY = h * (
                0.06f + 0.045f * cos(phase * 0.8f)
                )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00F5D4).copy(alpha = 0.28f),
                    Color(0xFF00D6C9).copy(alpha = 0.17f),
                    Color(0xFF008FA3).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(upperX, upperY),
                radius = radius
            ),
            radius = radius,
            center = Offset(upperX, upperY)
        )

        // Marea inferior derecha, en diagonal opuesta
        val lowerX = w * (
                0.96f + 0.045f * cos(phase * 0.75f)
                )
        val lowerY = h * (
                0.94f + 0.045f * sin(phase * 0.85f)
                )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00F5D4).copy(alpha = 0.28f),
                    Color(0xFF00CDBB).copy(alpha = 0.17f),
                    Color(0xFF087C99).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(lowerX, lowerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(lowerX, lowerY)
        )
    }
}
