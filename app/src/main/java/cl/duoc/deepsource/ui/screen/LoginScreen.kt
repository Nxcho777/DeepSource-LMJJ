package cl.duoc.deepsource.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R

private val LoginSurface = Color(0xFF080D1C)
private val LoginText = Color(0xFFF7FAFF)
private val LoginMuted = Color(0xFFAEB9D0)
private val LoginSubtle = Color(0xFF7D8AA7)
private val LoginTeal = Color(0xFF2AAE9E)

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var username by remember { mutableStateOf("marina.soto@deepsource.cl") }
    var password by remember { mutableStateOf("deepsource") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().background(LoginSurface).imePadding()) {
        LoginAmbientBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DeepSourceLogo()
                    Text(
                        "Control de proyectos y tiempo, en un solo lugar.",
                        color = LoginMuted,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 290.dp)
                    )
                }
                Spacer(Modifier.height(32.dp))
                LoginCard(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                    rememberMe = rememberMe,
                    onRememberMeChange = { rememberMe = it },
                    onLogin = onLogin,
                    onGoogleLogin = onLogin
                )
                Spacer(Modifier.height(26.dp))
                LoginSecurityFooter()
            }
        }
    }
}

@Composable
private fun LoginAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4520C5B2), Color.Transparent), Offset(size.width * 0.72f, -size.height * 0.05f), size.width * 0.98f),
            radius = size.width * 0.98f,
            center = Offset(size.width * 0.72f, -size.height * 0.05f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4C5233C3), Color.Transparent), Offset(-size.width * 0.20f, size.height * 0.57f), size.width),
            radius = size.width,
            center = Offset(-size.width * 0.20f, size.height * 0.57f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x402069A8), Color.Transparent), Offset(size.width * 0.95f, size.height * 0.90f), size.width * 0.82f),
            radius = size.width * 0.82f,
            center = Offset(size.width * 0.95f, size.height * 0.90f)
        )
    }
}

@Composable
private fun DeepSourceLogo() {
    Image(
        painter = painterResource(R.drawable.deepsource_logo),
        contentDescription = "Deep Source",
        modifier = Modifier.width(144.dp).height(44.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun LoginCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF))), shape)
            .border(1.dp, Color(0x33FFFFFF), shape)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).background(Color(0x1A42C7B8), CircleShape).border(1.dp, Color(0x4D42C7B8), CircleShape),
                contentAlignment = Alignment.Center
            ) { UserGlyph(27.dp) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Sign In", color = LoginText, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
                Text("Accede a tu espacio de trabajo", color = LoginMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LoginField("Username", username, onUsernameChange, LoginFieldKind.User)
            LoginField(
                "Password",
                password,
                onPasswordChange,
                LoginFieldKind.Password,
                passwordVisible,
                onPasswordVisibilityChange
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onRememberMeChange(!rememberMe) },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RememberCheckbox(rememberMe)
                Text("Remember me", color = LoginMuted, fontSize = 12.sp)
            }
            Text(
                "Forgot password?",
                color = LoginMuted,
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { }
            )
        }

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LoginTeal, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                ArrowGlyph()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(Color(0x26FFFFFF)))
            Text("o continuar con", color = LoginSubtle, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Box(Modifier.weight(1f).height(1.dp).background(Color(0x26FFFFFF)))
        }

        OutlinedButton(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0x12FFFFFF),
                contentColor = LoginText
            ),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(56.dp), // Aumentado a 44dp para cancelar el borde transparente del PNG
                    contentScale = ContentScale.Fit
                )
                Text("Iniciar sesión con Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LoginText)
            }
        }
    }
}

@Composable
private fun LoginSecurityFooter() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(LoginTeal, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            "Conexión segura · Deep Source Workspace",
            color = LoginSubtle,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

private enum class LoginFieldKind { User, Password }

@Composable
private fun LoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    kind: LoginFieldKind,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = LoginMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x17FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
            singleLine = true,
            textStyle = TextStyle(color = LoginText, fontSize = 15.sp),
            cursorBrush = SolidColor(LoginText),
            visualTransformation = if (kind == LoginFieldKind.Password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (kind == LoginFieldKind.User) UserGlyph(18.dp) else LockGlyph()
                    Box(Modifier.width(1.dp).height(20.dp).background(Color(0x3DFFFFFF)))
                    Box(Modifier.weight(1f)) { innerTextField() }
                    if (kind == LoginFieldKind.Password) {
                        Box(Modifier.size(18.dp).clickable { onPasswordVisibilityChange?.invoke() }, contentAlignment = Alignment.Center) {
                            EyeGlyph(passwordVisible)
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun RememberCheckbox(checked: Boolean) {
    Box(
        Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color(0x1FFFFFFF)).border(1.dp, Color(0x3DFFFFFF), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) { if (checked) CheckGlyph() }
}

@Composable
private fun UserGlyph(glyphSize: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(glyphSize)) {
        val stroke = 1.8.dp.toPx()
        drawCircle(LoginMuted, size.minDimension * 0.19f, Offset(size.width / 2, size.height * 0.31f), style = Stroke(stroke))
        drawArc(LoginMuted, 204f, 132f, false, Offset(size.width * 0.17f, size.height * 0.43f), androidx.compose.ui.geometry.Size(size.width * 0.66f, size.height * 0.48f), style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Composable
private fun LockGlyph() {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(LoginMuted, Offset(size.width * 0.20f, size.height * 0.45f), androidx.compose.ui.geometry.Size(size.width * 0.60f, size.height * 0.40f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke))
        drawArc(LoginMuted, 180f, 180f, false, Offset(size.width * 0.31f, size.height * 0.12f), androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.52f), style = Stroke(stroke))
    }
}

@Composable
private fun EyeGlyph(visible: Boolean) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        drawOval(LoginMuted, Offset(size.width * 0.10f, size.height * 0.25f), androidx.compose.ui.geometry.Size(size.width * 0.80f, size.height * 0.50f), style = Stroke(stroke))
        drawCircle(LoginMuted, size.minDimension * 0.12f, Offset(size.width / 2, size.height / 2))
        if (!visible) drawLine(LoginMuted, Offset(size.width * 0.14f, size.height * 0.14f), Offset(size.width * 0.86f, size.height * 0.86f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CheckGlyph() {
    Canvas(Modifier.size(12.dp)) {
        drawLine(Color.White, Offset(size.width * 0.18f, size.height * 0.52f), Offset(size.width * 0.42f, size.height * 0.74f), 1.7.dp.toPx(), StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * 0.42f, size.height * 0.74f), Offset(size.width * 0.82f, size.height * 0.25f), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun ArrowGlyph() {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawLine(Color.White, Offset(size.width * 0.15f, size.height / 2), Offset(size.width * 0.83f, size.height / 2), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * 0.58f, size.height * 0.24f), Offset(size.width * 0.83f, size.height / 2), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * 0.83f, size.height / 2), Offset(size.width * 0.58f, size.height * 0.76f), stroke, StrokeCap.Round)
    }
}