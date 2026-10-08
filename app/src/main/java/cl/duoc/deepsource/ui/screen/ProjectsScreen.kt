package cl.duoc.deepsource.ui.screen


import cl.duoc.deepsource.data.auth.AuthManager
import cl.duoc.deepsource.domain.model.User
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab

private val ScreenBackground = Color(0xFF080D1C)
private val PrimaryTeal = Color(0xFF42C7B8)
private val ActiveTeal = Color(0xFF2AAE9E)
private val MainText = Color(0xFFF7FAFF)
private val MutedText = Color(0xFFAEB9D0)
private val SubtleText = Color(0xFF7D8AA7)
private val GlassBorder = Color(0x26FFFFFF)

@Composable
fun ProjectsScreen(onRegisterTime: () -> Unit, onProfile: () -> Unit) {
    val user = AuthManager.currentUser.value ?: return
    val userProjects = MockData.getProjectsForUser(user.id)
    val selectedProjectId by MockData.selectedProjectId
    var statusLegendExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBackground)) {
        ProjectsAmbientBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 136.dp)
        ) {
            item { ProjectsHeader(user) }
            item {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProjectIntro(user)
                    ProjectFilter()
                    ProjectStatusLegend(statusLegendExpanded) { statusLegendExpanded = !statusLegendExpanded }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            items(userProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    selected = project.id == selectedProjectId,
                    onClick = { MockData.selectedProjectId.value = project.id },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.5.dp)
                )
            }
        }
        DeepSourceBottomNavigation(
            activeTab = DeepSourceTab.Projects,
            onProjects = {},
            onRegister = onRegisterTime,
            onProfile = onProfile,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
private fun ProjectsHeader(user: User) {
    val initials = user.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

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
                ) { Text(initials, color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun ProjectIntro(user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("ESPACIO DE ${user.name.substringBefore(" ").uppercase()}", color = PrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Selecciona un proyecto", color = MainText, fontSize = 28.sp, lineHeight = 34.sp)
        Text("El proyecto activo se usará en tu próximo registro de horario.", color = MutedText, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun ProjectFilter() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = ActiveTeal, shape = RoundedCornerShape(50)) {
            Text("Activos · 4", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }
        Surface(color = Color(0x12FFFFFF), shape = RoundedCornerShape(50), border = BorderStroke(1.dp, GlassBorder)) {
            Text("Completados", color = MutedText, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ProjectStatusLegend(expanded: Boolean, onToggle: () -> Unit) {
    val rows = listOf(
        listOf<ProjectStatus?>(ProjectStatus.Initial, ProjectStatus.Medium),
        listOf(ProjectStatus.NoTime, ProjectStatus.Overtime),
        listOf(ProjectStatus.Finished, null)
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
            verticalArrangement = Arrangement.spacedBy(if (expanded) 9.dp else 0.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(Color(0x1A42C7B8), CircleShape), contentAlignment = Alignment.Center) { InfoGlyph() }
                Text("Estados del proyecto", color = MainText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp).weight(1f))
                Text(if (expanded) "Ocultar" else "Ver", color = PrimaryTeal, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                LegendChevron(expanded)
            }
            if (expanded) {
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
fun ProjectCard(project: Project, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = Color(project.status.color)
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
                ProjectStatusIcon(accent)
                Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(project.name, color = MainText, fontSize = 17.sp, lineHeight = 21.sp)
                    Text("${project.client} · ${project.description}", color = MutedText, fontSize = 12.sp)
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
                    progress = { project.progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = accent,
                    trackColor = Color(0x14FFFFFF)
                )
            }
        }
    }
}

@Composable
private fun ProjectStatusIcon(accent: Color) {
    Canvas(Modifier.size(38.dp)) {
        drawCircle(accent.copy(alpha = .18f), size.minDimension / 2)
        drawCircle(accent, size.minDimension * .12f, Offset(size.width * .33f, size.height * .35f))
        drawCircle(accent, size.minDimension * .12f, Offset(size.width * .65f, size.height * .35f))
        drawCircle(accent, size.minDimension * .12f, Offset(size.width * .50f, size.height * .66f))
    }
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

