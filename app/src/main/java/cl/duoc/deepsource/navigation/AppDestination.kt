package cl.duoc.deepsource.navigation

sealed class AppDestination(val route: String, val title: String) {
    data object Login : AppDestination("login", "Login")
    data object Projects : AppDestination("projects", "Proyectos")
    data object RegisterTime : AppDestination("register_time", "Registrar tiempo")
    data object Profile : AppDestination("profile", "Perfil")
}
