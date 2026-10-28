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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
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

// --- Tema local para la pantalla de Perfil ---
private object ProfileTheme {
    val DarkGradientTop = Color(0xFF0D1B2A)
    val DarkGradientCenter = Color(0xFF1B263B)
    val DarkGradientBottom = Color(0xFF0D1117)

    val CardBackground = Color(0xFF161F2E).copy(alpha = 0.85f)
    val CardBorder = Color(0xFF28354A)

    val AccentTeal = Color(0xFF1ABC9C)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
    val DividerColor = Color(0xFF233144)
}

private data class FinishedProject(
    val title: String,
    val subtitle: String
)

@Composable
fun ProfileScreen(
    onProjectsClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    val finishedProjectsList = listOf(
        FinishedProject("Proyecto Atlas", "Deep Source · Producto"),
        FinishedProject("App Boreal", "Nébula Labs · Mobile"),
        FinishedProject("Portal Nova", "Nova Retail · Web")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ProfileTheme.DarkGradientTop,
                        ProfileTheme.DarkGradientCenter,
                        ProfileTheme.DarkGradientBottom
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                ProfileBottomNavigationBar(
                    selectedTab = "Perfil",
                    onProjectsClick = onProjectsClick,
                    onRegisterClick = onRegisterClick,
                    onProfileClick = { }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. Top Bar (Logo + Título Perfil)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.deepsource_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(44.dp)
                            .width(140.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "Perfil",
                        color = ProfileTheme.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Tarjeta del Usuario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileTheme.CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = listOf(ProfileTheme.CardBorder, ProfileTheme.CardBorder)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color(0xFF164E5B), CircleShape)
                                    .border(1.dp, ProfileTheme.AccentTeal.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MS",
                                    color = ProfileTheme.AccentTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(
                                    text = "Marina Soto",
                                    color = ProfileTheme.TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "juan@duoc.cl",
                                    color = ProfileTheme.TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileTheme.AccentTeal,
                                contentColor = ProfileTheme.TextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Editar perfil",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Menú de Opciones y Configuración
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ProfileTheme.CardBackground),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = listOf(ProfileTheme.CardBorder, ProfileTheme.CardBorder)
                        )
                    )
                ) {
                    Column {
                        // Opción 1: Editar perfil
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            title = "Editar perfil",
                            subtitle = "Modifica tus datos personales",
                            onClick = { }
                        )

                        HorizontalDivider(color = ProfileTheme.DividerColor, thickness = 1.dp)

                        // Opción 2: Privacidad
                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            title = "Privacidad",
                            subtitle = "Configura la privacidad de tu cuenta",
                            onClick = { }
                        )

                        HorizontalDivider(color = ProfileTheme.DividerColor, thickness = 1.dp)

                        // Opción 3: Notificaciones (Switch)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF1E2A3A), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = ProfileTheme.AccentTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Notificaciones",
                                        color = ProfileTheme.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Configura tus notificaciones",
                                        color = ProfileTheme.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ProfileTheme.AccentTeal,
                                    uncheckedThumbColor = ProfileTheme.TextSecondary,
                                    uncheckedTrackColor = Color(0xFF1E2A3A)
                                )
                            )
                        }

                        HorizontalDivider(color = ProfileTheme.DividerColor, thickness = 1.dp)

                        // Opción 4: Proyectos finalizados (Badge 3)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF1E2A3A), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = ProfileTheme.AccentTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Proyectos finalizados",
                                        color = ProfileTheme.TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Consulta tus proyectos completados",
                                        color = ProfileTheme.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color(0xFF164E5B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "3",
                                    color = ProfileTheme.AccentTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Sección Proyectos Finalizados
                Text(
                    text = "Proyectos finalizados",
                    color = ProfileTheme.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                finishedProjectsList.forEach { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ProfileTheme.CardBackground),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                colors = listOf(ProfileTheme.CardBorder, ProfileTheme.CardBorder)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(ProfileTheme.AccentTeal.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, ProfileTheme.AccentTeal.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ProfileTheme.AccentTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = project.title,
                                        color = ProfileTheme.TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = project.subtitle,
                                        color = ProfileTheme.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Chip Finalizado
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF0F3843), RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✓  Finalizado",
                                    color = ProfileTheme.AccentTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E2A3A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileTheme.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = ProfileTheme.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = ProfileTheme.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ProfileTheme.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- Navigation Bar ---
@Composable
private fun ProfileBottomNavigationBar(
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
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileBottomNavItem(
                icon = Icons.Default.GridView,
                label = "Proyectos",
                isSelected = selectedTab == "Proyectos",
                onClick = onProjectsClick
            )

            ProfileBottomNavItem(
                icon = Icons.Default.Schedule,
                label = "Registro",
                isSelected = selectedTab == "Registro",
                onClick = onRegisterClick
            )

            ProfileBottomNavItem(
                icon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedTab == "Perfil",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun ProfileBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) ProfileTheme.AccentTeal else ProfileTheme.TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) ProfileTheme.AccentTeal.copy(alpha = 0.15f) else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
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
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}