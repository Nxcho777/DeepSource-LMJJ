package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import cl.duoc.deepsource.R

// --- Tema local para evitar conflictos de nombres ---
private object ProjectsTheme {
    val DarkGradientTop = Color(0xFF0D1B2A)
    val DarkGradientCenter = Color(0xFF1B263B)
    val DarkGradientBottom = Color(0xFF0D1117)

    val CardBackground = Color(0xFF161F2E).copy(alpha = 0.85f)
    val CardBorderUnselected = Color(0xFF28354A)

    val AccentTeal = Color(0xFF1ABC9C)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
}

// --- Modelo de Datos ---
data class ProjectItem(
    val id: String,
    val name: String,
    val clientAndType: String,
    val iconRes: Int,
    val hoursRegistered: Int,
    val hoursTotal: Int,
    val progressColor: Color,
    val isSelected: Boolean = false
)

@Composable
fun ProjectsHomeScreen(
    onRegisterClick: (projectId: String) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Activos") }
    var selectedProjectId by remember { mutableStateOf("1") }

    val projectsList = listOf(
        ProjectItem(
            id = "1",
            name = "Proyecto Atlas",
            clientAndType = "Deep Source · Producto",
            iconRes = R.drawable.ic_icono_1,
            hoursRegistered = 10,
            hoursTotal = 40,
            progressColor = ProjectsTheme.AccentTeal,
            isSelected = selectedProjectId == "1"
        ),
        ProjectItem(
            id = "2",
            name = "App Boreal",
            clientAndType = "Nébula Labs · Mobile",
            iconRes = R.drawable.ic_icono_2,
            hoursRegistered = 25,
            hoursTotal = 60,
            progressColor = Color(0xFFEAB308),
            isSelected = selectedProjectId == "2"
        ),
        ProjectItem(
            id = "3",
            name = "Portal Nova",
            clientAndType = "Nova Retail · Web",
            iconRes = R.drawable.ic_icono_3,
            hoursRegistered = 50,
            hoursTotal = 50,
            progressColor = Color(0xFF06B6D4),
            isSelected = selectedProjectId == "3"
        ),
        ProjectItem(
            id = "4",
            name = "Lumen CRM",
            clientAndType = "Lumen Co. · Research",
            iconRes = R.drawable.ic_icono_4,
            hoursRegistered = 75,
            hoursTotal = 90,
            progressColor = Color(0xFFEF4444),
            isSelected = selectedProjectId == "4"
        )
    )

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
                    onProjectsClick = { },
                    onRegisterClick = {onRegisterClick("1")},
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

                // 1. Top Bar Custom
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.deepsource_logo),
                            contentDescription = "Logo",
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
                                    .background(Color(0xFF1E2A3A), CircleShape)
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
                                    .background(Color(0xFF164E5B), CircleShape)
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

                // 2. Encabezado de la Pantalla
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
                }

                // 3. Chips de Filtrado
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedFilter = "Activos" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFilter == "Activos") ProjectsTheme.AccentTeal else Color.Transparent,
                                contentColor = if (selectedFilter == "Activos") Color.White else ProjectsTheme.TextSecondary
                            ),
                            border = if (selectedFilter == "Activos") null else ButtonDefaults.outlinedButtonBorder,
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Activos · 4",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { selectedFilter = "Completados" },
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Completados",
                                color = ProjectsTheme.TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 4. Banner Desplegable "Estados del proyecto"
                item {
                    ExpandableStatusCard()
                }

                // 5. Lista de Tarjetas de Proyectos (Redirecciona a Registro)
                items(projectsList) { project ->
                    ProjectCard(
                        project = project,
                        onClick = {
                            selectedProjectId = project.id
                            onRegisterClick(project.id) // Pasa "1", "2", "3" o "4"
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// --- Tarjeta Desplegable de Estados / Leyenda ---
@Composable
private fun ExpandableStatusCard() {
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
                    title = "En curso / Normal",
                    description = "Progreso según plan sin variaciones críticas."
                )
                LegendItem(
                    color = Color(0xFFEAB308),
                    title = "En atención",
                    description = "Avance medio, requiere seguimiento continuo."
                )
                LegendItem(
                    color = Color(0xFF06B6D4),
                    title = "Completado / Meta",
                    description = "Horas registradas alcanzan el total presupuestado."
                )
                LegendItem(
                    color = Color(0xFFEF4444),
                    title = "Cercano al límite",
                    description = "Alerta de consumo alto de tiempo asignado."
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

// --- Componente de Tarjeta de Proyecto ---
@Composable
private fun ProjectCard(
    project: ProjectItem,
    onClick: () -> Unit
) {
    val borderColor = if (project.isSelected) ProjectsTheme.AccentTeal else ProjectsTheme.CardBorderUnselected
    val borderWidth = if (project.isSelected) 1.5.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProjectsTheme.CardBackground, RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
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
                            color = project.progressColor.copy(alpha = 0.18f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = project.progressColor.copy(alpha = 0.45f),
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
                    text = "${project.hoursRegistered} h registradas",
                    color = ProjectsTheme.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "de ${project.hoursTotal} h",
                    color = ProjectsTheme.TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (project.hoursRegistered.toFloat() / project.hoursTotal.toFloat()).coerceIn(0f, 1f)
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
                        .fillMaxWidth(progress)
                        .background(project.progressColor, RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

// --- Barra de Navegación Inferior ---
@Composable
private fun BottomNavigationBar(
    selectedTab: String,
    onProjectsClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.GridView,
                label = "Proyectos",
                isSelected = selectedTab == "Proyectos",
                onClick = onProjectsClick
            )

            BottomNavItem(
                icon = Icons.Default.Schedule,
                label = "Registro",
                isSelected = selectedTab == "Registro",
                onClick = onRegisterClick
            )

            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedTab == "Perfil",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) ProjectsTheme.AccentTeal else ProjectsTheme.TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) ProjectsTheme.AccentTeal.copy(alpha = 0.15f) else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}