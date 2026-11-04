package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.window.DialogProperties
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.notification.NotificationPermissionStore
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import cl.duoc.deepsource.ui.component.TimeReceiptDialog
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalTime
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTimeScreen(viewModel: WorkspaceViewModel, onProjects: () -> Unit, onProfile: () -> Unit) {
    val context = LocalContext.current
    val permissionStore = remember(context) { NotificationPermissionStore(context) }
    var offerNotificationPermission by remember(permissionStore) { mutableStateOf(permissionStore.shouldOfferRequest()) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val workspace by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.registerState.collectAsStateWithLifecycle()
    val date = LocalDate.parse(form.date)
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }
    var projectPickerOpen by rememberSaveable { mutableStateOf(false) }
    var durationPickerOpen by rememberSaveable { mutableStateOf(false) }
    var durationPickerStep by rememberSaveable { mutableStateOf(DurationPickerStep.Hours) }
    var pendingHours by rememberSaveable { mutableStateOf(0) }
    var pendingMinutes by rememberSaveable { mutableStateOf(0) }
    val project = workspace.selectedProject
    val user = workspace.currentUser
    LaunchedEffect(user?.id) {
        if (user != null && form.userId != user.id) viewModel.selectUser(user.id)
    }
    val openDurationPicker = {
        pendingHours = (form.hours.toIntOrNull() ?: 0).coerceIn(0, 23)
        pendingMinutes = (form.minutes.toIntOrNull() ?: 0).coerceIn(0, 59)
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
                        Text("Nuevo registro de ${user?.name ?: "usuario"}", color = RegisterText, fontSize = 28.sp, lineHeight = 34.sp)
                    }
                    if (workspace.isLoading) Text("Cargando datos...", color = RegisterMuted, fontSize = 12.sp)
                    if (offerNotificationPermission) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Recibe un aviso al guardar horas.", color = RegisterMuted, fontSize = 12.sp)
                            TextButton(onClick = {
                                permissionStore.markRequested()
                                offerNotificationPermission = false
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) { Text("Activar notificaciones", color = RegisterTeal, fontSize = 12.sp) }
                        }
                    }
                    if (project != null) {
                        SelectedProjectCard(project, workspace.projects.indexOfFirst { it.id == project.id }, onClick = { projectPickerOpen = true })
                    }
                    DateField(date) {
                        datePickerOpen = true
                    }
                    DurationFields(form.hours, form.minutes, openDurationPicker, form.durationError)
                    DurationSummary(form.hours, form.minutes)
                    RegisterTextField("Nombre de la tarea", form.task, viewModel::setTask, Modifier.fillMaxWidth(), KeyboardType.Text, FieldIcon.Task, form.taskError)
                    (form.message ?: workspace.error)?.let { Text(it, color = if (form.isError || workspace.error != null) Color(0xFFE43939) else RegisterTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = viewModel::saveTimeEntry,
                        enabled = form.isValid && !form.isSaving && form.receipt == null && !workspace.isLoading && project != null && user != null,
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
        }
        // Calendario DeepSource: reemplaza el selector nativo de Android.
        if (datePickerOpen) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = date
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )
            Dialog(
                onDismissRequest = {
                    datePickerOpen = false
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0C1529))
                        .border(
                            1.dp,
                            RegisterBorder,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(bottom = 12.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = {
                            Text(
                                text = "SELECCIONAR FECHA",
                                color = RegisterTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 24.dp, top = 20.dp)
                            )
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF0C1529),
                            titleContentColor = RegisterTeal,
                            headlineContentColor = RegisterText,
                            weekdayContentColor = RegisterMuted,
                            subheadContentColor = RegisterText,
                            navigationContentColor = RegisterTeal,
                            yearContentColor = RegisterText,
                            currentYearContentColor = RegisterTeal,
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = RegisterAction,
                            dayContentColor = RegisterText,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = RegisterAction,
                            todayContentColor = RegisterTeal,
                            todayDateBorderColor = RegisterTeal
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { datePickerOpen = false }) {
                            Text("Cancelar", color = RegisterMuted)
                        }
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val selectedDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                    viewModel.setDate(selectedDate.toString())
                                }
                                datePickerOpen = false
                            },
                            enabled = datePickerState.selectedDateMillis != null
                        ) {
                            Text(
                                "Confirmar",
                                color = if (datePickerState.selectedDateMillis != null)
                                    RegisterTeal else RegisterSubtle,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (projectPickerOpen) {
            Dialog(onDismissRequest = { projectPickerOpen = false }) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0C1529))
                        .border(1.dp, RegisterBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Cambiar proyecto", color = RegisterText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Selecciona el proyecto para este registro.", color = RegisterMuted, fontSize = 13.sp)
                    Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        workspace.projects.forEachIndexed { index, option ->
                            val iconRes = projectIconForIndex(index)
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (option.id == project?.id) Color(0x3039BFAF) else Color(0x12FFFFFF))
                                    .border(1.dp, if (option.id == project?.id) RegisterTeal else RegisterBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.selectProject(option.id)
                                        projectPickerOpen = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(option.name, color = RegisterText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Text(option.description, color = RegisterMuted, fontSize = 12.sp, maxLines = 2)
                                }
                                if (option.id == project?.id) Text("✓", color = RegisterTeal, fontSize = 18.sp)
                            }
                        }
                    }
                    TextButton(onClick = { projectPickerOpen = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancelar", color = RegisterTeal)
                    }
                }
            }
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
                                viewModel.setHours(pendingHours.toPickerValue())
                                viewModel.setMinutes(pendingMinutes.toPickerValue())
                                durationPickerOpen = false
                            }
                        )
                    }
                }
            }
        }
        form.receipt?.let { receipt ->
            TimeReceiptDialog(receipt) {
                viewModel.acceptReceipt()
                onProjects()
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
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000L)
        }
    }
    val clockFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
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
            Text(currentTime.format(clockFormat), color = RegisterTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun projectIconForIndex(projectIndex: Int): Int = when (projectIndex) {
    0 -> R.drawable.ic_icono_1
    1 -> R.drawable.ic_icono_2
    2 -> R.drawable.ic_icono_3
    3 -> R.drawable.ic_icono_4
    else -> R.drawable.ic_icono_1
}

@Composable
private fun SelectedProjectCard(project: Project, projectIndex: Int, onClick: () -> Unit) {
    val iconRes = projectIconForIndex(projectIndex)
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Proyecto", color = RegisterMuted, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x1939BFAF))
                .border(1.dp, RegisterBorder, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(iconRes),
                contentDescription = "Icono de ${project.name}",
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(project.name, color = RegisterText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(project.description, color = RegisterMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Text("⌄", color = RegisterTeal, fontSize = 24.sp)
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
private fun RegisterTextField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier, keyboardType: KeyboardType, icon: FieldIcon = FieldIcon.None, errorMessage: String? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = RegisterMuted, fontSize = 12.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, if (errorMessage != null) Color(0xFFE43939) else RegisterBorder, RoundedCornerShape(14.dp)),
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
        if (errorMessage != null) {
            Text(errorMessage, color = Color(0xFFE43939), fontSize = 12.sp)
        }
    }
}

@Composable
private fun DurationFields(hours: String, minutes: String, onClick: () -> Unit, errorMessage: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DurationSelectionField("Horas", hours, Modifier.weight(1f), onClick, errorMessage)
            DurationSelectionField("Minutos", minutes, Modifier.weight(1f), onClick, errorMessage)
        }
        if (errorMessage != null) {
            Text(errorMessage, color = Color(0xFFE43939), fontSize = 12.sp)
        }
    }
}

@Composable
private fun DurationSelectionField(label: String, value: String, modifier: Modifier, onClick: () -> Unit, errorMessage: String? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = RegisterMuted, fontSize = 12.sp)
        Row(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x12FFFFFF)).border(1.dp, if (errorMessage != null) Color(0xFFE43939) else RegisterBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
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


@Composable private fun ProjectSymbol(color: Color) { Canvas(Modifier.size(42.dp)) { drawCircle(color.copy(alpha = .18f), size.minDimension / 2); drawCircle(color, size.minDimension * .12f, Offset(size.width * .33f, size.height * .35f)); drawCircle(color, size.minDimension * .12f, Offset(size.width * .65f, size.height * .35f)); drawCircle(color, size.minDimension * .12f, Offset(size.width * .50f, size.height * .66f)) } }
@Composable private fun TimerGlyph(glyphSize: androidx.compose.ui.unit.Dp, color: Color) { Canvas(Modifier.size(glyphSize)) { val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round); drawCircle(color, size.minDimension * .38f, style = stroke); drawLine(color, Offset(size.width / 2, size.height * .29f), Offset(size.width / 2, size.height * .53f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(color, Offset(size.width / 2, size.height * .53f), Offset(size.width * .67f, size.height * .53f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun TaskGlyph() { Canvas(Modifier.size(18.dp)) { val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(RegisterMuted, Offset(size.width * .14f, size.height * .16f), Size(size.width * .72f, size.height * .68f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), stroke); drawLine(RegisterMuted, Offset(size.width * .31f, size.height * .39f), Offset(size.width * .68f, size.height * .39f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .31f, size.height * .60f), Offset(size.width * .57f, size.height * .60f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun CalendarGlyph() { Canvas(Modifier.size(18.dp)) { val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round); drawRoundRect(RegisterMuted, Offset(size.width * .13f, size.height * .19f), Size(size.width * .74f, size.height * .67f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), stroke); drawLine(RegisterMuted, Offset(size.width * .13f, size.height * .42f), Offset(size.width * .87f, size.height * .42f), 1.7.dp.toPx()); drawLine(RegisterMuted, Offset(size.width * .35f, size.height * .10f), Offset(size.width * .35f, size.height * .30f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .65f, size.height * .10f), Offset(size.width * .65f, size.height * .30f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun DownGlyph() { Canvas(Modifier.size(16.dp)) { drawLine(RegisterMuted, Offset(size.width * .25f, size.height * .38f), Offset(size.width * .50f, size.height * .63f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .50f, size.height * .63f), Offset(size.width * .75f, size.height * .38f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun ChevronGlyph() { Canvas(Modifier.size(20.dp)) { drawLine(RegisterMuted, Offset(size.width * .38f, size.height * .22f), Offset(size.width * .64f, size.height * .50f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(RegisterMuted, Offset(size.width * .64f, size.height * .50f), Offset(size.width * .38f, size.height * .78f), 1.8.dp.toPx(), StrokeCap.Round) } }
@Composable private fun CheckGlyph() { Canvas(Modifier.size(18.dp)) { drawLine(Color.White, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .74f), 1.9.dp.toPx(), StrokeCap.Round); drawLine(Color.White, Offset(size.width * .42f, size.height * .74f), Offset(size.width * .82f, size.height * .25f), 1.9.dp.toPx(), StrokeCap.Round) } }
