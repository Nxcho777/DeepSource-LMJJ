package com.example.deepsourcelmjj

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.Normalizer

// Función para remover acentos/tildes y convertir a minúsculas
fun String.normalizeForSearch(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return Regex("\\p{InCombiningDiacriticalMarks}+")
        .replace(normalized, "")
        .lowercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    currentUser: AppUser,
    onProjectClick: (ProjectItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val allProjects = remember(currentUser.userId) {
        ProjectRepository.getProjectsForUser(currentUser.userId)
    }

    // Búsqueda flexible e informal (sin importar tildes ni mayúsculas/minúsculas)
    val filteredProjects = remember(searchQuery, allProjects) {
        val queryNormalized = searchQuery.normalizeForSearch()
        if (queryNormalized.isBlank()) {
            allProjects
        } else {
            allProjects.filter { project ->
                project.title.normalizeForSearch().contains(queryNormalized) ||
                        project.client.normalizeForSearch().contains(queryNormalized)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proyectos Activos", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09121D)),
                actions = {
                    IconButton(onClick = { /* TODO: Accion de filtro adicional */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser.profile == UserRole.LEADER) {
                FloatingActionButton(
                    onClick = { /* TODO: Nuevo Proyecto */ },
                    containerColor = Color(0xFF1DB98A),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
                }
            }
        },
        containerColor = Color(0xFF09121D)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar proyecto o cliente...", color = Color(0xFF5A6978)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1DB98A),
                    unfocusedBorderColor = Color(0xFF1E2D3E),
                    focusedContainerColor = Color(0xFF131E2C),
                    unfocusedContainerColor = Color(0xFF131E2C)
                )
            )

            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron proyectos",
                        color = Color(0xFF8E9BAE),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectCard(project = project, onClick = { onProjectClick(project) })
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: ProjectItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131E2C)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (project.isCompleted) Color(0xFF1E2D3E) else Color(0xFF1DB98A).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = project.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = if (project.isCompleted) Color(0xFF1E2D3E) else Color(0xFF1DB98A).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = project.status,
                        color = if (project.isCompleted) Color(0xFF8E9BAE) else Color(0xFF1DB98A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(text = project.client, color = Color(0xFF8E9BAE), fontSize = 13.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${project.teamCount} miembros", color = Color(0xFF8E9BAE), fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = project.totalHours, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(text = project.lastUpdate, color = Color(0xFF5A6978), fontSize = 11.sp)
            }
        }
    }
}