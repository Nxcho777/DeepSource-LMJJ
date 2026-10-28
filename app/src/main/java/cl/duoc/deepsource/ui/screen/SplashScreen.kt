package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.deepsource.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private object NewBrandPalette {
    val DeepNavy = Color(0xFF070C1E)        // Fondo azul noche profundo
    val VibrantTurquoise = Color(0xFF00D29E) // Verde turquesa/cian resplandeciente
    val MediumOcean = Color(0xFF0C527B)      // Azul de transición
    val DeepIndigo = Color(0xFF1B1242)       // Tono violeta oscuro
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.94f) }

    // Genera una fase/posición inicial aleatoria (entre 0 y 360 grados) cada vez que se abre la pantalla
    val initialPhase = remember { (Random.nextFloat() * 2 * Math.PI).toFloat() }

    val infiniteTransition = rememberInfiniteTransition(label = "FluidGradientMotion")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GradientAngle"
    )

    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(key1 = true) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(2500.milliseconds)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NewBrandPalette.DeepNavy)
            .drawBehind {
                val width = size.width
                val height = size.height

                // Ángulo actual combinado con la fase inicial aleatoria
                val currentAngle = progress + initialPhase

                // Trayectoria con mayor radio de desplazamiento para que varíe en toda la pantalla
                val turquoiseX = width * (0.45f + 0.22f * sin(currentAngle))
                val turquoiseY = height * (0.48f + 0.18f * cos(currentAngle))

                val indigoX = width * (0.65f - 0.20f * cos(currentAngle))
                val indigoY = height * (0.68f - 0.15f * sin(currentAngle))

                // Radio del pulso
                val pulseRadiusTurquoise = width * (0.72f + 0.05f * sin(currentAngle * 2f))
                val pulseRadiusIndigo = width * (0.85f + 0.06f * cos(currentAngle * 2f))

                // Resplandor turquesa
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NewBrandPalette.VibrantTurquoise.copy(alpha = 0.52f),
                            NewBrandPalette.VibrantTurquoise.copy(alpha = 0.28f),
                            NewBrandPalette.VibrantTurquoise.copy(alpha = 0.12f),
                            NewBrandPalette.MediumOcean.copy(alpha = 0.04f),
                            NewBrandPalette.MediumOcean.copy(alpha = 0.01f),
                            Color.Transparent
                        ),
                        center = Offset(turquoiseX, turquoiseY),
                        radius = pulseRadiusTurquoise
                    )
                )

                // Fondo índigo complementario
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NewBrandPalette.DeepIndigo.copy(alpha = 0.60f),
                            NewBrandPalette.DeepIndigo.copy(alpha = 0.25f),
                            NewBrandPalette.DeepIndigo.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(indigoX, indigoY),
                        radius = pulseRadiusIndigo
                    )
                )

                // Barrido sutil de haz de luz
                val sweepStartX = width * (0.5f + 0.4f * sin(currentAngle))
                val sweepEndX = width * (0.5f - 0.4f * sin(currentAngle))

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            NewBrandPalette.VibrantTurquoise.copy(alpha = 0.05f),
                            NewBrandPalette.MediumOcean.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        start = Offset(sweepStartX, 0f),
                        end = Offset(sweepEndX, height)
                    )
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
                .padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_cube),
                contentDescription = "DeepSource Cube Logo",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_splash_text),
                contentDescription = "DeepSource Text Logo",
                modifier = Modifier
                    .width(200.dp)
                    .height(36.dp),
                contentScale = ContentScale.Fit
            )
        }

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(36.dp)
                .alpha(alphaAnim.value),
            color = NewBrandPalette.VibrantTurquoise,
            strokeWidth = 3.dp
        )
    }
}