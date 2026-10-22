package cl.duoc.deepsource.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duoc.deepsource.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF080D1C)
private val SplashTeal = Color(0xFF42C7B8)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animación suave de aparición (Fade-In)
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        delay(1000) // Permanece visible 1 segundo adicional
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        // Destellos de luz azul/turquesa ambientales idénticos al resto de la app
        SplashAmbientBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .padding(horizontal = 32.dp)
        ) {
            // 1. Icono del Cubo (icono.png)
            Image(
                painter = painterResource(id = R.drawable.ic_splash_cube),
                contentDescription = "Icono DeepSource",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Logotipo con Nombre (deepsource.png)
            Image(
                painter = painterResource(id = R.drawable.ic_splash_text),
                contentDescription = "DeepSource",
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Indicador de carga turquesa característico de DeepSource
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = SplashTeal,
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
private fun SplashAmbientBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4520C5B2), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.35f),
                radius = size.width * 0.85f
            ),
            radius = size.width * 0.85f,
            center = Offset(size.width * 0.5f, size.height * 0.35f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4C5233C3), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.70f),
                radius = size.width * 0.90f
            ),
            radius = size.width * 0.90f,
            center = Offset(size.width * 0.5f, size.height * 0.70f)
        )
    }
}