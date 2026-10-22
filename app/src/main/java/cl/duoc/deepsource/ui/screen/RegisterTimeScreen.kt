package cl.duoc.deepsource.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.widget.DeepSourceWidgetProvider
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val RegisterSurface = Color(0xFF080D1C)
private val RegisterText = Color(0xFFF7FAFF)
private val RegisterMuted = Color(0xFFAEB9D0)
private val RegisterSubtle = Color(0xFF7D8AA7)
private val RegisterTeal = Color(0xFF42C7B8)
private val RegisterAction = Color(0xFF2AAE9E)
private val RegisterBorder = Color(0x26FFFFFF)

private enum class DurationPickerStep { Hours, Minutes }

@Composable
fun RegisterTimeScreen(onProjects: () -> Unit, onProfile: () -> Unit) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf(MockData.currentUser.id) }
    var projectId by remember { mutableStateOf(MockData.selectedProjectId.value) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var hours by remember { mutableStateOf("00") }
    var minutes by remember { mutableStateOf("00") }
    var task by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var userPickerOpen by remember { mutableStateOf(false) }
    var projectPickerOpen by remember { mutableStateOf(false) }
    var durationPickerOpen by remember { mutableStateOf(false) }
    var durationPickerStep by remember { mutableStateOf(DurationPickerStep.Hours) }
    var pendingHours by remember { mutableStateOf(0) }
    var pendingMinutes by remember { mutableStateOf(0) }
    val hoursValue = hours.toIntOrNull()
    val minutesValue = minutes.toIntOrNull()
    val durationMinutes = (hoursValue ?: 0) * 60 + (minutesValue ?: 0)
    val valid = hoursValue?.let { it in 0..23 } == true &&
        minutesValue?.let { it in 0..59 } == true && durationMinutes > 0 && task.isNotBlank()
    val project = MockData.projects.firstOrNull { it.id == projectId } ?: MockData.projects.first()
    val user = MockData.users.firstOrNull { it.id == userId } ?: MockData.currentUser
    val openDurationPicker = {
        pendingHours = (hours.toIntOrNull() ?: 0).coerceIn(0, 23)
        pendingMinutes = (minutes.toIntOrNull() ?: 0).coerceIn(0, 59)
        durationPickerStep = DurationPickerStep.Hours
        durationPickerOpen = true
    }

    Box(Modifier.fillMaxSize().background(RegisterSurface)) {
        RegisterAmbientBackground()
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RegisterHeader()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("NUEVO REGISTRO", color = RegisterTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Registra tus horas", color = RegisterText, fontSize = 28.sp, lineHeight = 34.sp)
                    }
                    UserPicker(user, userPickerOpen, onToggle = { userPickerOpen = !userPickerOpen }) { selected ->
                        userId = selected.id
                        userPickerOpen = false
                    }
                    ProjectPicker(project, projectPickerOpen, onToggle = { projectPickerOpen = !projectPickerOpen }) { selected ->
                        projectId = selected.id
                        MockData.selectedProjectId.value = selected.id
                        projectPickerOpen = false
                    }
                    DateField(date) {
                        DatePickerDialog(context, { _, year, month, day ->
                            date = LocalDate.of(year, month + 1, day)
                        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
                    }
                    DurationFields(hours, minutes, openDurationPicker)
                    DurationSummary(hours, minutes)
                    RegisterTextField("Nombre de la tarea", task, { task = it }, Modifier.fillMaxWidth(), KeyboardType.Text, FieldIcon.Task)
                    savedMessage?.let { Text(it, color = RegisterTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            MockData.addTimeEntry(project.id, durationMinutes, date.toString(), task.trim())
                            MockData.selectedProjectId.value = project.id
                            DeepSourceWidgetProvider.requestRefresh(context)
                            savedMessage = "Registro guardado: ${durationMinutes.toDurationLabel()} añadidos a ${project.name}."
                            hours = "00"
                            minutes = "00"
                            task = ""
                        },
                        enabled = valid,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RegisterAction,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF365B61),
                            disabledContentColor = Color(0xFFAEB9D0)
                        )
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Guardar registro", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            CheckGlyph()
                        }
                    }
                }
            }
            DeepSourceBottomNavigation(
                activeTab = DeepSourceTab.Register,
                onProjects = onProjects,
                onRegister = {},
                onProfile = onProfile
            )
        }
        if (durationPickerOpen) {
            Dialog(onDismissRequest = { durationPickerOpen = false }) {
                val dialogShape = RoundedCornerShape(24.dp)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    contentColor = RegisterText,
                    shape = dialogShape,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(dialogShape)
                            .background(Color(0xFF0C1529), dialogShape)
                            .border(1.dp, RegisterBorder, dialogShape)
                    ) {
                        DurationPickerDialog(
                            step = durationPickerStep,
                            hours = pendingHours,
                            minutes = pendingMinutes,
                            onHoursChange = { pendingHours = it },
                            onMinutesChange = { pendingMinutes = it },
                            onCancel = { durationPickerOpen = false },
                            onContinue = { durationPickerStep = DurationPickerStep.Minutes },
                            onBack = { durationPickerStep = DurationPickerStep.Hours },
                            onConfirm = {
                                hours = pendingHours.toPickerValue()
                                minutes = pendingMinutes.toPickerValue()
                                durationPickerOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x462E73E6), Color.Transparent), Offset(size.width * 1.05f, -size.height * .05f), size.width * .90f),
            radius = size.width * .90f,
            center = Offset(size.width * 1.05f, -size.height * .05f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4A4F33C4), Color.Transparent), Offset(-size.width * .22f, size.height * .56f), size.width),
            radius = size.width,
            center = Offset(-size.width * .22f, size.height * .56f)
        )
    }
}

@Composable
private fun RegisterHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).background(Color(0x08FFFFFF)).border(1.dp, Color.Transparent).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.deepsource_logo),
            contentDescription = "Deep Source",
            modifier = Modifier.width(108.dp).height(33.dp),
            contentScale = ContentScale.Fit
        )
        Row(
            Modifier.height(34.dp).clip(CircleShape).background(Color(0x1F42C7B8)).border(1.dp, Color(0x4D42C7B8), CircleShape).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerGlyph(16.dp, RegisterTeal)
            Text("3 h 30 min hoy", color = RegisterTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UserPicker(user: User, expanded: Boolean, onToggle: () -> Unit, onUserSelected: (User) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Usuario", color = RegisterMuted, fontSize = 12.sp)
        Row(
            Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp)).clickable(onClick = onToggle).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(30.dp).background(Color(0x1A42C7B8), CircleShape), contentAlignment = Alignment.Center) {
                Text(user.name.split(" ").take(2).joinToString("") { it.first().toString() }, color = RegisterTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(user.name, color = RegisterText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(user.email, color = RegisterMuted, fontSize = 11.sp)
            }
            DownGlyph()
        }
        if (expanded) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp))) {
                MockData.users.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onUserSelected(item) }.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(28.dp).background(if (item.id == user.id) Color(0x1A42C7B8) else Color(0x12FFFFFF), CircleShape), contentAlignment = Alignment.Center) {
                            Text(item.name.split(" ").take(2).joinToString("") { it.first().toString() }, color = if (item.id == user.id) RegisterTeal else RegisterMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(item.name, color = if (item.id == user.id) RegisterTeal else RegisterText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(item.email, color = RegisterMuted, fontSize = 10.sp)
                        }
                        if (item.id == user.id) CheckGlyph()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectPicker(project: Project, expanded: Boolean, onToggle: () -> Unit, onProjectSelected: (Project) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Proyecto", color = RegisterMuted, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0x1939BFAF)).border(1.dp, RegisterTeal, RoundedCornerShape(20.dp)).clickable(onClick = onToggle).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectSymbol(Color(project.status.color))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("PROYECTO SELECCIONADO", color = RegisterMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                Text(project.name, color = RegisterText, fontSize = 17.sp)
                Text("${project.client} · ${project.description}", color = RegisterSubtle, fontSize = 12.sp)
            }
            ChevronGlyph()
        }
        if (expanded) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp))) {
                MockData.projects.forEach { item ->
                    Text(
                        item.name,
                        color = if (item.id == project.id) RegisterTeal else RegisterText,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().clickable { onProjectSelected(item) }.padding(horizontal = 14.dp, vertical = 11.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateField(date: LocalDate, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale("es", "CL")) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Fecha", color = RegisterMuted, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarGlyph()
            Text(date.format(formatter), color = RegisterText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            DownGlyph()
        }
    }
}

private enum class FieldIcon { Task, None }

@Composable
private fun RegisterTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, keyboardType: KeyboardType, icon: FieldIcon = FieldIcon.None) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = RegisterMuted, fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp)),
            textStyle = TextStyle(color = RegisterText, fontSize = 15.sp),
            cursorBrush = SolidColor(RegisterText),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { inner ->
                Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (icon) {
                        FieldIcon.Task -> TaskGlyph()
                        FieldIcon.None -> Unit
                    }
                    Box(Modifier.weight(1f)) { inner() }
                }
            }
        )
    }
}

@Composable
private fun DurationFields(hours: String, minutes: String, onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DurationSelectionField("Horas", hours, Modifier.weight(1f), onClick)
        DurationSelectionField("Minutos", minutes, Modifier.weight(1f), onClick)
    }
}

@Composable
private fun DurationSelectionField(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = RegisterMuted, fontSize = 12.sp)
        Row(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, RegisterBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value.toIntOrNull()?.coerceAtLeast(0)?.toPickerValue() ?: "00", color = RegisterText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            DownGlyph()
        }
    }
}

@Composable
private fun DurationPickerDialog(
    step: DurationPickerStep,
    hours: Int,
    minutes: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val selectingHours = step == DurationPickerStep.Hours
    val value = if (selectingHours) hours else minutes
    val max = if (selectingHours) 23 else 59
    Column(
        Modifier.fillMaxWidth().padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (selectingHours) "Seleccionar horas" else "Seleccionar minutos", color = RegisterText, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Text(if (selectingHours) "Paso 1 de 2" else "Paso 2 de 2", color = RegisterSubtle, fontSize = 12.sp)
        }
        WheelNumberPicker(
            value = value,
            maxValue = max,
            onValueChange = if (selectingHours) onHoursChange else onMinutesChange
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = if (selectingHours) onCancel else onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = RegisterMuted)
            ) {
                Text(if (selectingHours) "Cancelar" else "Atrás", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = if (selectingHours) onContinue else onConfirm,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RegisterAction, contentColor = Color.White)
            ) {
                Text(if (selectingHours) "Continuar" else "Confirmar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WheelNumberPicker(value: Int, maxValue: Int, onValueChange: (Int) -> Unit) {
    key(maxValue) {
        val itemHeight = 48.dp
        val rangeSize = maxValue + 1
        val virtualItemCount = Int.MAX_VALUE
        val middleIndex = virtualItemCount / 2
        val initialIndex = middleIndex - (middleIndex % rangeSize) + value
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
        val flingBehavior = rememberSnapFlingBehavior(listState)

        LaunchedEffect(listState) {
            snapshotFlow {
                val layout = listState.layoutInfo
                val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
                layout.visibleItemsInfo.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index?.mod(rangeSize)
            }.collect { selected ->
                selected?.let(onValueChange)
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x12FFFFFF), RoundedCornerShape(20.dp))
                .border(1.dp, RegisterBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(virtualItemCount, key = { it }) { visualIndex ->
                    val item = visualIndex % rangeSize
                    val distance = abs(item - value).let { minOf(it, rangeSize - it) }
                    val emphasis = when (distance) {
                        0 -> RegisterText
                        1 -> RegisterMuted
                        else -> RegisterSubtle.copy(alpha = .55f)
                    }
                    Text(
                        item.toPickerValue(),
                        color = emphasis,
                        fontSize = if (item == value) 28.sp else 18.sp,
                        fontWeight = if (item == value) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.height(itemHeight).fillMaxWidth().wrapContentHeight(Alignment.CenterVertically),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Box(
                Modifier.fillMaxWidth().height(itemHeight).background(Color(0x1939BFAF)).border(1.dp, RegisterTeal, RoundedCornerShape(12.dp))
            )
        }
    }
}

private fun Int.toPickerValue(): String = toString().padStart(2, '0')

@Composable
private fun DurationSummary(hours: String, minutes: String) {
    val hoursText = hours.ifBlank { "0" }
    val minutesText = minutes.ifBlank { "00" }.padStart(2, '0')
    Row(
        Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(20.dp)).background(Color(0x1942C7B8)).border(1.dp, Color(0x4D42C7B8), RoundedCornerShape(20.dp)).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Duración del registro", color = RegisterMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("$hoursText h $minutesText min", color = RegisterText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.size(42.dp).background(RegisterAction, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { TimerGlyph(21.dp, Color.White) }
    }
}

private fun Int.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return if (minutes == 0) "$hours h" else "$hours h ${minutes.toString().padStart(2, '0')} min"
}

@Composable private fun ProjectSymbol(color: Color) { Canvas(Modifier.size(42.dp)) { drawCircle(color.copy(alpha = .18f), size.minDimension / 2); drawCircle(color, size.minDimension * .12f, Offset(size.width * .33f, size.height * .35f)); drawCircle(color, size.minDimension * .12f, Offset(size.width * .65f, size.height * .35f)); drawCircle(color, size.minDimension * .12f, Offset(size.width * .50f, size.height * .66f)) } }
@Composable private fun TimerGlyph(glyphSize: androidx.compose.ui.unit.Dp, color: Color) { Canvas(Modifier.size(glyphSize)) { val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round); drawCircle(color, size.minDimension * .38f, style = stroke); drawLine(color, Offset(size.width / 2, size.height * .29f), Offset(size.width / 2, size.height * .53f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(color, Offset(size.width / 2, size.height * .53f), Offset(size.width * .67f, size.height * .53f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun TaskGlyph() { Canvas(Modifier.size(18.dp)) { val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(RegisterMuted, Offset(size.width * .14f, size.height * .16f), Size(size.width * .72f, size.height * .68f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), stroke); drawLine(RegisterMuted, Offset(size.width * .31f, size.height * .39f), Offset(size.width * .68f, size.height * .39f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .31f, size.height * .60f), Offset(size.width * .57f, size.height * .60f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun CalendarGlyph() { Canvas(Modifier.size(18.dp)) { val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(RegisterMuted, Offset(size.width * .13f, size.height * .19f), Size(size.width * .74f, size.height * .67f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), stroke); drawLine(RegisterMuted, Offset(size.width * .13f, size.height * .42f), Offset(size.width * .87f, size.height * .42f), 1.7.dp.toPx()); drawLine(RegisterMuted, Offset(size.width * .35f, size.height * .10f), Offset(size.width * .35f, size.height * .30f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .65f, size.height * .10f), Offset(size.width * .65f, size.height * .30f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun DownGlyph() { Canvas(Modifier.size(16.dp)) { drawLine(RegisterMuted, Offset(size.width * .25f, size.height * .38f), Offset(size.width * .50f, size.height * .63f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .50f, size.height * .63f), Offset(size.width * .75f, size.height * .38f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun ChevronGlyph() { Canvas(Modifier.size(20.dp)) { drawLine(RegisterMuted, Offset(size.width * .38f, size.height * .22f), Offset(size.width * .64f, size.height * .50f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .64f, size.height * .50f), Offset(size.width * .38f, size.height * .78f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun CheckGlyph() { Canvas(Modifier.size(18.dp)) { drawLine(Color.White, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .74f), 1.9.dp.toPx(), StrokeCap.Round); drawLine(Color.White, Offset(size.width * .42f, size.height * .74f), Offset(size.width * .82f, size.height * .25f), 1.9.dp.toPx(), StrokeCap.Round) } }
