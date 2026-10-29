
package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// -----------------------------------------------------
// PALETA DEL FONDO ANIMADO
// -----------------------------------------------------

private object AnimatedBrandPalette {
    val DeepNavy = Color(0xFF070C1E)
    val VibrantTurquoise = Color(0xFF00D29E)
    val MediumOcean = Color(0xFF0C527B)
    val DeepIndigo = Color(0xFF1B1242)
}

// -----------------------------------------------------
// PALETA DE LA INTERFAZ
// -----------------------------------------------------

val DarkGradientTop = Color(0xFF0D1B2A)
val DarkGradientCenter = Color(0xFF1B263B)
val DarkGradientBottom = Color(0xFF0D1117)

val GlassFillTop = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val GlassFillBottom = Color(0xFFFFFFFF).copy(alpha = 0.03f)
val GlassBorderTop = Color(0xFFFFFFFF).copy(alpha = 0.40f)
val GlassBorderBottom = Color(0xFFFFFFFF).copy(alpha = 0.08f)

val InputFieldBackground = Color(0xFF0F172A).copy(alpha = 0.5f)
val InputFieldBorder = Color(0xFF334155).copy(alpha = 0.7f)

val AccentTeal = Color(0xFF1ABC9C)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8)
val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (username: String, pass: String) -> Boolean = { _, _ -> true },
    onForgotPasswordClick: () -> Unit = {}
) {
    var username by remember {
        mutableStateOf("marina.soto@deepsource.cl")
    }
    var password by remember {
        mutableStateOf("12345678")
    }
    var passwordVisible by remember {
        mutableStateOf(false)
    }
    var rememberMe by remember {
        mutableStateOf(true)
    }
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val glassShape = RoundedCornerShape(24.dp)

    // -------------------------------------------------
    // POSICIONES ALEATORIAS DEL FONDO
    // -------------------------------------------------

    val turquoiseX = remember { Animatable(0.45f) }
    val turquoiseY = remember { Animatable(0.48f) }

    val indigoX = remember { Animatable(0.65f) }
    val indigoY = remember { Animatable(0.68f) }

    LaunchedEffect(Unit) {
        while (true) {
            coroutineScope {
                launch {
                    turquoiseX.animateTo(
                        targetValue = Random.nextFloat().coerceIn(0.10f, 0.90f),
                        animationSpec = tween(
                            durationMillis = Random.nextInt(3000, 6500),
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                launch {
                    turquoiseY.animateTo(
                        targetValue = Random.nextFloat().coerceIn(0.10f, 0.90f),
                        animationSpec = tween(
                            durationMillis = Random.nextInt(3000, 6500),
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                launch {
                    indigoX.animateTo(
                        targetValue = Random.nextFloat().coerceIn(0.10f, 0.90f),
                        animationSpec = tween(
                            durationMillis = Random.nextInt(4000, 7500),
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                launch {
                    indigoY.animateTo(
                        targetValue = Random.nextFloat().coerceIn(0.10f, 0.90f),
                        animationSpec = tween(
                            durationMillis = Random.nextInt(4000, 7500),
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    // Animación independiente para la pulsación y el barrido de luz.
    val infiniteTransition = rememberInfiniteTransition(
        label = "LoginFluidGradientMotion"
    )

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoginGradientAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AnimatedBrandPalette.DeepNavy)
            .drawBehind {
                val width = size.width
                val height = size.height
                val currentAngle = progress

                // Los centros ahora dependen de las posiciones aleatorias.
                val turquoiseCenter = Offset(
                    turquoiseX.value * width,
                    turquoiseY.value * height
                )

                val indigoCenter = Offset(
                    indigoX.value * width,
                    indigoY.value * height
                )

                // Pulsación de los resplandores.
                val pulseRadiusTurquoise =
                    width * (0.72f + 0.05f * sin(currentAngle * 2f))

                val pulseRadiusIndigo =
                    width * (0.85f + 0.06f * cos(currentAngle * 2f))

                // Resplandor turquesa.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AnimatedBrandPalette.VibrantTurquoise.copy(alpha = 0.52f),
                            AnimatedBrandPalette.VibrantTurquoise.copy(alpha = 0.28f),
                            AnimatedBrandPalette.VibrantTurquoise.copy(alpha = 0.12f),
                            AnimatedBrandPalette.MediumOcean.copy(alpha = 0.04f),
                            AnimatedBrandPalette.MediumOcean.copy(alpha = 0.01f),
                            Color.Transparent
                        ),
                        center = turquoiseCenter,
                        radius = pulseRadiusTurquoise
                    )
                )

                // Resplandor índigo.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AnimatedBrandPalette.DeepIndigo.copy(alpha = 0.60f),
                            AnimatedBrandPalette.DeepIndigo.copy(alpha = 0.25f),
                            AnimatedBrandPalette.DeepIndigo.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = indigoCenter,
                        radius = pulseRadiusIndigo
                    )
                )

                // Barrido diagonal de luz.
                val sweepStartX =
                    width * (0.5f + 0.4f * sin(currentAngle))

                val sweepEndX =
                    width * (0.5f - 0.4f * sin(currentAngle))

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            AnimatedBrandPalette.VibrantTurquoise.copy(alpha = 0.05f),
                            AnimatedBrandPalette.MediumOcean.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        start = Offset(sweepStartX, 0f),
                        end = Offset(sweepEndX, height)
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // HEADER: LOGO Y ESLOGAN
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.deepsource_logo),
                    contentDescription = "Deep Source Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(80.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Control de proyectos y tiempo, en un solo\nlugar.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CONTENEDOR PRINCIPAL LIQUID GLASS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassFillTop,
                                GlassFillBottom
                            )
                        ),
                        shape = glassShape
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassBorderTop,
                                GlassBorderBottom
                            )
                        ),
                        shape = glassShape
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // ENCABEZADO DE INICIO DE SESIÓN
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Sign In",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Accede a tu espacio de trabajo",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // USERNAME
                    Text(
                        text = "Username",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputFieldBackground,
                            unfocusedContainerColor = InputFieldBackground,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = InputFieldBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD
                    Text(
                        text = "Password",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    },
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputFieldBackground,
                            unfocusedContainerColor = InputFieldBackground,
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = InputFieldBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    // MENSAJE DE ERROR
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorMessage!!,
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // RECORDAR USUARIO Y RECUPERAR CONTRASEÑA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                rememberMe = !rememberMe
                            }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = {
                                    rememberMe = it
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentTeal,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = TextPrimary
                                )
                            )

                            Text(
                                text = "Remember me",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "Forgot password?",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                onForgotPasswordClick()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BOTÓN LOGIN
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage =
                                    "Por favor, ingresa correo y contraseña"
                            } else {
                                val success = onLoginClick(username, password)

                                if (!success) {
                                    errorMessage = "Credenciales incorrectas"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentTeal,
                            contentColor = TextPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "LOGIN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // SEPARADOR PARA GOOGLE
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.18f)
                        )

                        Text(
                            text = "  o continúa con  ",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.18f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN VISUAL DE GOOGLE
                    OutlinedButton(
                        onClick = {
                            // La autenticación real con Google se conectará después.
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.22f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.04f),
                            contentColor = TextPrimary
                        )
                    ) {
                        Image(
                            painter = painterResource(
                                id = R.drawable.ic_google
                            ),
                            contentDescription = "Google",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Continuar con Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FOOTER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentTeal, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Conexión segura · Deep Source Workspace",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
