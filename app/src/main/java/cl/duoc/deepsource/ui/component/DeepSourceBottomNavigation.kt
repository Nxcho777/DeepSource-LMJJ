
package cl.duoc.deepsource.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class DeepSourceTab {
    PROJECTS,
    REGISTER,
    PROFILE
}

private val NavigationBackground = Color(0xFF0C1428)
private val NavigationTeal = Color(0xFF29B5A6)
private val NavigationMuted = Color(0xFFA8B5CC)
private val NavigationIndicator = Color(0xFF163B42)

@Composable
fun DeepSourceBottomNavigation(
    selectedTab: DeepSourceTab,
    onTabSelected: (DeepSourceTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = NavigationBackground,
        contentColor = NavigationMuted
    ) {
        NavigationBarItem(
            selected = selectedTab == DeepSourceTab.PROJECTS,
            onClick = { onTabSelected(DeepSourceTab.PROJECTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Proyectos"
                )
            },
            label = {
                Text(
                    text = "Proyectos",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == DeepSourceTab.PROJECTS) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            },
            colors = navigationItemColors()
        )

        NavigationBarItem(
            selected = selectedTab == DeepSourceTab.REGISTER,
            onClick = { onTabSelected(DeepSourceTab.REGISTER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Registro de horas"
                )
            },
            label = {
                Text(
                    text = "Registro",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == DeepSourceTab.REGISTER) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            },
            colors = navigationItemColors()
        )

        NavigationBarItem(
            selected = selectedTab == DeepSourceTab.PROFILE,
            onClick = { onTabSelected(DeepSourceTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil"
                )
            },
            label = {
                Text(
                    text = "Perfil",
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == DeepSourceTab.PROFILE) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            },
            colors = navigationItemColors()
        )
    }
}

@Composable
private fun navigationItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = NavigationTeal,
        selectedTextColor = NavigationTeal,
        indicatorColor = NavigationIndicator,
        unselectedIconColor = NavigationMuted,
        unselectedTextColor = NavigationMuted
    )
