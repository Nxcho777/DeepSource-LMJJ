package com.example.deepsourcelmjj

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Projects : BottomNavItem("projects", "Proyectos", Icons.Default.List)
    object Register : BottomNavItem("register", "Registrar", Icons.Default.AddCircle)
    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    currentUser: AppUser,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val assignedProjects = remember(currentUser.userId) {
        ProjectRepository.getProjectsForUser(currentUser.userId)
    }

    val items = listOf(
        BottomNavItem.Projects,
        BottomNavItem.Register,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF131E2C)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF1DB98A),
                            indicatorColor = Color(0xFF1DB98A),
                            unselectedIconColor = Color(0xFF8E9BAE),
                            unselectedTextColor = Color(0xFF8E9BAE)
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFF09121D)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Projects.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Projects.route) {
                ProjectsListScreen(
                    currentUser = currentUser,
                    onProjectClick = { project ->
                        navController.navigate("project_detail/${project.projectId}")
                    }
                )
            }
            composable("project_detail/{projectId}") { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                val project = ProjectRepository.getProjectById(projectId)

                ProjectDetailScreen(
                    projectTitle = project?.title ?: "Proyecto",
                    totalHours = project?.totalHours ?: "0 hrs",
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(BottomNavItem.Register.route) {
                DailyRegisterScreen(
                    projectOptions = assignedProjects.map { project -> project.title }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    currentUser = currentUser,
                    assignedProjectsCount = assignedProjects.size,
                    onLogoutClick = {
                        onLogout()
                    }
                )
            }
        }
    }
}
