package cl.duoc.deepsource.ui.screen

import cl.duoc.deepsource.ui.theme.DeepSourceTransparent
import cl.duoc.deepsource.ui.theme.DeepSourceWhite

import cl.duoc.deepsource.ui.theme.DeepSourceWhiteOverlay01
import cl.duoc.deepsource.ui.theme.DeepSourceWhiteOverlay03
import cl.duoc.deepsource.ui.theme.DeepSourceWhiteOverlay04
import cl.duoc.deepsource.ui.theme.DeepSourceWhiteOverlay05
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay02
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay03
import cl.duoc.deepsource.ui.theme.DeepSourceWhiteOverlay09
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay06
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay07
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay10
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccentOverlay06
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccentOverlay08
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay14
import cl.duoc.deepsource.ui.theme.DeepSourceDarkSurface03
import cl.duoc.deepsource.ui.theme.DeepSourceDarkSurface06
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent03
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccent08
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccent10
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent13
import cl.duoc.deepsource.ui.theme.DeepSourceVioletAccent01
import cl.duoc.deepsource.ui.theme.DeepSourceRedAccent01
import cl.duoc.deepsource.ui.theme.DeepSourceTextBright01
import cl.duoc.deepsource.ui.theme.DeepSourceRedAccent05

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import cl.duoc.deepsource.ui.animation.motionDurationMillis
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import androidx.compose.foundation.border
import cl.duoc.deepsource.ui.components.DeepSourceAnimatedBackground


private val ScreenBackground = DeepSourceDarkSurface03
private val PrimaryTeal = DeepSourceTealAccent10
private val ActiveTeal = DeepSourceTealAccent08
private val MainText = DeepSourceTextBright01
private val MutedText = DeepSourceVioletAccent01
private val SubtleText = DeepSourceBlueAccent13
private val GlassBorder = DeepSourceWhiteOverlay09

@Composable
fun ProjectsScreen(viewModel: WorkspaceViewModel, onRegisterTime: () -> Unit, onProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var statusLegendExpanded by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    var createDialogOpen by remember { mutableStateOf(false) }
    var projectToFinish by remember { mutableStateOf<Project?>(null) }
    var projectDetailsId by remember { mutableStateOf<String?>(null) }
    var projectToEdit by remember { mutableStateOf<Project?>(null) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    val catalogError by viewModel.catalogError.collectAsStateWithLifecycle()
    val createState by viewModel.createProjectState.collectAsStateWithLifecycle()
    LaunchedEffect(createState.createdProjectId) {
        if (createState.createdProjectId != null) {
            showCompleted = false
            createDialogOpen = false
            projectToEdit = null
            viewModel.resetCreateProjectState()
        }
    }
    val isAdmin = state.isAdmin
    LaunchedEffect(isAdmin) {
        if (!isAdmin) {
            createDialogOpen = false
            projectToFinish = null
        }
    }
    val activeProjects = state.projects.filter { !it.archived && it.status != ProjectStatus.Finished }
    val completedProjects = state.projects.filter { it.archived || it.status == ProjectStatus.Finished }
    val visibleProjects = if (showCompleted) completedProjects else activeProjects

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .then(if (projectDetailsId != null) Modifier.blur(12.dp) else Modifier)
                .align(Alignment.TopCenter)
                .widthIn(max = 900.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 132.dp)
        ) {
            item { ProjectsHeader(onProfile = onProfile) }
            item {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProjectIntro(userName = state.currentUser?.name ?: "Usuario")
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DeepSourceTealAccentOverlay02)
                                .border(1.dp, PrimaryTeal, CircleShape)
                                .clickable {
                                    statusLegendExpanded = !statusLegendExpanded
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            InfoGlyph()
                        }
                    }

                    AnimatedVisibility(
                        visible = statusLegendExpanded,
                        enter = fadeIn(tween(motionDurationMillis(240))) +
                                expandVertically(tween(motionDurationMillis(240))),
                        exit = fadeOut(tween(motionDurationMillis(240))) +
                                shrinkVertically(tween(motionDurationMillis(240)))
                    ) {
                        ProjectStatusLegend(true) {
                            statusLegendExpanded = false
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProjectFilter(
                                showCompleted = showCompleted,
                                activeCount = activeProjects.size,
                                completedCount = completedProjects.size,
                                onShowActive = { showCompleted = false },
                                onShowCompleted = { showCompleted = true }
                            )
                        }

                        if (isAdmin) Surface(
                            modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable {
                                viewModel.resetCreateProjectState()
                                createDialogOpen = true
                            },
                            color = DeepSourceTealAccentOverlay03,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PrimaryTeal)
                        ) {
                            Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("+", color = PrimaryTeal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("Crear", color = MainText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            if (state.isLoading) {
                item { Text("Cargando proyectos...", color = MutedText, modifier = Modifier.padding(horizontal = 16.dp)) }
            } else if (state.error != null) {
                item { Text(state.error.orEmpty(), color = DeepSourceRedAccent01, modifier = Modifier.padding(horizontal = 16.dp)) }
            }
            if (!state.isLoading && state.error == null && visibleProjects.isEmpty()) {
                item {
                    Text(
                        text = if (showCompleted) "Todavía no tienes proyectos completados." else "No hay proyectos activos.",
                        color = MutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                    )
                }
            }
            item {
                TextButton(onClick = viewModel::refreshCatalog) { Text("Actualizar catálogo", color = PrimaryTeal) }
                catalogError?.let { Text(it, color = DeepSourceRedAccent01, modifier = Modifier.padding(horizontal = 16.dp)) }
            }
            items(visibleProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    selected = project.id == state.selectedProjectId,
                    onClick = {
                        if (project.archived) {
                            projectDetailsId = project.id
                        } else {
                            viewModel.selectProject(project.id)
                            onRegisterTime()
                        }
                    },
                    iconRes = projectIconResource(project.iconIndex),
                    onLongClick = { projectDetailsId = project.id },
                    onFinish = if (isAdmin && !project.archived && project.status != ProjectStatus.Finished && project.progress >= 1f) {
                        { projectToFinish = project }
                    } else null,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.5.dp)
                )
            }
        }
        projectDetailsId?.let { id ->
            state.projects.firstOrNull { it.id == id }?.let { project ->
                ProjectStatisticsDialog(
                    project = project,
                    entries = state.timeEntries.filter { it.projectId == id },
                    users = state.users,
                    tasks = state.tasks.filter { it.projectId == id },
                    iconRes = projectIconResource(project.iconIndex),
                    onDismiss = { projectDetailsId = null },
                    onEdit = if (isAdmin && !project.archived) ({
                        projectDetailsId = null
                        projectToEdit = project
                        viewModel.resetCreateProjectState()
                        createDialogOpen = true
                    }) else null,
                    onDelete = if (isAdmin && !project.archived) ({
                        projectDetailsId = null
                        projectToDelete = project
                    }) else null
                )
            } ?: run { projectDetailsId = null }
        }
        if (isAdmin) projectToDelete?.let { project ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { projectToDelete = null },
                title = { Text("Eliminar proyecto") },
                text = { Text("¿Eliminar \"${project.name}\" del catálogo? Su historial de horas se conserva.") },
                confirmButton = {
                    TextButton(onClick = {
                        projectToDelete = null
                        viewModel.deleteProject(project)
                    }) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { projectToDelete = null }) { Text("Cancelar") } }
            )
        }
        if (isAdmin) projectToFinish?.let { project ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { projectToFinish = null },
                title = { Text("Terminar proyecto", color = MainText) },
                text = { Text("¿Marcar \"${project.name}\" como terminado? Pasará a Completados y cambiará a turquesa.", color = MutedText) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.finishProject(project.id)
                        projectToFinish = null
                    }) { Text("Terminar", color = PrimaryTeal) }
                },
                dismissButton = {
                    TextButton(onClick = { projectToFinish = null }) { Text("Cancelar", color = MutedText) }
                },
                containerColor = DeepSourceBlueAccent03.copy(alpha = 0.88f)
            )
        }
        if (isAdmin && createDialogOpen) {
            CreateProjectDialog(
                project = projectToEdit,
                isSaving = createState.isSaving,
                error = createState.error,
                onDismiss = {
                    if (!createState.isSaving) {
                        createDialogOpen = false
                        projectToEdit = null
                        viewModel.resetCreateProjectState()
                    }
                },
                onCreate = { name, client, description, hours, iconIndex ->
                    viewModel.createProject(name, client, description, hours, iconIndex, projectToEdit)
                }
            )
        }
    }
}

@Composable
private fun ProjectsAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(DeepSourceTealAccentOverlay10, DeepSourceTransparent), Offset(size.width * .92f, -size.height * .02f), size.width * .82f),
            radius = size.width * .82f,
            center = Offset(size.width * .92f, -size.height * .02f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(DeepSourceBlueAccentOverlay06, DeepSourceTransparent), Offset(-size.width * .20f, size.height * .53f), size.width * 1.04f),
            radius = size.width * 1.04f,
            center = Offset(-size.width * .20f, size.height * .53f)
        )
    }
}

@Composable
private fun ProjectsHeader(onProfile: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = DeepSourceWhiteOverlay01,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.deepsource_logo),
                contentDescription = "DeepSource",
                modifier = Modifier
                    .width(144.dp)
                    .height(38.dp),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        DeepSourceTealAccentOverlay06,
                        CircleShape
                    )
                    .clickable(onClick = onProfile),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MS",
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectIntro(userName: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "ESPACIO DE ${userName.uppercase()}",
            color = PrimaryTeal,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "El proyecto activo se usará en tu próximo registro de horario.",
            color = MutedText,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ProjectFilter(
    showCompleted: Boolean,
    activeCount: Int,
    completedCount: Int,
    onShowActive: () -> Unit,
    onShowCompleted: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onShowActive),
            color = if (!showCompleted) ActiveTeal else DeepSourceWhiteOverlay04,
            shape = RoundedCornerShape(50),
            border = if (showCompleted) BorderStroke(1.dp, GlassBorder) else null
        ) {
            Text(
                "Activos · $activeCount",
                color = if (!showCompleted) DeepSourceWhite else MutedText,
                fontWeight = if (!showCompleted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onShowCompleted),
            color = if (showCompleted) ActiveTeal else DeepSourceWhiteOverlay04,
            shape = RoundedCornerShape(50),
            border = if (!showCompleted) BorderStroke(1.dp, GlassBorder) else null
        ) {
            Text(
                "Completados · $completedCount",
                color = if (showCompleted) DeepSourceWhite else MutedText,
                fontWeight = if (showCompleted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ProjectStatusLegend(expanded: Boolean, onToggle: () -> Unit) {
    val duration = motionDurationMillis(240)
    val rows = listOf(
        listOf<ProjectStatus?>(ProjectStatus.Initial, ProjectStatus.Medium),
        listOf(ProjectStatus.NoTime, ProjectStatus.Finished),
        listOf(ProjectStatus.Overtime, null)
    )
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(shape).clickable(onClick = onToggle),
        color = DeepSourceWhiteOverlay03,
        shape = shape,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(DeepSourceTealAccentOverlay02, CircleShape), contentAlignment = Alignment.Center) { InfoGlyph() }
                Text("Estados del proyecto", color = MainText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp).weight(1f))
                Text(if (expanded) "Ocultar" else "Ver", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                LegendChevron(expanded)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(duration)) + expandVertically(tween(duration)),
                exit = fadeOut(tween(duration)) + shrinkVertically(tween(duration))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    rows.forEach { statuses ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            statuses.forEach { status ->
                                if (status == null) {
                                    Spacer(Modifier.weight(1f))
                                } else {
                                    StatusLegendItem(status, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Text(
                        text = "Mantén presionado un proyecto para abrir su resumen completo: avance, horas registradas, participantes, historial y detalles.",
                        color = MutedText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLegendItem(status: ProjectStatus, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(Color(status.color), CircleShape))
        Text(status.label, color = MutedText, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun InfoGlyph() {
    Canvas(Modifier.size(13.dp)) {
        drawCircle(PrimaryTeal, size.minDimension * .42f, style = Stroke(1.5.dp.toPx()))
        drawCircle(PrimaryTeal, size.minDimension * .07f, Offset(size.width / 2, size.height * .32f))
        drawLine(PrimaryTeal, Offset(size.width / 2, size.height * .47f), Offset(size.width / 2, size.height * .70f), 1.5.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun LegendChevron(expanded: Boolean) {
    Canvas(Modifier.size(18.dp).padding(start = 5.dp)) {
        val top = if (expanded) .60f else .38f
        val bottom = if (expanded) .38f else .60f
        drawLine(PrimaryTeal, Offset(size.width * .28f, top * size.height), Offset(size.width * .50f, bottom * size.height), 1.7.dp.toPx(), StrokeCap.Round)
        drawLine(PrimaryTeal, Offset(size.width * .50f, bottom * size.height), Offset(size.width * .72f, top * size.height), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectCard(project: Project, selected: Boolean, onClick: () -> Unit, iconRes: Int = R.drawable.ic_icono_1, modifier: Modifier = Modifier, onFinish: (() -> Unit)? = null, onLongClick: () -> Unit = {}) {
    val accent = Color(project.status.color)
    val isFinished = project.status == ProjectStatus.Finished
    val completionPulse = rememberInfiniteTransition(label = "Pulso de proyecto terminado")
    val pulseAlpha by completionPulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Brillo turquesa"
    )
    val finishedAlpha = if (isFinished) pulseAlpha else 1f
    val animatedProgress by animateFloatAsState(
        targetValue = project.progress,
        animationSpec = tween(durationMillis = motionDurationMillis(420)),
        label = "Progreso del proyecto"
    )
    val cardShape = RoundedCornerShape(20.dp)
    Surface(
        modifier = modifier.fillMaxWidth().clip(cardShape).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (selected) DeepSourceTealAccentOverlay14 else DeepSourceBlueAccentOverlay08,
        shape = cardShape,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) PrimaryTeal else GlassBorder),
        shadowElevation = if (selected) 10.dp else 0.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.graphicsLayer { alpha = finishedAlpha }) {
                    ProjectStatusIcon(accent, iconRes)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(project.name, color = MainText, fontSize = 17.sp, lineHeight = 21.sp)
                    Text("${project.client} · ${project.description}", color = MutedText, fontSize = 12.sp)
                }
                if (onFinish != null) {
                    TextButton(onClick = onFinish) {
                        Text("Terminar", color = PrimaryTeal, fontSize = 12.sp)
                    }
                }
                if (selected) {
                    Box(Modifier.size(28.dp).background(ActiveTeal, CircleShape), contentAlignment = Alignment.Center) { CheckGlyph() }
                } else ChevronGlyph()
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${project.registeredTimeLabel} registradas", color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("de ${project.totalTimeLabel}", color = SubtleText, fontSize = 12.sp)
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                        .graphicsLayer { alpha = finishedAlpha },
                    color = accent,
                    trackColor = DeepSourceWhiteOverlay05
                )
            }
        }
    }
}

@Composable
private fun ProjectStatusIcon(accent: Color, iconRes: Int) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = .18f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = "Icono del proyecto",
            modifier = Modifier.size(34.dp),
            contentScale = ContentScale.Fit
        )
    }
}

private fun projectIconResource(index: Int): Int = when (index) {
    0 -> R.drawable.ic_icono_1
    1 -> R.drawable.ic_icono_2
    2 -> R.drawable.ic_icono_3
    3 -> R.drawable.ic_icono_4
    4 -> R.drawable.ic_icono_5
    5 -> R.drawable.ic_icono_6
    6 -> R.drawable.ic_icono_7
    7 -> R.drawable.ic_icono_8
    8 -> R.drawable.ic_icono_9
    9 -> R.drawable.ic_icono_10
    10 -> R.drawable.ic_icono_11
    11 -> R.drawable.ic_icono_12
    12 -> R.drawable.ic_icono_13
    13 -> R.drawable.ic_icono_14
    14 -> R.drawable.ic_icono_15
    15 -> R.drawable.ic_icono_16
    else -> R.drawable.ic_icono_1
}

@Composable
private fun SearchGlyph() {
    Canvas(Modifier.size(19.dp)) {
        val stroke = 1.8.dp.toPx()
        drawCircle(DeepSourceTextBright01, size.minDimension * .29f, Offset(size.width * .43f, size.height * .43f), style = Stroke(stroke))
        drawLine(DeepSourceTextBright01, Offset(size.width * .62f, size.height * .62f), Offset(size.width * .87f, size.height * .87f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CheckGlyph() {
    Canvas(Modifier.size(15.dp)) {
        drawLine(DeepSourceWhite, Offset(size.width * .20f, size.height * .52f), Offset(size.width * .43f, size.height * .73f), 2.dp.toPx(), StrokeCap.Round)
        drawLine(DeepSourceWhite, Offset(size.width * .43f, size.height * .73f), Offset(size.width * .80f, size.height * .28f), 2.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun ChevronGlyph() {
    Canvas(Modifier.size(20.dp)) {
        drawLine(MutedText, Offset(size.width * .38f, size.height * .22f), Offset(size.width * .64f, size.height * .50f), 1.8.dp.toPx(), StrokeCap.Round)
        drawLine(MutedText, Offset(size.width * .64f, size.height * .50f), Offset(size.width * .38f, size.height * .78f), 1.8.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun CreateProjectDialog(
    project: Project?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, Int) -> Unit
) {
    var name by remember(project?.id) { mutableStateOf(project?.name.orEmpty()) }
    var client by remember(project?.id) { mutableStateOf(project?.client.orEmpty()) }
    var description by remember(project?.id) { mutableStateOf(project?.description.orEmpty()) }
    var hours by remember(project?.id) { mutableStateOf(project?.let { (it.totalMinutes / 60).toString() } ?: "40") }
    var selectedIcon by remember(project?.id) { mutableStateOf(project?.iconIndex ?: 0) }
    val parsedHours = hours.toIntOrNull()
    val valid = name.isNotBlank() && client.isNotBlank() && description.isNotBlank() &&
            parsedHours != null && parsedHours in 1..100000
    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MainText, unfocusedTextColor = MainText,
        focusedBorderColor = PrimaryTeal, unfocusedBorderColor = GlassBorder,
        focusedLabelColor = PrimaryTeal, unfocusedLabelColor = MutedText,
        cursorColor = PrimaryTeal
    )

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).imePadding()
                .clip(RoundedCornerShape(24.dp)).background(DeepSourceDarkSurface06.copy(alpha = 0.75f))
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (project == null) "Crear proyecto" else "Editar proyecto", color = MainText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            if (project == null) Text("Agrega un proyecto a tu espacio de trabajo.", color = MutedText, fontSize = 13.sp)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del proyecto") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = colors)
            OutlinedTextField(value = client, onValueChange = { client = it }, label = { Text("Cliente o empresa") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = colors)
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3, colors = colors)
            OutlinedTextField(value = hours, onValueChange = { value -> hours = value.filter(Char::isDigit).take(6) },
                label = { Text("Horas estimadas") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = colors)
            Text("Ícono del proyecto", color = MainText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 3.dp)
            ) {
                items(16) { index ->
                    val selected = selectedIcon == index
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) DeepSourceTealAccentOverlay07 else DeepSourceWhiteOverlay04)
                            .border(1.dp, if (selected) PrimaryTeal else GlassBorder, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isSaving) { selectedIcon = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(projectIconResource(index)),
                            contentDescription = "Seleccionar ícono ${index + 1}",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Text("Desliza para ver más iconos", color = MutedText, fontSize = 11.sp)
            if (error != null) Text(error, color = DeepSourceRedAccent05, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar", color = MutedText) }
                Button(onClick = { if (valid) onCreate(name, client, description, parsedHours!!, selectedIcon) },
                    enabled = valid && !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveTeal),
                    shape = RoundedCornerShape(12.dp)) {
                    Text(if (isSaving) "Guardando..." else if (project == null) "Crear proyecto" else "Guardar cambios", color = DeepSourceWhite)
                }
            }
        }
    }
}
