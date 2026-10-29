
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
            hoursRegistered = project.registeredMinutes / 60,
            hoursTotal = project.totalMinutes / 60
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
    ) {
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

                // 2. Encabezado y acceso a creación
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onCreateProjectClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProjectsTheme.AccentTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "+ Crear proyecto",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // 3. Filtros animados
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedFilter = "Activos" },
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
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Activos · ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                AnimatedContent(
                                    targetState = activeCount,
                                    transitionSpec = {
                                        slideInVertically { it } + fadeIn() togetherWith
                                                slideOutVertically { -it } + fadeOut()
                                    },
                                    label = "ActiveCountAnimation"
                                ) { count ->
                                    Text(
                                        text = "$count",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { selectedFilter = "Completados" },
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
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completados · ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                AnimatedContent(
                                    targetState = completedCount,
                                    transitionSpec = {
                                        slideInVertically { it } + fadeIn() togetherWith
                                                slideOutVertically { -it } + fadeOut()
                                    },
                                    label = "CompletedCountAnimation"
                                ) { count ->
                                    Text(
                                        text = "$count",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
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
