package cl.duoc.deepsource.ui.screen

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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

private val ScreenBackground = Color(0xFF080D1C)
private val PrimaryTeal = Color(0xFF42C7B8)
private val ActiveTeal = Color(0xFF2AAE9E)
private val MainText = Color(0xFFF7FAFF)
private val MutedText = Color(0xFFAEB9D0)
private val SubtleText = Color(0xFF7D8AA7)
private val GlassBorder = Color(0x26FFFFFF)

@Composable
fun ProjectsScreen(viewModel: WorkspaceViewModel, onRegisterTime: () -> Unit, onProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var statusLegendExpanded by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    var createDialogOpen by remember { mutableStateOf(false) }
    var projectToFinish by remember { mutableStateOf<Project?>(null) }
    val createState by viewModel.createProjectState.collectAsStateWithLifecycle()
    LaunchedEffect(createState.createdProjectId) {
        if (createState.createdProjectId != null) {
            showCompleted = false
            createDialogOpen = false
            viewModel.resetCreateProjectState()
        }
    }
    val activeProjects = state.projects.filter { it.status != ProjectStatus.Finished }
    val completedProjects = state.projects.filter { it.status == ProjectStatus.Finished }
    val visibleProjects = if (showCompleted) completedProjects else activeProjects

    Box(modifier = Modifier.fillMaxSize().background(ScreenBackground)) {
        ProjectsAmbientBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { ProjectsHeader() }
            item {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProjectIntro()
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable {
                            viewModel.resetCreateProjectState()
                            createDialogOpen = true
                        },
                        color = Color(0x1F2AAE9E),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PrimaryTeal)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("+", color = PrimaryTeal, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Crear proyecto", color = MainText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    ProjectFilter(
                        showCompleted = showCompleted,
                        activeCount = activeProjects.size,
                        completedCount = completedProjects.size,
                        onShowActive = { showCompleted = false },
                        onShowCompleted = { showCompleted = true }
                    )
                    ProjectStatusLegend(statusLegendExpanded) { statusLegendExpanded = !statusLegendExpanded }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            if (state.isLoading) {
                item { Text("Cargando proyectos...", color = MutedText, modifier = Modifier.padding(horizontal = 16.dp)) }
            } else if (state.error != null) {
                item { Text(state.error.orEmpty(), color = Color(0xFFE43939), modifier = Modifier.padding(horizontal = 16.dp)) }
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
            items(visibleProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    selected = project.id == state.selectedProjectId,
                    onClick = {
                        viewModel.selectProject(project.id)
                        onRegisterTime()
                    },
                    iconRes = projectIconResource(project.iconIndex),
                    onFinish = if (project.isCompleted) null else { { projectToFinish = project } },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.5.dp)
                )
            }
        }
        projectToFinish?.let { project ->
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
                containerColor = Color(0xFF111B30)
            )
        }
        if (createDialogOpen) {
            CreateProjectDialog(
                isSaving = createState.isSaving,
                error = createState.error,
                onDismiss = {
                    if (!createState.isSaving) {
                        createDialogOpen = false
                        viewModel.resetCreateProjectState()
                    }
                },
                onCreate = { name, client, description, hours, iconIndex ->
                    viewModel.createProject(name, client, description, hours, iconIndex)
                }
            )
        }
    }
}

@Composable
private fun ProjectsAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4D1EB9A8), Color.Transparent), Offset(size.width * .92f, -size.height * .02f), size.width * .82f),
            radius = size.width * .82f,
            center = Offset(size.width * .92f, -size.height * .02f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4D6D49DE), Color.Transparent), Offset(-size.width * .20f, size.height * .53f), size.width * 1.04f),
            radius = size.width * 1.04f,
            center = Offset(-size.width * .20f, size.height * .53f)
        )
    }
}

@Composable
private fun ProjectsHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        color = Color(0x08FFFFFF),
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.deepsource_logo),
                contentDescription = "Deep Source",
                modifier = Modifier.width(108.dp).height(33.dp),
                contentScale = ContentScale.Fit
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).background(Color(0x12FFFFFF), RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) { SearchGlyph() }
                Box(
                    modifier = Modifier.size(38.dp).background(Color(0x2942C7B8), CircleShape).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("MS", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun ProjectIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("ESPACIO DE MARINA", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Selecciona un proyecto", color = MainText, fontSize = 28.sp, lineHeight = 34.sp)
        Text("El proyecto activo se usará en tu próximo registro de horario.", color = MutedText, fontSize = 13.sp, lineHeight = 19.sp)
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onShowActive),
            color = if (!showCompleted) ActiveTeal else Color(0x12FFFFFF),
            shape = RoundedCornerShape(50),
            border = if (showCompleted) BorderStroke(1.dp, GlassBorder) else null
        ) {
            Text(
                "Activos · $activeCount",
                color = if (!showCompleted) Color.White else MutedText,
                fontWeight = if (!showCompleted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onShowCompleted),
            color = if (showCompleted) ActiveTeal else Color(0x12FFFFFF),
            shape = RoundedCornerShape(50),
            border = if (!showCompleted) BorderStroke(1.dp, GlassBorder) else null
        ) {
            Text(
                "Completados · $completedCount",
                color = if (showCompleted) Color.White else MutedText,
                fontWeight = if (showCompleted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
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
        color = Color(0x0EFFFFFF),
        shape = shape,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(Color(0x1A42C7B8), CircleShape), contentAlignment = Alignment.Center) { InfoGlyph() }
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

@Composable
fun ProjectCard(project: Project, selected: Boolean, onClick: () -> Unit, iconRes: Int = R.drawable.ic_icono_1, modifier: Modifier = Modifier, onFinish: (() -> Unit)? = null) {
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
        modifier = modifier.fillMaxWidth().clip(cardShape).clickable(onClick = onClick),
        color = if (selected) Color(0x1F39BFAF) else Color(0x12FFFFFF),
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
                    trackColor = Color(0x14FFFFFF)
                )
            }
        }
    }
}

@Composable
private fun ProjectStatusIcon(accent: Color, iconRes: Int) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = .18f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = "Icono del proyecto",
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )
    }
}

private fun projectIconResource(index: Int): Int = when (index) {
    0 -> R.drawable.ic_icono_1
    1 -> R.drawable.ic_icono_2
    2 -> R.drawable.ic_icono_3
    3 -> R.drawable.ic_icono_4
    else -> R.drawable.ic_icono_1
}

@Composable
private fun SearchGlyph() {
    Canvas(Modifier.size(19.dp)) {
        val stroke = 1.8.dp.toPx()
        drawCircle(Color(0xFFF7FAFF), size.minDimension * .29f, Offset(size.width * .43f, size.height * .43f), style = Stroke(stroke))
        drawLine(Color(0xFFF7FAFF), Offset(size.width * .62f, size.height * .62f), Offset(size.width * .87f, size.height * .87f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CheckGlyph() {
    Canvas(Modifier.size(15.dp)) {
        drawLine(Color.White, Offset(size.width * .20f, size.height * .52f), Offset(size.width * .43f, size.height * .73f), 2.dp.toPx(), StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * .43f, size.height * .73f), Offset(size.width * .80f, size.height * .28f), 2.dp.toPx(), StrokeCap.Round)
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
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("40") }
    var selectedIcon by remember { mutableStateOf(0) }
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
                .clip(RoundedCornerShape(24.dp)).background(Color(0xFF0C1529))
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Crear proyecto", color = MainText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Agrega un proyecto a tu espacio de trabajo.", color = MutedText, fontSize = 13.sp)
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (0..3).forEach { index ->
                    val selected = selectedIcon == index
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Color(0x3030B2A6) else Color(0x12FFFFFF))
                            .border(1.dp, if (selected) PrimaryTeal else GlassBorder, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isSaving) { selectedIcon = index }
                            .padding(vertical = 12.dp),
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
            if (error != null) Text(error, color = Color(0xFFFF8585), fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar", color = MutedText) }
                Button(onClick = { if (valid) onCreate(name, client, description, parsedHours!!, selectedIcon) },
                    enabled = valid && !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveTeal),
                    shape = RoundedCornerShape(12.dp)) {
                    Text(if (isSaving) "Guardando..." else "Crear proyecto", color = Color.White)
                }
            }
        }
    }
}
