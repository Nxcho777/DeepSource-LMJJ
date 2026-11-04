
package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.deepsource.R
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random
import kotlinx.coroutines.launch

private val SplashBackground = Color(0xFF050B19)
private val SplashTeal = Color(0xFF00E5C3)

private const val SPLASH_DURATION = 2600L
private const val GLOW_MOVE_DURATION = 1250

// Selecciona una nueva ubicación alejada de las anteriores.
private fun nextGlowPosition(
    history: List<Offset>
): Offset {
    repeat(80) {
        val candidate = Offset(
            x = Random.nextFloat() * 0.90f + 0.05f,
            y = Random.nextFloat() * 0.76f + 0.12f
        )

        val farEnough = history.all { previous ->
            hypot(
                (candidate.x - previous.x).toDouble(),
                (candidate.y - previous.y).toDouble()
            ) > 0.30
        }

        if (farEnough) {
            return candidate
        }
    }

    // Si no existe una posición suficientemente alejada
    // de todas las anteriores, prioriza la más distante.
    val candidates = List(40) {
        Offset(
            x = Random.nextFloat() * 0.90f + 0.05f,
            y = Random.nextFloat() * 0.76f + 0.12f
        )
    }

    return candidates.maxByOrNull { candidate ->
        history.minOfOrNull { previous ->
            hypot(
                (candidate.x - previous.x).toDouble(),
                (candidate.y - previous.y).toDouble()
            )
        } ?: 1.0
    } ?: Offset(0.5f, 0.4f)
}

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Navegación automática al terminar Splash
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION)
        onSplashFinished()
    }

    // Aparición del logotipo
    val logoAlpha = remember {
        Animatable(0f)
    }

    val logoScale = remember {
        Animatable(0.92f)
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Posición animada del brillo turquesa
    // Las coordenadas están normalizadas de 0 a 1.
    val glowX = remember {
        Animatable(0.32f)
    }

    val glowY = remember {
        Animatable(0.34f)
    }

    // Historial de posiciones visitadas
    val positionHistory = remember {
        mutableListOf(
            Offset(0.32f, 0.34f)
        )
    }

    // Movimiento ambiental continuo
    LaunchedEffect(Unit) {
        while (true) {
            val nextPosition = nextGlowPosition(
                positionHistory
            )

            // Ejecutamos las dos animaciones en paralelo
            kotlinx.coroutines.coroutineScope {
                launch {
                    glowX.animateTo(
                        targetValue = nextPosition.x,
                        animationSpec = tween(
                            durationMillis = GLOW_MOVE_DURATION,
                            easing = EaseInOutSine
                        )
                    )
                }

                launch {
                    glowY.animateTo(
                        targetValue = nextPosition.y,
                        animationSpec = tween(
                            durationMillis = GLOW_MOVE_DURATION,
                            easing = EaseInOutSine
                        )
                    )
                }
            }

            positionHistory.add(nextPosition)

            // Conservamos las últimas posiciones para
            // evitar que el brillo vuelva al mismo sector.
            if (positionHistory.size > 6) {
                positionHistory.removeAt(0)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(
        label = "SplashAnimation"
    )

    // Variación suave de intensidad
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Rotación del indicador de carga
    val loadingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = LinearEasing
            )
        ),
        label = "LoadingRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {

        // FONDO ANIMADO
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050B19),
                        Color(0xFF061728),
                        Color(0xFF080D20),
                        Color(0xFF090B1D)
                    )
                )
            )

            val glowCenter = Offset(
                x = size.width * glowX.value,
                y = size.height * glowY.value
            )

            val glowRadius = size.width * 0.95f

            // Brillo turquesa flotante
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF008D78).copy(
                            alpha = 0.60f * glowAlpha
                        ),
                        Color(0xFF005A57).copy(
                            alpha = 0.22f * glowAlpha
                        ),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = glowRadius
                ),
                center = glowCenter,
                radius = glowRadius
            )

            // Brillo violeta inferior
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF17153D).copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * 0.65f,
                        size.height * 0.88f
                    ),
                    radius = size.width * 0.85f
                ),
                center = Offset(
                    size.width * 0.65f,
                    size.height * 0.88f
                ),
                radius = size.width * 0.85f
            )
        }

        // LOGOTIPO CENTRAL
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-5).dp)
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.ic_splash_cube
                ),
                contentDescription = "Logo DeepSource",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(
                    id = R.drawable.ic_splash_text
                ),
                contentDescription = "DeepSource",
                modifier = Modifier
                    .width(112.dp)
                    .height(55.dp),
                contentScale = ContentScale.Fit
            )
        }

        // INDICADOR DE CARGA INFERIOR
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
                .size(18.dp)
        ) {
            drawArc(
                color = SplashTeal,
                startAngle = loadingRotation,
                sweepAngle = 75f,
                useCenter = false,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
