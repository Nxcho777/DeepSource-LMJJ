
package cl.duoc.deepsource.navigation

sealed class AppDestination(val route: String, val title: String) {
    data object Splash : AppDestination("splash", "Splash")
    data object Login : AppDestination("login", "Login")
    data object Projects : AppDestination("projects", "Proyectos")
    data object CreateProject : AppDestination("create_project", "Crear proyecto")
    data object RegisterTime : AppDestination("register_time", "Registrar tiempo")
    data object Profile : AppDestination("profile", "Perfil")
}
