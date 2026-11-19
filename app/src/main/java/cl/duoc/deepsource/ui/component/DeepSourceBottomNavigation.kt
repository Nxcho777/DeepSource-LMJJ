package cl.duoc.deepsource.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI


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

    var dragFraction by remember {
        mutableStateOf<Float?>(null)
    }

    var barWidth by remember {
        mutableFloatStateOf(1f)
    }

    var dragFlow by remember {
        mutableFloatStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    // Opción D: iluminación pulsante del contorno.
    val glowTransition = rememberInfiniteTransition(
        label = "Glass border glow"
    )

    val glowPulse by glowTransition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glass border pulse"
    )

    val bubblePosition = remember {
        Animatable(target)
    }

    val bubbleVisibility = remember {
        Animatable(1f)
    }

    // Flujo sincronizado: la forma cambia durante TODO el recorrido.
    val liquidFlow = remember { Animatable(0f) }
    val travelProgress = remember { Animatable(1f) }
    var travelDirection by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(target) {
        val start = bubblePosition.value
        if (kotlin.math.abs(start - target) > 0.0001f) {
            travelDirection = if (target > start) 1f else -1f
            travelProgress.snapTo(0f)
            liquidFlow.snapTo(0f)
            coroutineScope {
                launch {
                    bubblePosition.animateTo(
                        targetValue = target,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    travelProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                    // Breve oscilación de asentamiento al llegar.
                    liquidFlow.snapTo(-travelDirection * 0.20f)
                    liquidFlow.animateTo(
                        0f,
                        spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            )
            .height(94.dp)
            .onSizeChanged {
                barWidth = it.width.toFloat()
                    .coerceAtLeast(1f)
            }
            .pointerInput(activeTab) {

                fun fraction(x: Float): Float {
                    val inset = 8.dp.toPx()

                    return (
                            (x - inset) /
                                    (barWidth - 2f * inset)
                                        .coerceAtLeast(1f)
                            ).coerceIn(
                            1f / 6f,
                            5f / 6f
                        )
                }

                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragFraction = fraction(offset.x)
                        dragFlow = 0f
                    },

                    onHorizontalDrag = { change, dragAmount ->
                        dragFraction = fraction(
                            change.position.x
                        )

                        val normalizedMovement = (
                                dragAmount /
                                        (barWidth * 0.045f)
                                            .coerceAtLeast(1f)
                                ).coerceIn(-1f, 1f)

                        dragFlow = (
                                dragFlow * 0.55f +
                                        normalizedMovement * 0.45f
                                ).coerceIn(-1f, 1f)

                        change.consume()
                    },

                    onDragEnd = {
                        val released = dragFraction ?: target

                        val selected = when {
                            released < 1f / 3f ->
                                DeepSourceTab.Projects

                            released < 2f / 3f ->
                                DeepSourceTab.Register

                            else ->
                                DeepSourceTab.Profile
                        }

                        scope.launch {
                            bubblePosition.snapTo(released)
                            dragFlow = 0f
                            dragFraction = null

                            if (selected == activeTab) {
                                bubblePosition.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            } else {
                                when (selected) {
                                    DeepSourceTab.Projects ->
                                        onProjects()

                                    DeepSourceTab.Register ->
                                        onRegister()

                                    DeepSourceTab.Profile ->
                                        onProfile()
                                }
                            }
                        }
                    },

                    onDragCancel = {
                        val released = dragFraction ?: target

                        scope.launch {
                            bubblePosition.snapTo(released)
                            dragFlow = 0f
                            dragFraction = null

                            bubblePosition.animateTo(
                                targetValue = target,
                                animationSpec = spring()
                            )
                        }
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            val left = 2.dp.toPx()
            val right = w - left
            val top = 4.dp.toPx()
            val bottom = h - 16.dp.toPx()
            val radius = 25.dp.toPx()

            val horizontalPadding = 8.dp.toPx()
            val contentWidth = w - horizontalPadding * 2f

            val centerX = horizontalPadding +
                    contentWidth * (
                    dragFraction ?: bubblePosition.value
                    )

            val cornerClearance = 2.dp.toPx()
            val safeLeft = left + radius + cornerClearance
            val safeRight = right - radius - cornerClearance

            // Dirección del flujo líquido.
            val flow = if (dragFraction != null) {
                dragFlow
            } else {
                // Crece en la mitad del recorrido y se reduce al llegar.
                // No depende de una animación que termine antes que la posición.
                val movingWave = sin(
                    (travelProgress.value * PI).toFloat()
                ) * travelDirection * 0.90f
                (movingWave + liquidFlow.value).coerceIn(-1f, 1f)
            }

            val intensity = abs(flow)

            // Estiramiento de la onda.
            val desiredHalfWidth =
                65.dp.toPx() +
                        27.dp.toPx() * intensity

            val halfWidth = min(
                desiredHalfWidth,
                min(
                    centerX - safeLeft,
                    safeRight - centerX
                ).coerceAtLeast(0f)
            )

            val directionalStretch =
                halfWidth * 0.34f * flow

            val bumpStart = centerX - halfWidth
            val bumpEnd = centerX + halfWidth

            val bubbleHeight = (
                    9.dp.toPx() +
                            3.dp.toPx() * intensity
                    ) * bubbleVisibility.value

            val bumpBottom = bottom + bubbleHeight

            val liquidCenterX = (
                    centerX + directionalStretch
                    ).coerceIn(
                    bumpStart,
                    bumpEnd
                )

            // Curva Bézier que cambia de forma
            // según la dirección del movimiento.
            fun Path.addSmoothBubble() {
                val leftSpan =
                    liquidCenterX - bumpStart

                val rightSpan =
                    bumpEnd - liquidCenterX

                val leftControlHeight = (
                        bubbleHeight *
                                (0.78f - flow * 0.18f)
                        ).coerceAtLeast(0f)

                val rightControlHeight = (
                        bubbleHeight *
                                (0.78f + flow * 0.18f)
                        ).coerceAtLeast(0f)

                cubicTo(
                    bumpEnd - rightSpan * 0.48f,
                    bottom + rightControlHeight * 0.10f,

                    liquidCenterX + rightSpan * 0.43f,
                    bumpBottom,

                    liquidCenterX,
                    bumpBottom
                )

                cubicTo(
                    liquidCenterX - leftSpan * 0.43f,
                    bumpBottom,

                    bumpStart + leftSpan * 0.48f,
                    bottom + leftControlHeight * 0.10f,

                    bumpStart,
                    bottom
                )
            }

            // Silueta de la barra.
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

            // Fondo original Liquid Glass.
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

            // Opción D: halo luminoso.
            val glowBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF24D5D1),
                    Color(0xFF49DFFF),
                    Color(0xFF45C5F1),
                    Color(0xFF24D5D1)
                ),
                startX = left,
                endX = right
            )

            drawPath(
                path = glassPath,
                brush = glowBrush,
                alpha = 0.08f * glowPulse,
                style = Stroke(
                    width = 11.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = glassPath,
                brush = glowBrush,
                alpha = 0.16f * glowPulse,
                style = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = glassPath,
                brush = glowBrush,
                alpha = 0.45f * glowPulse,
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
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
                style = Stroke(
                    width = 1.4.dp.toPx()
                )
            )

            // Reflejo de la onda.
            val bubbleHighlight = Path().apply {
                moveTo(bumpEnd, bottom)
                addSmoothBubble()
            }

            if (bubbleVisibility.value > 0.01f) {
                drawPath(
                    path = bubbleHighlight,
                    color = GlassTeal.copy(
                        alpha = 0.10f *
                                bubbleVisibility.value
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

            // Liquid Glass 3.2 — estelas separadas de la onda principal.
            // El brillo nace detras del movimiento y desaparece al asentarse.
            // No altera la silueta del vidrio ni la navegacion.
            if (intensity > 0.025f && halfWidth > 1f) {
                val strength = ((intensity - 0.025f) / 0.65f)
                    .coerceIn(0f, 1f)
                val direction = if (flow >= 0f) 1f else -1f

                for (index in 0..1) {
                    // Distancias deliberadamente mayores que en 3.1:
                    // las lineas no quedan ocultas bajo el contorno principal.
                    val lag = (12f + index * 11f).dp.toPx() * strength
                    val lift = (2.8f + index * 2.4f).dp.toPx() * strength
                    val trailStart = bumpStart - direction * lag
                    val trailEnd = bumpEnd - direction * lag
                    val trailMiddle = liquidCenterX - direction * lag
                    val trailBottom = bottom + bubbleHeight *
                        (0.72f - index * 0.20f)

                    val trailPath = Path().apply {
                        moveTo(trailEnd, bottom - lift)
                        cubicTo(
                            trailEnd - (trailEnd - trailMiddle) * 0.48f,
                            bottom - lift,
                            trailMiddle + (trailEnd - trailMiddle) * 0.44f,
                            trailBottom,
                            trailMiddle,
                            trailBottom
                        )
                        cubicTo(
                            trailMiddle - (trailMiddle - trailStart) * 0.44f,
                            trailBottom,
                            trailStart + (trailMiddle - trailStart) * 0.48f,
                            bottom - lift,
                            trailStart,
                            bottom - lift
                        )
                    }

                    val trailBrush = Brush.horizontalGradient(
                        colors = if (direction > 0f) listOf(
                            Color.Transparent,
                            GlassBlue.copy(alpha = 0.50f),
                            GlassTeal.copy(alpha = 0.90f),
                            Color(0xFFC7FFFF).copy(alpha = 0.65f)
                        ) else listOf(
                            Color(0xFFC7FFFF).copy(alpha = 0.65f),
                            GlassTeal.copy(alpha = 0.90f),
                            GlassBlue.copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        startX = trailStart.coerceAtMost(trailEnd),
                        endX = trailStart.coerceAtLeast(trailEnd)
                    )
                    // Halo suave, seguido de un filamento nitido.
                    drawPath(
                        path = trailPath,
                        color = GlassTeal,
                        alpha = strength * (if (index == 0) 0.18f else 0.11f),
                        style = Stroke(
                            width = (if (index == 0) 4.dp else 3.dp).toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                    drawPath(
                        path = trailPath,
                        brush = trailBrush,
                        alpha = strength * (if (index == 0) 0.90f else 0.66f),
                        style = Stroke(
                            width = (if (index == 0) 1.5.dp else 1.1.dp).toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            // Reflejo superior original.
            val topHighlight = Path().apply {
                moveTo(
                    left + radius,
                    top + 2.dp.toPx()
                )

                lineTo(
                    right - radius,
                    top + 2.dp.toPx()
                )
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

        // Botones originales.
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
        targetValue = if (active) {
            GlassTeal
        } else {
            GlassMuted
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Liquid Glass Icon Tint"
    )

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource
        .collectIsPressedAsState()

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
                    cornerRadius = CornerRadius(
                        3.dp.toPx()
                    ),
                    style = stroke
                )

                drawLine(
                    color = tint,
                    start = Offset(
                        size.width * 0.32f,
                        size.height * 0.40f
                    ),
                    end = Offset(
                        size.width * 0.32f,
                        size.height * 0.65f
                    ),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = tint,
                    start = Offset(
                        size.width * 0.62f,
                        size.height * 0.35f
                    ),
                    end = Offset(
                        size.width * 0.62f,
                        size.height * 0.58f
                    ),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            DeepSourceTab.Register -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.37f,
                    style = stroke
                )

                drawLine(
                    color = tint,
                    start = Offset(
                        size.width / 2f,
                        size.height * 0.29f
                    ),
                    end = Offset(
                        size.width / 2f,
                        size.height * 0.52f
                    ),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = tint,
                    start = Offset(
                        size.width / 2f,
                        size.height * 0.52f
                    ),
                    end = Offset(
                        size.width * 0.67f,
                        size.height * 0.52f
                    ),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            DeepSourceTab.Profile -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.17f,
                    center = Offset(
                        size.width / 2f,
                        size.height * 0.34f
                    ),
                    style = stroke
                )

                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.36f,
                    center = Offset(
                        size.width / 2f,
                        size.height * 0.60f
                    ),
                    style = stroke
                )
            }
        }
    }
}
