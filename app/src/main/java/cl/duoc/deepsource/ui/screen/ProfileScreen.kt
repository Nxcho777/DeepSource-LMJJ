package cl.duoc.deepsource.ui.screen

import cl.duoc.deepsource.data.auth.AuthManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.ProjectStatus
import cl.duoc.deepsource.ui.component.DeepSourceBottomNavigation
import cl.duoc.deepsource.ui.component.DeepSourceTab

private val ProfileSurface = Color(0xFF080D1C)
private val ProfileText = Color(0xFFF7FAFF)
private val ProfileMuted = Color(0xFFAEB9D0)
private val ProfileSubtle = Color(0xFF7D8AA7)
private val ProfileTeal = Color(0xFF42C7B8)
private val ProfileAction = Color(0xFF2AAE9E)
private val ProfileBorder = Color(0x26FFFFFF)

@Composable
fun ProfileScreen(
    onProjects: () -> Unit,
    onRegisterTime: () -> Unit,
    onLogout: () -> Unit
) {
    val completed = MockData.projects.take(3)
    Box(Modifier.fillMaxSize().background(ProfileSurface)) {
        ProfileAmbientBackground()
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            ProfileHeader()
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileIdentity()
                    ProfileOptions(completed.size)
                    Text("Proyectos finalizados", color = ProfileText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { completed.forEach { CompletedProjectCard(it) } }
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x22FF5252),
                            contentColor = Color(0xFFFF6B6B)
                        )
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            DeepSourceBottomNavigation(
                activeTab = DeepSourceTab.Profile,
                onProjects = onProjects,
                onRegister = onRegisterTime,
                onProfile = {}
            )
        }
    }
}

@Composable
private fun ProfileAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4520C5B2), Color.Transparent), Offset(size.width * 1.02f, -size.height * .06f), size.width * .88f),
            radius = size.width * .88f,
            center = Offset(size.width * 1.02f, -size.height * .06f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4A5438C9), Color.Transparent), Offset(-size.width * .25f, size.height * .55f), size.width),
            radius = size.width,
            center = Offset(-size.width * .25f, size.height * .55f)
        )
    }
}

@Composable
private fun ProfileHeader() {
    Row(
        Modifier.fillMaxWidth().height(64.dp).background(Color(0x08FFFFFF)).border(1.dp, Color.Transparent).padding(horizontal = 16.dp),
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
private fun ProfileIdentity() {
    val shape = RoundedCornerShape(20.dp)
    val user = AuthManager.currentUser.value ?: return

    val initials = user.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth().height(90.dp).clip(shape).background(Color(0xB810214E)).border(1.dp, Color(0x3DFFFFFF), shape).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(54.dp).background(Color(0x2442C7B8), CircleShape).border(1.dp, Color(0x6642C7B8), CircleShape), contentAlignment = Alignment.Center) {
            Text(initials, color = ProfileTeal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(user.name, color = ProfileText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(user.email, color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = user.role.name, color = ProfileTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = {},
            modifier = Modifier.height(32.dp),
            shape = RoundedCornerShape(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 11.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAction, contentColor = Color.White)
        ) { Text("Editar perfil", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
    }
}

@Composable
private fun ProfileOptions(count: Int) {
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(Color(0x12FFFFFF)).border(1.dp, ProfileBorder, shape)) {
        ProfileOption("Editar perfil", "Modifica tus datos personales", OptionIcon.User, OptionEnd.Chevron)
        OptionDivider()
        ProfileOption("Privacidad", "Configura la privacidad de tu cuenta", OptionIcon.Lock, OptionEnd.Chevron)
        OptionDivider()
        ProfileOption("Notificaciones", "Configura tus notificaciones", OptionIcon.Bell, OptionEnd.Toggle)
        OptionDivider()
        ProfileOption("Proyectos finalizados", "Consulta tus proyectos completados", OptionIcon.Folder, OptionEnd.Badge(count))
    }
}

@Composable
private fun OptionDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x12FFFFFF)))

private enum class OptionIcon { User, Lock, Bell, Folder }
private sealed interface OptionEnd { data object Chevron : OptionEnd; data object Toggle : OptionEnd; data class Badge(val count: Int) : OptionEnd }

@Composable
private fun ProfileOption(title: String, subtitle: String, icon: OptionIcon, end: OptionEnd) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clickable { }.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(34.dp).background(Color(0x2442C7B8), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { OptionGlyph(icon) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = ProfileText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(subtitle, color = ProfileMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        when (end) {
            OptionEnd.Chevron -> ChevronGlyph()
            OptionEnd.Toggle -> ToggleVisual()
            is OptionEnd.Badge -> Box(Modifier.height(24.dp).clip(CircleShape).background(Color(0x2442C7B8)).border(1.dp, Color(0x4D42C7B8), CircleShape).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Text(end.count.toString(), color = ProfileTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CompletedProjectCard(project: Project) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(shape).background(Color(0x12FFFFFF)).border(1.dp, ProfileBorder, shape).padding(horizontal = 12.dp),
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

@Composable private fun ToggleVisual() = Box(Modifier.width(38.dp).height(22.dp).background(ProfileAction, CircleShape).padding(3.dp), contentAlignment = Alignment.CenterEnd) { Box(Modifier.size(16.dp).background(Color.White, CircleShape)) }
@Composable private fun CompletionIndicator() { Canvas(Modifier.size(34.dp)) { val finished = Color(ProjectStatus.Finished.color); drawCircle(finished.copy(alpha = .16f), size.minDimension / 2); drawCircle(finished, size.minDimension * .30f, style = Stroke(2.dp.toPx())); drawLine(finished, Offset(size.width * .30f, size.height * .52f), Offset(size.width * .45f, size.height * .67f), 2.dp.toPx(), StrokeCap.Round); drawLine(finished, Offset(size.width * .45f, size.height * .67f), Offset(size.width * .72f, size.height * .36f), 2.dp.toPx(), StrokeCap.Round) } }
@Composable private fun MiniCheckGlyph() { Canvas(Modifier.size(12.dp)) { val finished = Color(ProjectStatus.Finished.color); drawLine(finished, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .74f), 1.7.dp.toPx(), StrokeCap.Round); drawLine(finished, Offset(size.width * .42f, size.height * .74f), Offset(size.width * .82f, size.height * .25f), 1.7.dp.toPx(), StrokeCap.Round) } }
@Composable private fun ChevronGlyph() { Canvas(Modifier.size(18.dp)) { drawLine(ProfileMuted, Offset(size.width * .36f, size.height * .22f), Offset(size.width * .64f, size.height * .50f), 1.8.dp.toPx(), StrokeCap.Round); drawLine(ProfileMuted, Offset(size.width * .64f, size.height * .50f), Offset(size.width * .36f, size.height * .78f), 1.8.dp.toPx(), StrokeCap.Round) } }
