package cl.duoc.deepsource.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.deepsource.R
import cl.duoc.deepsource.data.mock.MockData

// --------------------------------------------------
// Tema visual de la pantalla
// --------------------------------------------------

private object RegisterTheme {
    val DarkGradientTop = Color(0xFF0D1B2A)
    val DarkGradientCenter = Color(0xFF1B263B)
    val DarkGradientBottom = Color(0xFF0D1117)

    val CardBackground = Color(0xFF161F2E).copy(alpha = 0.85f)
    val CardBorder = Color(0xFF28354A)
    val InputBackground = Color(0xFF1E2A3A)

    val AccentTeal = Color(0xFF1ABC9C)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
}

private data class SelectedProjectData(
    val name: String,
    val clientAndType: String,
    val iconRes: Int,
    val accentColor: Color
)

// --------------------------------------------------
// Pantalla principal de registro de horas
// --------------------------------------------------

@Composable
fun RegisterTimeScreen(
    projectId: String = "project-1",
    onProjects: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val selectedProject = MockData.projects.firstOrNull {
        it.id == projectId
    } ?: MockData.projects.firstOrNull()

    var selectedUser by remember {
        mutableStateOf("Juan Pérez")
    }

    var selectedUserEmail by remember {
        mutableStateOf("juan@duoc.cl")
    }

    var userDropdownExpanded by remember {
        mutableStateOf(false)
    }

    var selectedDate by remember {
        mutableStateOf("mié, 28 oct 2026")
    }

    var selectedHours by remember {
        mutableStateOf("00")
    }

    var selectedMinutes by remember {
        mutableStateOf("00")
    }

    var taskName by remember {
        mutableStateOf("")
    }

    var hoursDropdownExpanded by remember {
        mutableStateOf(false)
    }

    var minutesDropdownExpanded by remember {
        mutableStateOf(false)
    }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    val hoursList = (0..12).map {
        it.toString().padStart(2, '0')
    }

    val minutesList = listOf("00", "15", "30", "45")

    // Si no hay proyectos, mostrar un mensaje en vez de fallar.
    if (selectedProject == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RegisterTheme.DarkGradientBottom)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No hay proyectos disponibles",
                    color = RegisterTheme.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onProjects) {
                    Text("Volver a proyectos")
                }
            }
        }
        return
    }

    val currentProject = SelectedProjectData(
        name = selectedProject.name,
        clientAndType =
            "${selectedProject.client} · ${selectedProject.description}",
        iconRes = selectedProject.iconRes ?: R.drawable.ic_icono_1,
        accentColor = when (selectedProject.iconRes) {
            R.drawable.ic_icono_1 -> Color(0xFFEF4444)
            R.drawable.ic_icono_2 -> Color(0xFFEAB308)
            R.drawable.ic_icono_3 -> Color(0xFF06B6D4)
            R.drawable.ic_icono_4 -> Color(0xFF8B5CF6)
            else -> RegisterTheme.AccentTeal
        }
    )

    // --------------------------------------------------
    // Fondo y estructura principal
    // --------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RegisterTheme.DarkGradientTop,
                        RegisterTheme.DarkGradientCenter,
                        RegisterTheme.DarkGradientBottom
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                RegisterBottomNavigationBar(
                    selectedTab = "Registro",
                    onProjectsClick = onProjects,
                    onRegisterClick = {},
                    onProfileClick = onProfile
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. Barra superior

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = R.drawable.deepsource_logo
                        ),
                        contentDescription = "Logo DeepSource",
                        modifier = Modifier
                            .height(44.dp)
                            .width(140.dp),
                        contentScale = ContentScale.Fit
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                RegisterTheme.AccentTeal.copy(alpha = 0.15f),
                                RoundedCornerShape(50)
                            )
                            .border(
                                1.dp,
                                RegisterTheme.AccentTeal.copy(alpha = 0.4f),
                                RoundedCornerShape(50)
                            )
                            .padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = RegisterTheme.AccentTeal,
                                modifier = Modifier.size(16.dp)
                            )

                            Text(
                                text = "3 h 30 min hoy",
                                color = RegisterTheme.AccentTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 2. Encabezado

                Column {
                    Text(
                        text = "NUEVO REGISTRO",
                        color = RegisterTheme.AccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Registra tus horas",
                        color = RegisterTheme.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 3. Usuario

                Column {
                    Text(
                        text = "Usuario",
                        color = RegisterTheme.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    RegisterTheme.CardBackground,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    RegisterTheme.CardBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    userDropdownExpanded = true
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(0xFF164E5B),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (
                                            selectedUser.contains("Marina")
                                        ) "MS" else "JP",
                                        color = RegisterTheme.AccentTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = selectedUser,
                                        color = RegisterTheme.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = selectedUserEmail,
                                        color = RegisterTheme.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector =
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = RegisterTheme.TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = userDropdownExpanded,
                            onDismissRequest = {
                                userDropdownExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Juan Pérez (juan@duoc.cl)")
                                },
                                onClick = {
                                    selectedUser = "Juan Pérez"
                                    selectedUserEmail = "juan@duoc.cl"
                                    userDropdownExpanded = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Marina Soto (marina.soto@deepsource.cl)"
                                    )
                                },
                                onClick = {
                                    selectedUser = "Marina Soto"
                                    selectedUserEmail =
                                        "marina.soto@deepsource.cl"
                                    userDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 4. Proyecto seleccionado

                Column {
                    Text(
                        text = "Proyecto",
                        color = RegisterTheme.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                RegisterTheme.CardBackground,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                RegisterTheme.AccentTeal,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onProjects()
                            }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "PROYECTO SELECCIONADO",
                                color = RegisterTheme.AccentTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                currentProject.accentColor
                                                    .copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                currentProject.accentColor
                                                    .copy(alpha = 0.5f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(
                                                id = currentProject.iconRes
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = currentProject.name,
                                            color = RegisterTheme.TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text =
                                                currentProject.clientAndType,
                                            color =
                                                RegisterTheme.TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = RegisterTheme.TextSecondary
                                )
                            }
                        }
                    }
                }

                // 5. Fecha

                Column {
                    Text(
                        text = "Fecha",
                        color = RegisterTheme.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                RegisterTheme.CardBackground,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                RegisterTheme.CardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = RegisterTheme.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = selectedDate,
                                color = RegisterTheme.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = RegisterTheme.TextSecondary
                        )
                    }
                }

                // 6. Selección de horas y minutos

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Horas

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Horas",
                            color = RegisterTheme.TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        RegisterTheme.CardBackground,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        RegisterTheme.CardBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        hoursDropdownExpanded = true
                                    }
                                    .padding(14.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedHours,
                                    color = RegisterTheme.TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Icon(
                                    imageVector =
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = RegisterTheme.TextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = hoursDropdownExpanded,
                                onDismissRequest = {
                                    hoursDropdownExpanded = false
                                }
                            ) {
                                hoursList.forEach { hour ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("$hour h")
                                        },
                                        onClick = {
                                            selectedHours = hour
                                            hoursDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Minutos

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Minutos",
                            color = RegisterTheme.TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        RegisterTheme.CardBackground,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        1.dp,
                                        RegisterTheme.CardBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        minutesDropdownExpanded = true
                                    }
                                    .padding(14.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically,
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedMinutes,
                                    color = RegisterTheme.TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Icon(
                                    imageVector =
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = RegisterTheme.TextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = minutesDropdownExpanded,
                                onDismissRequest = {
                                    minutesDropdownExpanded = false
                                }
                            ) {
                                minutesList.forEach { min ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("$min min")
                                        },
                                        onClick = {
                                            selectedMinutes = min
                                            minutesDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Duración del registro

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            RegisterTheme.CardBackground,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            RegisterTheme.AccentTeal,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Duración del registro",
                                color = RegisterTheme.TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text =
                                    "$selectedHours h $selectedMinutes min",
                                color = RegisterTheme.TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    RegisterTheme.AccentTeal,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = RegisterTheme.TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // 8. Nombre de la tarea

                Column {
                    Text(
                        text = "Nombre de la tarea",
                        color = RegisterTheme.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = taskName,
                        onValueChange = {
                            taskName = it
                            validationMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                text =
                                    "Ej. Implementación de login y navegación...",
                                color = RegisterTheme.TextSecondary
                                    .copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = RegisterTheme.TextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor =
                                RegisterTheme.InputBackground,
                            unfocusedContainerColor =
                                RegisterTheme.InputBackground,
                            focusedBorderColor =
                                RegisterTheme.AccentTeal,
                            unfocusedBorderColor =
                                RegisterTheme.CardBorder,
                            focusedTextColor =
                                RegisterTheme.TextPrimary,
                            unfocusedTextColor =
                                RegisterTheme.TextPrimary
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 9. Botón Guardar registro

                Button(
                    onClick = {
                        val hours =
                            selectedHours.toIntOrNull() ?: 0

                        val minutes =
                            selectedMinutes.toIntOrNull() ?: 0

                        val totalMinutes = hours * 60 + minutes

                        when {
                            totalMinutes <= 0 -> {
                                validationMessage =
                                    "Selecciona una duración mayor que cero."
                            }

                            taskName.isBlank() -> {
                                validationMessage =
                                    "Ingresa el nombre de la tarea."
                            }

                            else -> {
                                try {
                                    MockData.addTimeEntry(
                                        projectId = selectedProject.id,
                                        durationMinutes = totalMinutes,
                                        date = selectedDate,
                                        note = taskName.trim()
                                    )

                                    validationMessage = null
                                    onSaveSuccess()
                                    onProjects()

                                } catch (e: IllegalArgumentException) {
                                    validationMessage =
                                        e.message
                                            ?: "No se pudo guardar el registro."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RegisterTheme.AccentTeal,
                        contentColor = RegisterTheme.TextPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "GUARDAR REGISTRO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Mensaje de validación

                validationMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --------------------------------------------------
// Navegación inferior
// --------------------------------------------------

@Composable
private fun RegisterBottomNavigationBar(
    selectedTab: String,
    onProjectsClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RegisterBottomNavItem(
                icon = Icons.Default.GridView,
                label = "Proyectos",
                isSelected = selectedTab == "Proyectos",
                onClick = onProjectsClick
            )

            RegisterBottomNavItem(
                icon = Icons.Default.Schedule,
                label = "Registro",
                isSelected = selectedTab == "Registro",
                onClick = onRegisterClick
            )

            RegisterBottomNavItem(
                icon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedTab == "Perfil",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun RegisterBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) {
        RegisterTheme.AccentTeal
    } else {
        RegisterTheme.TextSecondary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSelected) {
                        RegisterTheme.AccentTeal.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}