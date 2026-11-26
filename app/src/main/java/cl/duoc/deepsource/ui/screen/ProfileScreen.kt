package cl.duoc.deepsource.ui.screen

import cl.duoc.deepsource.ui.theme.DeepSourceColor_08FFFFFF
import cl.duoc.deepsource.ui.theme.DeepSourceColor_12FFFFFF
import cl.duoc.deepsource.ui.theme.DeepSourceColor_2442C7B8
import cl.duoc.deepsource.ui.theme.DeepSourceColor_26FFFFFF
import cl.duoc.deepsource.ui.theme.DeepSourceColor_3DFFFFFF
import cl.duoc.deepsource.ui.theme.DeepSourceColor_4520C5B2
import cl.duoc.deepsource.ui.theme.DeepSourceColor_4A5438C9
import cl.duoc.deepsource.ui.theme.DeepSourceColor_4D42C7B8
import cl.duoc.deepsource.ui.theme.DeepSourceColor_6642C7B8
import cl.duoc.deepsource.ui.theme.DeepSourceColor_90142035
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF080D1C
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF142338
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF2AAE9E
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF42C7B8
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF7D8AA7
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FFAEB9D0
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FFE43939
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FFF7FAFF
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FFFF5757

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.notification.AndroidTimeEntryNotifier
import cl.duoc.deepsource.notification.NotificationPermissionStore
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import cl.duoc.deepsource.ui.components.DeepSourceAnimatedBackground
import kotlinx.coroutines.launch

private val ProfileSurface = DeepSourceColor_FF080D1C
private val ProfileText = DeepSourceColor_FFF7FAFF
private val ProfileMuted = DeepSourceColor_FFAEB9D0
private val ProfileSubtle = DeepSourceColor_FF7D8AA7
private val ProfileTeal = DeepSourceColor_FF42C7B8
private val ProfileAction = DeepSourceColor_FF2AAE9E
private val ProfileBorder = DeepSourceColor_26FFFFFF

internal fun notificationSwitchChecked(preferenceEnabled: Boolean, androidAllowed: Boolean): Boolean =
    preferenceEnabled && androidAllowed

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ProfileScreen(viewModel: WorkspaceViewModel, onProjects: () -> Unit, onRegisterTime: () -> Unit, onLogout: () -> Unit) {
    var showLogoutConfirmation by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context.findActivity()
    val permissionStore = remember(context) { NotificationPermissionStore(context) }
    var notificationsEnabled by remember(permissionStore) { mutableStateOf(permissionStore.notificationsEnabled()) }
    var androidNotificationsEnabled by remember(context) { mutableStateOf(AndroidTimeEntryNotifier.canPostNotifications(context)) }
    var canRequestPermission by remember(context) { mutableStateOf(activity?.let(permissionStore::canRequestPermission) == true) }
    var pendingEnableFromSettings by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        androidNotificationsEnabled = AndroidTimeEntryNotifier.canPostNotifications(context)
        notificationsEnabled = permissionStore.setNotificationsEnabled(granted && androidNotificationsEnabled)
        canRequestPermission = activity?.let(permissionStore::canRequestPermission) == true
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        androidNotificationsEnabled = AndroidTimeEntryNotifier.canPostNotifications(context)
        canRequestPermission = activity?.let(permissionStore::canRequestPermission) == true
        notificationsEnabled = if (pendingEnableFromSettings) {
            pendingEnableFromSettings = false
            permissionStore.setNotificationsEnabled(androidNotificationsEnabled)
        } else if (!androidNotificationsEnabled && permissionStore.notificationsEnabled()) {
            permissionStore.setNotificationsEnabled(false)
        } else {
            permissionStore.notificationsEnabled()
        }
    }
    val onNotificationsCheckedChange: (Boolean) -> Unit = { checked ->
        if (!checked) {
            notificationsEnabled = permissionStore.setNotificationsEnabled(false)
        } else {
            androidNotificationsEnabled = AndroidTimeEntryNotifier.canPostNotifications(context)
            when {
                androidNotificationsEnabled -> {
                    notificationsEnabled = permissionStore.setNotificationsEnabled(true)
                }
                activity != null && permissionStore.canRequestPermission(activity) -> {
                    notificationsEnabled = permissionStore.setNotificationsEnabled(false)
                    permissionStore.markRequested()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    notificationsEnabled = permissionStore.setNotificationsEnabled(false)
                    pendingEnableFromSettings = true
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val completed = state.profileProjects
    val currentUser = state.currentUser
    val profileScroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var editing by rememberSaveable { mutableStateOf(false) }
    var privacy by rememberSaveable { mutableStateOf(false) }
    var profileName by rememberSaveable { mutableStateOf("") }
    var profileError by rememberSaveable { mutableStateOf<String?>(null) }
    var savingProfile by remember { mutableStateOf(false) }
    val openEditor = {
        currentUser?.let {
            profileName = it.name
            profileError = null
            editing = true
        }
        Unit
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            ProfileHeader()
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(profileScroll).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 132.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.align(Alignment.CenterHorizontally).widthIn(max = 900.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileIdentity(currentUser, openEditor)
                    ProfileOptions(
                        completed.size,
                        notificationSwitchChecked(notificationsEnabled, androidNotificationsEnabled),
                        androidNotificationsEnabled,
                        canRequestPermission,
                        onNotificationsCheckedChange = onNotificationsCheckedChange,
                        onEdit = openEditor,
                        onPrivacy = { privacy = true },
                        onCompleted = { scope.launch { profileScroll.animateScrollTo(profileScroll.maxValue) } }
                    )
                    LogoutOption(onClick = { showLogoutConfirmation = true })
                    if (state.isLoading) Text("Cargando perfil...", color = ProfileMuted, fontSize = 12.sp)
                    state.error?.let { Text(it, color = DeepSourceColor_FFE43939, fontSize = 12.sp) }
                    Text("Proyectos finalizados", color = ProfileText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { completed.forEach { CompletedProjectCard(it) } }
                }
            }
        }
    }
    if (editing && currentUser != null) {
        AlertDialog(
            onDismissRequest = { if (!savingProfile) editing = false },
            title = { Text("Editar perfil") },
            text = {
                Column {
                    OutlinedTextField(profileName, { profileName = it.take(100); profileError = null },
                        label = { Text("Nombre") }, singleLine = true, enabled = !savingProfile)
                    profileError?.let { Text(it, color = DeepSourceColor_FFE43939, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(enabled = profileName.isNotBlank() && !savingProfile, onClick = {
                    savingProfile = true
                    viewModel.saveProfileName(profileName) { error ->
                        savingProfile = false
                        profileError = error
                        if (error == null) editing = false
                    }
                }) { Text("Guardar nombre") }
            },
            dismissButton = { TextButton(enabled = !savingProfile, onClick = { editing = false }) { Text("Cancelar") } }
        )
    }
    if (privacy) {
        AlertDialog(
            onDismissRequest = { privacy = false },
            title = { Text("Privacidad") },
            text = { Text("Tu cuenta y las horas registradas se guardan en este dispositivo. Los servicios guardan el catálogo de proyectos y tareas.") },
            confirmButton = { TextButton(onClick = { privacy = false }) { Text("Cerrar") } }
        )
    }
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres cerrar tu sesión y volver a la pantalla de inicio de sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    onLogout()
                }) { Text("Cerrar sesión", color = ProfileTeal) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) { Text("Cancelar") }
            },
            containerColor = DeepSourceColor_FF142338.copy(alpha = 0.75f),
            titleContentColor = ProfileText,
            textContentColor = ProfileMuted
        )
    }
}

@Composable
private fun LogoutOption(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(DeepSourceColor_90142035)
            .border(1.dp, ProfileBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cerrar sesión",
            color = DeepSourceColor_FFFF5757,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ProfileAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(DeepSourceColor_4520C5B2, Color.Transparent), Offset(size.width * 1.02f, -size.height * .06f), size.width * .88f),
            radius = size.width * .88f,
            center = Offset(size.width * 1.02f, -size.height * .06f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(DeepSourceColor_4A5438C9, Color.Transparent), Offset(-size.width * .25f, size.height * .55f), size.width),
            radius = size.width,
            center = Offset(-size.width * .25f, size.height * .55f)
        )
    }
}

@Composable
private fun ProfileHeader() {
    Row(
        Modifier.fillMaxWidth().height(64.dp).background(DeepSourceColor_08FFFFFF).border(1.dp, Color.Transparent).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.deepsource_logo),
            contentDescription = "Deep Source",
            modifier = Modifier.width(102.dp).height(31.dp),
            contentScale = ContentScale.Fit
        )
        Text("Perfil", color = ProfileText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileIdentity(user: User?, onEdit: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier.fillMaxWidth().height(90.dp).clip(shape).background(DeepSourceColor_90142035).border(1.dp, DeepSourceColor_3DFFFFFF, shape).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(54.dp).background(DeepSourceColor_2442C7B8, CircleShape).border(1.dp, DeepSourceColor_6642C7B8, CircleShape), contentAlignment = Alignment.Center) {
            val initials = user?.name?.split(" ")?.filter { it.isNotBlank() }?.take(2)
                ?.joinToString("") { it.first().uppercase() }.orEmpty()
            Text(initials, color = ProfileTeal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(user?.name.orEmpty(), color = ProfileText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(user?.email.orEmpty(), color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Button(
            onClick = onEdit,
            enabled = user != null,
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 11.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAction, contentColor = Color.White)
        ) { Text("Editar perfil", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
    }
}

@Composable
private fun ProfileOptions(
    count: Int,
    notificationsChecked: Boolean,
    androidNotificationsEnabled: Boolean,
    canRequestPermission: Boolean,
    onNotificationsCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onPrivacy: () -> Unit,
    onCompleted: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(DeepSourceColor_90142035).border(1.dp, ProfileBorder, shape)) {
        ProfileOption("Editar perfil", "Modifica tu nombre en este dispositivo", OptionIcon.User, OptionEnd.Chevron, onClick = onEdit)
        OptionDivider()
        ProfileOption("Privacidad", "Consulta dónde se guardan tus datos", OptionIcon.Lock, OptionEnd.Chevron, onClick = onPrivacy)
        OptionDivider()
        ProfileOption(
            "Notificaciones",
            when {
                androidNotificationsEnabled -> "Configura tus notificaciones"
                canRequestPermission -> "Permiso de Android requerido"
                else -> "Desactivadas en Android · Abrir ajustes"
            },
            OptionIcon.Bell,
            OptionEnd.Toggle,
            onClick = { onNotificationsCheckedChange(!notificationsChecked) },
            toggleChecked = notificationsChecked,
            onCheckedChange = onNotificationsCheckedChange
        )
        OptionDivider()
        ProfileOption("Proyectos finalizados", "Consulta tus proyectos completados", OptionIcon.Folder, OptionEnd.Badge(count), onClick = onCompleted)
    }
}

@Composable
private fun OptionDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(DeepSourceColor_12FFFFFF))

private enum class OptionIcon { User, Lock, Bell, Folder }
private sealed interface OptionEnd { data object Chevron : OptionEnd; data object Toggle : OptionEnd; data class Badge(val count: Int) : OptionEnd }

@Composable
private fun ProfileOption(
    title: String,
    subtitle: String,
    icon: OptionIcon,
    end: OptionEnd,
    onClick: () -> Unit = {},
    toggleChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onClick).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).background(DeepSourceColor_2442C7B8, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { OptionGlyph(icon) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = ProfileText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        when (end) {
            OptionEnd.Chevron -> ChevronGlyph()
            OptionEnd.Toggle -> ToggleVisual(toggleChecked, onCheckedChange)
            is OptionEnd.Badge -> Box(Modifier.height(24.dp).clip(CircleShape).background(DeepSourceColor_2442C7B8).border(1.dp, DeepSourceColor_4D42C7B8, CircleShape).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Text(end.count.toString(), color = ProfileTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompletedProjectCard(project: Project) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(shape).background(DeepSourceColor_90142035).border(1.dp, ProfileBorder, shape).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompletionIndicator()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(project.name, color = ProfileText, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("${project.client} · ${project.description}", color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(Modifier.clip(CircleShape).background(Color(ProjectStatus.Finished.color).copy(alpha = .16f)).padding(horizontal = 9.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniCheckGlyph()
            Text("Finalizado", color = Color(ProjectStatus.Finished.color), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OptionGlyph(icon: OptionIcon) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        when (icon) {
            OptionIcon.User -> { drawCircle(ProfileTeal, size.minDimension * .18f, Offset(size.width / 2, size.height * .31f), style = Stroke(stroke)); drawArc(ProfileTeal, 204f, 132f, false, Offset(size.width * .17f, size.height * .43f), Size(size.width * .66f, size.height * .48f), style = Stroke(stroke, cap = StrokeCap.Round)) }
            OptionIcon.Lock -> { drawRoundRect(ProfileTeal, Offset(size.width * .20f, size.height * .45f), Size(size.width * .60f, size.height * .40f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke)); drawArc(ProfileTeal, 180f, 180f, false, Offset(size.width * .31f, size.height * .12f), Size(size.width * .38f, size.height * .52f), style = Stroke(stroke)) }
            OptionIcon.Bell -> { drawCircle(ProfileTeal, size.minDimension * .07f, Offset(size.width / 2, size.height * .84f)); drawArc(ProfileTeal, 200f, 140f, false, Offset(size.width * .18f, size.height * .14f), Size(size.width * .64f, size.height * .65f), style = Stroke(stroke)); drawLine(ProfileTeal, Offset(size.width * .15f, size.height * .72f), Offset(size.width * .85f, size.height * .72f), stroke, StrokeCap.Round) }
            OptionIcon.Folder -> { drawRoundRect(ProfileTeal, Offset(size.width * .10f, size.height * .28f), Size(size.width * .80f, size.height * .55f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke)); drawLine(ProfileTeal, Offset(size.width * .18f, size.height * .54f), Offset(size.width * .40f, size.height * .70f), stroke, StrokeCap.Round); drawLine(ProfileTeal, Offset(size.width * .40f, size.height * .70f), Offset(size.width * .76f, size.height * .34f), stroke, StrokeCap.Round) }
        }
    }
}

@Composable private fun ToggleVisual(checked: Boolean, onCheckedChange: (Boolean) -> Unit) = Box(
    Modifier.width(38.dp).height(22.dp)
        .background(if (checked) ProfileAction else ProfileSubtle, CircleShape)
        .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
        .padding(3.dp),
    contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
) { Box(Modifier.size(16.dp).background(Color.White, CircleShape)) }
@Composable private fun CompletionIndicator() { Canvas(Modifier.size(34.dp)) { val finished = Color(ProjectStatus.Finished.color); drawCircle(finished.copy(alpha = .16f), size.minDimension / 2); drawCircle(finished, size.minDimension * .30f, style = Stroke(2.dp.toPx())); drawLine(finished, Offset(size.width * .30f, size.height * .52f), Offset(size.width * .45f, size.height * .67f), 2.dp.toPx(), StrokeCap.Round); drawLine(finished, Offset(size.width * .45f, size.height * .67f), Offset(size.width * .72f, size.height * .36f), 2.dp.toPx(), StrokeCap.Round) } }
@Composable private fun MiniCheckGlyph() { Canvas(Modifier.size(12.dp)) { val finished = Color(ProjectStatus.Finished.color); drawLine(finished, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .74f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(finished, Offset(size.width * .42f, size.height * .74f), Offset(size.width * .82f, size.height * .25f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun ChevronGlyph() { Canvas(Modifier.size(18.dp)) { drawLine(ProfileMuted, Offset(size.width * .36f, size.height * .22f), Offset(size.width * .64f, size.height * .50f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(ProfileMuted, Offset(size.width * .64f, size.height * .50f), Offset(size.width * .36f, size.height * .78f), 1.8.dp.toPx(), StrokeCap.Round) } }
