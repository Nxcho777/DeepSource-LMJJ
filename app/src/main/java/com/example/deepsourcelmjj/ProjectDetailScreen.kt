package com.example.deepsourcelmjj

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectTitle: String = "Proyecto",
    totalHours: String = "0 hrs",
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(projectTitle, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09121D))
            )
        },
        containerColor = Color(0xFF09121D)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131E2C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Resumen del Proyecto", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "Horas Totales", color = Color(0xFF8E9BAE), fontSize = 12.sp)
                                Text(text = totalHours, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Presupuesto Utilizado", color = Color(0xFF8E9BAE), fontSize = 12.sp)
                                Text(text = "68%", color = Color(0xFF1DB98A), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { 0.68f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF1DB98A),
                            trackColor = Color(0xFF1E2D3E)
                        )
                    }
                }
            }

            item {
                Text(text = "Integrantes del Equipo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            items(3) { index ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131E2C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1DB98A),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = if(index == 0) "MS" else if(index == 1) "CL" else "JP", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if(index == 0) "Marina Soto" else if(index == 1) "Carlos Lopez" else "Juan Perez",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if(index == 0) "Lead Developer · 64 hrs" else if(index == 1) "UX Designer · 40 hrs" else "Backend Dev · 80 hrs",
                                color = Color(0xFF8E9BAE),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}