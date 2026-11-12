
package cl.duoc.deepsource.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState

enum class DeepSourceTab {
    Projects,
    Register,
    Profile
}

private val GlassTeal = Color(0xFF42D9C5)
private val GlassMuted = Color(0xFF91A1BA)
private val GlassBlue = Color(0xFF3688C8)
private val GlassPurple = Color(0xFF8663DB)

@Composable
fun DeepSourceBottomNavigation(
    activeTab: DeepSourceTab,
    onProjects: () -> Unit,
    onRegister: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val target = when (activeTab) {
        DeepSourceTab.Projects -> 1f / 6f
        DeepSourceTab.Register -> 0.5f
        DeepSourceTab.Profile -> 5f / 6f
    }

    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var barWidth by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()

    val bubblePosition = remember {
        Animatable(target)
    }

    val bubbleVisibility = remember {
        Animatable(1f)
    }

    // Liquid Glass 2.0: comprimir, deslizar y recuperar la forma.
    // Las animaciones se cancelan correctamente si se pulsa otra pestaña.
    LaunchedEffect(target) {
        if (bubblePosition.value != target) {
            bubbleVisibility.animateTo(
                targetValue = 0.18f,
                animationSpec = tween(130, easing = FastOutSlowInEasing)
            )
            bubblePosition.animateTo(
                targetValue = target,
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            )
            bubbleVisibility.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // El arrastre sigue al dedo; al soltar se selecciona el tercio más cercano.
    // Los taps normales siguen siendo manejados por clickable en cada botón.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .height(94.dp)
            .onSizeChanged { barWidth = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(activeTab) {
                fun fraction(x: Float): Float {
                    val inset = 8.dp.toPx()
                    return ((x - inset) / (barWidth - 2f * inset).coerceAtLeast(1f))
                        .coerceIn(1f / 6f, 5f / 6f)
                }
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragFraction = fraction(offset.x)
                    },
                    onHorizontalDrag = { change, _ ->
                        dragFraction = fraction(change.position.x)
                        change.consume()
                    },
                    onDragEnd = {
                        val released = dragFraction ?: target
                        val selected = when {
                            released < 1f / 3f -> DeepSourceTab.Projects
                            released < 2f / 3f -> DeepSourceTab.Register
                            else -> DeepSourceTab.Profile
                        }
                        scope.launch {
                            bubblePosition.snapTo(released)
                            dragFraction = null
                            if (selected == activeTab) {
                                bubblePosition.animateTo(
                                    target,
                                    spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMedium)
                                )
                            } else {
                                when (selected) {
                                    DeepSourceTab.Projects -> onProjects()
                                    DeepSourceTab.Register -> onRegister()
                                    DeepSourceTab.Profile -> onProfile()
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        val released = dragFraction ?: target
                        scope.launch {
                            bubblePosition.snapTo(released)
                            dragFraction = null
                            bubblePosition.animateTo(target, spring())
                        }
                    }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val left = 2.dp.toPx()
            val right = w - left
            val top = 4.dp.toPx()
            val bottom = h - 16.dp.toPx()
            val radius = 25.dp.toPx()

            // Alineación exacta con el Row de botones.
            val horizontalPadding = 8.dp.toPx()
            val contentWidth = w - horizontalPadding * 2f

            val centerX =
                horizontalPadding +
                        contentWidth * (dragFraction ?: bubblePosition.value)

            val cornerClearance = 2.dp.toPx()
            val safeLeft = left + radius + cornerClearance
            val safeRight = right - radius - cornerClearance

            // Burbuja centrada y adaptada a las esquinas.
            val halfWidth = min(
                65.dp.toPx(),
                min(
                    centerX - safeLeft,
                    safeRight - centerX
                ).coerceAtLeast(0f)
            )

            val bumpStart = centerX - halfWidth
            val bumpEnd = centerX + halfWidth

            val bubbleHeight =
                9.dp.toPx() * bubbleVisibility.value

            val bumpBottom = bottom + bubbleHeight

            // Curva suave y simétrica.
            fun Path.addSmoothBubble() {
                val tangent = halfWidth * 0.5f

                cubicTo(
                    bumpEnd - tangent,
                    bottom,
                    centerX + tangent,
                    bumpBottom,
                    centerX,
                    bumpBottom
                )

                cubicTo(
                    centerX - tangent,
                    bumpBottom,
                    bumpStart + tangent,
                    bottom,
                    bumpStart,
                    bottom
                )
            }

            val glassPath = Path().apply {
                moveTo(left + radius, top)
                lineTo(right - radius, top)

                quadraticTo(
                    right, top,
                    right, top + radius
                )

                lineTo(right, bottom - radius)

                quadraticTo(
                    right, bottom,
                    right - radius, bottom
                )

                lineTo(bumpEnd, bottom)
                addSmoothBubble()
                lineTo(left + radius, bottom)

                quadraticTo(
                    left, bottom,
                    left, bottom - radius
                )

                lineTo(left, top + radius)

                quadraticTo(
                    left, top,
                    left + radius, top
                )

                close()
            }

            // Fondo de cristal.
            drawPath(
                path = glassPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE02A4965),
                        Color(0xDC10273F),
                        Color(0xE00A192E)
                    ),
                    startY = top,
                    endY = bumpBottom
                )
            )

            // Reflejos interiores.
            drawPath(
                path = glassPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        GlassTeal.copy(alpha = 0.14f),
                        Color.Transparent,
                        GlassBlue.copy(alpha = 0.10f),
                        GlassPurple.copy(alpha = 0.15f)
                    ),
                    startX = left,
                    endX = right
                )
            )

            // Contorno exterior.
            drawPath(
                path = glassPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xB7B8F5FF),
                        Color(0x664C9CB5),
                        Color(0x886E72C8)
                    ),
                    startY = top,
                    endY = bumpBottom
                ),
                style = Stroke(1.4.dp.toPx())
            )

            // Reflejo inferior de la burbuja.
            val bubbleHighlight = Path().apply {
                moveTo(bumpEnd, bottom)
                addSmoothBubble()
            }

            if (bubbleVisibility.value > 0.01f) {
                drawPath(
                    path = bubbleHighlight,
                    color = GlassTeal.copy(
                        alpha = 0.10f * bubbleVisibility.value
                    ),
                    style = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                drawPath(
                    path = bubbleHighlight,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GlassBlue.copy(alpha = 0.30f),
                            Color(0xFFD2FFFF),
                            GlassTeal,
                            GlassBlue.copy(alpha = 0.30f)
                        ),
                        startX = bumpStart,
                        endX = bumpEnd
                    ),
                    alpha = bubbleVisibility.value,
                    style = Stroke(
                        width = 1.7.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            // Reflejo superior.
            val topHighlight = Path().apply {
                moveTo(left + radius, top + 2.dp.toPx())
                lineTo(right - radius, top + 2.dp.toPx())
            }

            drawPath(
                path = topHighlight,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x77D6FAFF),
                        Color.Transparent
                    ),
                    startX = left,
                    endX = right
                ),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // Botones centrados en el cuerpo de la isla.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
                .height(74.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassItem(
                label = "Proyectos",
                tab = DeepSourceTab.Projects,
                activeTab = activeTab,
                onClick = onProjects,
                modifier = Modifier.weight(1f)
            )

            LiquidGlassItem(
                label = "Registro",
                tab = DeepSourceTab.Register,
                activeTab = activeTab,
                onClick = onRegister,
                modifier = Modifier.weight(1f)
            )

            LiquidGlassItem(
                label = "Perfil",
                tab = DeepSourceTab.Profile,
                activeTab = activeTab,
                onClick = onProfile,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LiquidGlassItem(
    label: String,
    tab: DeepSourceTab,
    activeTab: DeepSourceTab,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = tab == activeTab

    val tint by animateColorAsState(
        targetValue = if (active) GlassTeal else GlassMuted,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Liquid Glass Icon Tint"
    )

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "Liquid Glass press"
    )

    Column(
        modifier = modifier
            .height(51.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LiquidGlassGlyph(tab, tint)

        Spacer(Modifier.height(5.dp))

        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (active) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            }
        )
    }
}

@Composable
private fun LiquidGlassGlyph(
    tab: DeepSourceTab,
    tint: Color
) {
    Canvas(Modifier.size(21.dp)) {
        val stroke = Stroke(
            width = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        when (tab) {
            DeepSourceTab.Projects -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(
                        size.width * 0.12f,
                        size.height * 0.25f
                    ),
                    size = Size(
                        size.width * 0.76f,
                        size.height * 0.58f
                    ),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = stroke
                )

                drawLine(
                    tint,
                    Offset(size.width * 0.32f, size.height * 0.40f),
                    Offset(size.width * 0.32f, size.height * 0.65f),
                    1.8.dp.toPx(),
                    StrokeCap.Round
                )

                drawLine(
                    tint,
                    Offset(size.width * 0.62f, size.height * 0.35f),
                    Offset(size.width * 0.62f, size.height * 0.58f),
                    1.8.dp.toPx(),
                    StrokeCap.Round
                )
            }

            DeepSourceTab.Register -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.37f,
                    style = stroke
                )

                drawLine(
                    tint,
                    Offset(size.width / 2, size.height * 0.29f),
                    Offset(size.width / 2, size.height * 0.52f),
                    1.8.dp.toPx(),
                    StrokeCap.Round
                )

                drawLine(
                    tint,
                    Offset(size.width / 2, size.height * 0.52f),
                    Offset(size.width * 0.67f, size.height * 0.52f),
                    1.8.dp.toPx(),
                    StrokeCap.Round
                )
            }

            DeepSourceTab.Profile -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.17f,
                    center = Offset(
                        size.width / 2,
                        size.height * 0.34f
                    ),
                    style = stroke
                )

                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.36f,
                    center = Offset(
                        size.width / 2,
                        size.height * 0.60f
                    ),
                    style = stroke
                )
            }
        }
    }
}
