package cl.duoc.deepsource.ui.screen

import cl.duoc.deepsource.ui.theme.DeepSourceTransparent
import cl.duoc.deepsource.ui.theme.DeepSourceStatisticsGlassTop
import cl.duoc.deepsource.ui.theme.DeepSourceStatisticsGlassBottom
import cl.duoc.deepsource.ui.theme.DeepSourceWhite

import cl.duoc.deepsource.ui.theme.DeepSourceTealAccentOverlay15
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent01
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent02
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent05
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent06
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent08
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccent05
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent11
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccent14
import cl.duoc.deepsource.ui.theme.DeepSourceTealAccent15
import cl.duoc.deepsource.ui.theme.DeepSourceBlueAccent17
import cl.duoc.deepsource.ui.theme.DeepSourceBlueSurface01
import cl.duoc.deepsource.ui.theme.DeepSourceRedAccent02
import cl.duoc.deepsource.ui.theme.DeepSourceRedAccent06
import cl.duoc.deepsource.ui.theme.DeepSourceRedAccent07
import cl.duoc.deepsource.ui.theme.DeepSourceTeal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatistics
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Ink = DeepSourceBlueAccent01
private val Teal = DeepSourceTeal
private val Soft = DeepSourceBlueSurface01
private val Palette = listOf(
    DeepSourceTealAccent05, DeepSourceBlueAccent17, DeepSourceRedAccent06,
    DeepSourceBlueAccent11, DeepSourceRedAccent02, DeepSourceTealAccent15
)
private val Pending = DeepSourceTealAccent14

private fun minutesLabel(minutes: Int): String =
    minutesLabel(minutes.toLong())

private fun minutesLabel(minutes: Long): String =
    "${minutes / 60} h ${(minutes % 60).toString().padStart(2, '0')} min"

@Composable
fun ProjectStatisticsDialog(
    project: Project,
    entries: List<TimeEntry>,
    users: List<User>,
    tasks: List<cl.duoc.deepsource.domain.model.ProjectTask>,
    iconRes: Int,
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val taskNames = remember(tasks) { tasks.associate { it.id to it.name } }
    var tab by remember(project.id) { mutableIntStateOf(0) }
    var selectedEntry by remember(project.id) { mutableStateOf<TimeEntry?>(null) }
    val statistics = remember(project.totalMinutes, entries) { ProjectStatistics.calculate(project.totalMinutes, entries) }
    val byUser = statistics.byUser
    val totalLogged = statistics.registeredMinutes
    val projectTotal = statistics.plannedMinutes
    val percent = statistics.percent
    val names = remember(users) { users.associate { it.id to it.name } }

    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.70f
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(.90f).widthIn(max = 540.dp)
                .heightIn(max = maxHeight)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepSourceStatisticsGlassTop, DeepSourceStatisticsGlassBottom)
                    )
                )
                .border(1.dp, DeepSourceTealAccentOverlay15, RoundedCornerShape(28.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(project.name, color = DeepSourceWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(project.description.ifBlank { project.client }, color = Soft, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text("✕", color = DeepSourceWhite, fontSize = 23.sp, modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp))
            }
            if (project.archived) Text("Eliminado del catálogo", color = Soft, fontSize = 12.sp)
            if (onEdit != null || onDelete != null) {
                Row {
                    onEdit?.let { TextButton(onClick = it) { Text("Editar proyecto", color = Teal) } }
                    onDelete?.let { TextButton(onClick = it) { Text("Eliminar proyecto", color = DeepSourceRedAccent07) } }
                }
            }
            // Solo el contenido se desplaza; el encabezado y la X permanecen fijos.
            Column(
                Modifier.fillMaxWidth().weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("${project.status.label}  ·  ${byUser.size} participantes con registros",
                    color = Teal, fontSize = 12.sp)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(DeepSourceBlueAccent02.copy(alpha = 0.72f)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(Modifier.size(108.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val stroke = 17.dp.toPx()
                            drawArc(Pending, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                            var start = -90f
                            var remaining = 360f
                            byUser.forEachIndexed { index, (_, minutes) ->
                                val degrees = if (projectTotal > 0) minutes.toFloat() / projectTotal * 360f else 0f
                                val arc = degrees.coerceAtMost(remaining).coerceAtLeast(0f)
                                if (arc > 0f) drawArc(Palette[index % Palette.size], start, arc, false,
                                    style = Stroke(stroke, cap = StrokeCap.Butt))
                                start += arc
                                remaining -= arc
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$percent%", color = DeepSourceWhite, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                            Text("Avance", color = Soft, fontSize = 11.sp)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Aporte por participante", color = DeepSourceWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        byUser.take(5).forEachIndexed { index, (id, minutes) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(9.dp).background(Palette[index % Palette.size], CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(names[id] ?: "Usuario desconocido", color = Soft, fontSize = 11.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(if (projectTotal > 0) "${minutes * 100 / projectTotal}%" else "—",
                                    color = DeepSourceWhite, fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(9.dp).background(Pending, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("Pendiente", color = Soft, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("${100 - percent}%", color = DeepSourceWhite, fontSize = 11.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat("${minutesLabel(totalLogged)}", "Registradas", Modifier.weight(1f))
                    Stat("${minutesLabel(projectTotal)}", "Asignadas", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DeepSourceBlueAccent08.copy(alpha = 0.72f))) {
                    listOf("Historial", "Participantes", "Detalles").forEachIndexed { index, label ->
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (tab == index) Teal else DeepSourceTransparent)
                            .clickable { tab = index }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = if (tab == index) Ink else Soft)
                        }
                    }
                }
                when (tab) {
                    0 -> {
                        val ordered = remember(entries) { entries.sortedWith(compareByDescending<TimeEntry> { it.date }.thenByDescending { it.id }) }
                        if (ordered.isEmpty()) EmptyText("Aún no hay registros de horas para este proyecto.")
                        else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ordered.forEach { entry ->
                                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(DeepSourceBlueAccent05.copy(alpha = 0.72f))
                                     .clickable { selectedEntry = entry }.padding(11.dp)) {
                                    Row {
                                        Text(names[entry.userId] ?: "Usuario desconocido", color = DeepSourceWhite,
                                            fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text(minutesLabel(entry.durationMinutes), color = Teal, fontSize = 12.sp)
                                    }
                                    Text("${entry.date}  ·  ${entry.taskId?.let { taskNames[it] } ?: "Trabajo general"}",
                                        color = Soft, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        if (byUser.isEmpty()) EmptyText("Todavía no hay participantes con horas registradas.")
                        else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            byUser.forEach { (id, minutes) ->
                                val index = byUser.indexOfFirst { it.first == id }
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(DeepSourceBlueAccent05.copy(alpha = 0.72f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(12.dp).background(Palette[index % Palette.size], CircleShape))
                                    Spacer(Modifier.width(10.dp))
                                    Text(names[id] ?: "Usuario desconocido", color = DeepSourceWhite,
                                        fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text(minutesLabel(minutes), color = Teal, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Detail("Cliente", project.client)
                        Detail("Descripción", project.description)
                        Detail("Estado", project.status.label)
                        Detail("Tiempo pendiente", minutesLabel(statistics.pendingMinutes))
                        if (statistics.hasOvertime)
                            Text("Se registraron horas por encima del total asignado.", color = DeepSourceRedAccent07, fontSize = 12.sp)
                    }
                }
            } // scrollable content
        }
    }
    selectedEntry?.let { entry ->
        Dialog(
            onDismissRequest = { selectedEntry = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                Modifier.fillMaxWidth(.88f).widthIn(max = 480.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(DeepSourceStatisticsGlassTop, DeepSourceStatisticsGlassBottom)
                        )
                    )
                    .border(1.dp, Teal.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Resumen del registro", color = DeepSourceWhite,
                            fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(project.name, color = Soft, fontSize = 12.sp)
                    }
                    Text("✕", color = DeepSourceWhite, fontSize = 22.sp,
                        modifier = Modifier.clickable { selectedEntry = null }.padding(6.dp))
                }
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(DeepSourceBlueAccent05.copy(alpha = 0.72f)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Detail("Tarea", entry.taskId?.let { taskNames[it] } ?: "Trabajo general")
                    Detail("Participante", names[entry.userId] ?: "Usuario desconocido")
                    Detail("Fecha", entry.date)
                    Detail("Tiempo registrado", minutesLabel(entry.durationMinutes))
                    Detail("Descripción del registro", entry.note.ifBlank { "Sin descripción" })
                }
                Text("Volver al historial", color = Teal, fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clickable { selectedEntry = null }.padding(8.dp))
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(DeepSourceBlueAccent06.copy(alpha = 0.72f)).padding(10.dp)) {
        Text(value, color = DeepSourceWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Soft, fontSize = 11.sp)
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Column {
        Text(label, color = Teal, fontSize = 12.sp)
        Text(value.ifBlank { "Sin información" }, color = DeepSourceWhite, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyText(message: String) {
    Box(Modifier.fillMaxWidth().padding(22.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Soft, fontSize = 12.sp)
    }
}
