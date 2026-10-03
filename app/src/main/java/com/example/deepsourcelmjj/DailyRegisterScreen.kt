package com.example.deepsourcelmjj

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRegisterScreen(
    projectOptions: List<String>
) {
    var selectedProject by remember(projectOptions) { mutableStateOf(projectOptions.firstOrNull().orEmpty()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var hoursWorked by remember { mutableStateOf(4) }
    var taskDescription by remember { mutableStateOf("") }

    val currentDate = remember {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("es", "ES"))
        dateFormat.format(Date()).replaceFirstChar { it.uppercase() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Horas", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09121D))
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1DB98A),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color(0xFF09121D)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tarjeta de Fecha
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131E2C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = Color(0xFF1DB98A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Fecha del registro", color = Color(0xFF8E9BAE), fontSize = 12.sp)
                        Text(currentDate, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Seleccionar Proyecto
            Column {
                Text("Proyecto", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProject,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1DB98A),
                            unfocusedBorderColor = Color(0xFF1E2D3E),
                            focusedContainerColor = Color(0xFF131E2C),
                            unfocusedContainerColor = Color(0xFF131E2C)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF131E2C))
                    ) {
                        projectOptions.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project, color = Color.White) },
                                onClick = {
                                    selectedProject = project
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Cantidad de Horas
            Column {
                Text("Horas Dedicadas", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131E2C), shape = RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF1E2D3E), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (hoursWorked > 1) hoursWorked-- },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E2D3E))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Restar hora", tint = Color.White)
                    }

                    Text(
                        text = "$hoursWorked hrs",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { if (hoursWorked < 12) hoursWorked++ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1DB98A))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar hora", tint = Color.White)
                    }
                }
            }

            // Descripción de la Tarea
            Column {
                Text("Descripción de Actividades", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    placeholder = { Text("Ej: Reunión con cliente, refactorización...", color = Color(0xFF5A6978)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB98A),
                        unfocusedBorderColor = Color(0xFF1E2D3E),
                        focusedContainerColor = Color(0xFF131E2C),
                        unfocusedContainerColor = Color(0xFF131E2C)
                    ),
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón de Guardado
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Horas registradas en $selectedProject"
                        )
                    }
                    taskDescription = ""
                    hoursWorked = 4
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB98A))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}