package cl.duoc.deepsource.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import cl.duoc.deepsource.ui.viewmodel.LoginViewModel

private val LoginSurface = Color(0xFF080D1C)
private val LoginText = Color(0xFFF7FAFF)
private val LoginMuted = Color(0xFFAEB9D0)
private val LoginSubtle = Color(0xFF7D8AA7)
private val LoginTeal = Color(0xFF2AAE9E)
private val LoginError = Color(0xFFFF6B6B)
private val InputBorder = Color(0x26FFFFFF)

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLogin: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                Spacer(Modifier.height(36.dp))
                LoginCard(
                    username = state.username,
                    usernameError = state.usernameError,
                    onUsernameChange = viewModel::setUsername,
                    password = state.password,
                    passwordError = state.passwordError,
                    onPasswordChange = viewModel::setPassword,
                    passwordVisible = state.passwordVisible,
                    onPasswordVisibilityChange = viewModel::togglePasswordVisibility,
                    rememberMe = state.rememberMe,
                    onRememberMeChange = viewModel::setRememberMe,
                    loginError = state.loginError,
                    onLogin = {
                        if (viewModel.validateLogin()) onLogin()
                    }
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
            brush = Brush.radialGradient(listOf(Color(0x4520C5B2), Color.Transparent), Offset(size.width * .72f, -size.height * .05f), size.width * .98f),
            radius = size.width * .98f,
            center = Offset(size.width * .72f, -size.height * .05f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x4C5233C3), Color.Transparent), Offset(-size.width * .20f, size.height * .57f), size.width),
            radius = size.width,
            center = Offset(-size.width * .20f, size.height * .57f)
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x402069A8), Color.Transparent), Offset(size.width * .95f, size.height * .90f), size.width * .82f),
            radius = size.width * .82f,
            center = Offset(size.width * .95f, size.height * .90f)
        )
    }
}

@Composable
private fun DeepSourceLogo() {
    androidx.compose.foundation.Image(
        painter = painterResource(R.drawable.deepsource_logo),
        contentDescription = "Deep Source",
        modifier = Modifier.width(144.dp).height(44.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun LoginCard(
    username: String,
    usernameError: String?,
    onUsernameChange: (String) -> Unit,
    password: String,
    passwordError: String?,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    loginError: String?,
    onLogin: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF))), shape)
            .border(1.dp, Color(0x33FFFFFF), shape)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
        Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
            LoginField("Username", username, onUsernameChange, LoginFieldKind.User, errorMessage = usernameError)
            LoginField(
                "Password",
                password,
                onPasswordChange,
                LoginFieldKind.Password,
                passwordVisible,
                onPasswordVisibilityChange,
                errorMessage = passwordError
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
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LoginTeal, contentColor = Color.White),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                ArrowGlyph()
            }
        }
        if (loginError != null) {
            Text(
                text = loginError,
                color = LoginError,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
    onPasswordVisibilityChange: (() -> Unit)? = null,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = LoginMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x17FFFFFF))
                .border(1.dp, if (errorMessage != null) LoginError else Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
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
        if (errorMessage != null) {
            Text(text = errorMessage, color = LoginError, fontSize = 12.sp)
        }
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
        drawCircle(LoginMuted, size.minDimension * .19f, Offset(size.width / 2, size.height * .31f), style = Stroke(stroke))
        drawArc(LoginMuted, 204f, 132f, false, Offset(size.width * .17f, size.height * .43f), androidx.compose.ui.geometry.Size(size.width * .66f, size.height * .48f), style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Composable
private fun LockGlyph() {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(LoginMuted, Offset(size.width * .20f, size.height * .45f), androidx.compose.ui.geometry.Size(size.width * .60f, size.height * .40f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = Stroke(stroke))
        drawArc(LoginMuted, 180f, 180f, false, Offset(size.width * .31f, size.height * .12f), androidx.compose.ui.geometry.Size(size.width * .38f, size.height * .52f), style = Stroke(stroke))
    }
}

@Composable
private fun EyeGlyph(visible: Boolean) {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.7.dp.toPx()
        drawOval(LoginMuted, Offset(size.width * .10f, size.height * .25f), androidx.compose.ui.geometry.Size(size.width * .80f, size.height * .50f), style = Stroke(stroke))
        drawCircle(LoginMuted, size.minDimension * .12f, Offset(size.width / 2, size.height / 2))
        if (!visible) drawLine(LoginMuted, Offset(size.width * .14f, size.height * .14f), Offset(size.width * .86f, size.height * .86f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun CheckGlyph() {
    Canvas(Modifier.size(12.dp)) {
        drawLine(Color.White, Offset(size.width * .18f, size.height * .52f), Offset(size.width * .42f, size.height * .74f), 1.7.dp.toPx(), StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * .42f, size.height * .74f), Offset(size.width * .82f, size.height * .25f), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun ArrowGlyph() {
    Canvas(Modifier.size(18.dp)) {
        val stroke = 1.8.dp.toPx()
        drawLine(Color.White, Offset(size.width * .15f, size.height / 2), Offset(size.width * .83f, size.height / 2), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * .58f, size.height * .24f), Offset(size.width * .83f, size.height / 2), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * .83f, size.height / 2), Offset(size.width * .58f, size.height * .76f), stroke, StrokeCap.Round)
    }
}
