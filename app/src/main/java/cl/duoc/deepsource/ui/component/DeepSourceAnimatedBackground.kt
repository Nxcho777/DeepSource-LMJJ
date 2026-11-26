
package cl.duoc.deepsource.ui.components

import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF080D1C
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF1C327C
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF209479
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF25D6C0
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF5264D9
import cl.duoc.deepsource.ui.theme.DeepSourceColor_FF8057D6

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val AuroraBackground = DeepSourceColor_FF080D1C

private val AuroraColors = listOf(
    DeepSourceColor_FF1C327C,
    DeepSourceColor_FF209479,
    DeepSourceColor_FF25D6C0,
    DeepSourceColor_FF5264D9,
    DeepSourceColor_FF8057D6
)

private class AuroraLayer(
    initialX: Float,
    initialY: Float,
    initialRotation: Float,
    initialIntensity: Float,
    initialWidth: Float,
    val color: Color
) {
    val x = Animatable(initialX)
    val y = Animatable(initialY)
    val rotation = Animatable(initialRotation)
    val intensity = Animatable(initialIntensity)
    val width = Animatable(initialWidth)
    val wave = Animatable(0f)
}

@Composable
fun DeepSourceAnimatedBackground(
    modifier: Modifier = Modifier
) {
    val auroras = remember {
        val random = Random(System.nanoTime())

        List(5) { index ->
            AuroraLayer(
                initialX = random.nextFloat(),
                initialY = (index + 0.5f) / 5f,
                initialRotation = random.nextFloat() * 50f - 25f,
                initialIntensity = 0.65f + random.nextFloat() * 0.35f,
                initialWidth = 0.65f + random.nextFloat() * 0.45f,
                color = AuroraColors[index]
            )
        }
    }

    if (cl.duoc.deepsource.ui.animation.motionDurationMillis(1) > 0) {
        auroras.forEach { aurora ->
            AuroraLayerAnimation(aurora)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        drawRect(AuroraBackground)

        auroras.forEach { aurora ->
            drawAurora(aurora)
        }
    }
}

@Composable
private fun AuroraLayerAnimation(
    layer: AuroraLayer
) {
    LaunchedEffect(layer) {
        val random = Random(System.nanoTime())

        launch {
            while (true) {
                layer.x.animateTo(
                    targetValue = random.nextFloat(),
                    animationSpec = tween(
                        durationMillis = random.nextInt(6500, 14000),
                        easing = LinearEasing
                    )
                )
            }
        }

        launch {
            while (true) {
                layer.y.animateTo(
                    targetValue = 0.08f + random.nextFloat() * 0.84f,
                    animationSpec = tween(
                        durationMillis = random.nextInt(7500, 16000),
                        easing = LinearEasing
                    )
                )
            }
        }

        launch {
            while (true) {
                layer.rotation.animateTo(
                    targetValue = random.nextFloat() * 70f - 35f,
                    animationSpec = tween(
                        durationMillis = random.nextInt(9000, 18000),
                        easing = LinearEasing
                    )
                )
            }
        }

        launch {
            while (true) {
                layer.intensity.animateTo(
                    targetValue = 0.55f + random.nextFloat() * 0.45f,
                    animationSpec = tween(
                        durationMillis = random.nextInt(4000, 9500),
                        easing = LinearEasing
                    )
                )
            }
        }

        launch {
            while (true) {
                layer.width.animateTo(
                    targetValue = 0.55f + random.nextFloat() * 0.65f,
                    animationSpec = tween(
                        durationMillis = random.nextInt(7000, 14000),
                        easing = LinearEasing
                    )
                )
            }
        }

        launch {
            while (true) {
                layer.wave.animateTo(
                    targetValue = layer.wave.value + (2f * PI).toFloat(),
                    animationSpec = tween(
                        durationMillis = random.nextInt(9000, 15000),
                        easing = LinearEasing
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawAurora(
    layer: AuroraLayer
) {
    val centerX = size.width * layer.x.value
    val centerY = size.height * layer.y.value

    val bandWidth = size.width * layer.width.value
    val bandHeight = size.height * 0.45f

    val intensity = layer.intensity.value
    val phase = layer.wave.value

    rotate(
        degrees = layer.rotation.value,
        pivot = Offset(centerX, centerY)
    ) {
        val path = Path()

        val segments = 40
        val startX = centerX - bandWidth / 2f

        for (i in 0..segments) {
            val progress = i.toFloat() / segments
            val x = startX + progress * bandWidth

            val waveY =
                sin(progress * PI * 2.0 + phase).toFloat() *
                        bandHeight * 0.12f +
                        sin(progress * PI * 4.0 - phase * 0.6f).toFloat() *
                        bandHeight * 0.06f

            val y = centerY + waveY

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        for (i in segments downTo 0) {
            val progress = i.toFloat() / segments
            val x = startX + progress * bandWidth

            val waveY =
                sin(progress * PI * 2.0 + phase).toFloat() *
                        bandHeight * 0.12f +
                        sin(progress * PI * 4.0 - phase * 0.6f).toFloat() *
                        bandHeight * 0.06f

            val y = centerY + waveY + bandHeight

            path.lineTo(x, y)
        }

        path.close()

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    layer.color.copy(alpha = 0.0f),
                    layer.color.copy(alpha = 0.35f * intensity),
                    layer.color.copy(alpha = 0.18f * intensity),
                    Color.Transparent
                ),
                startY = centerY - bandHeight * 0.2f,
                endY = centerY + bandHeight
            )
        )
    }
}
