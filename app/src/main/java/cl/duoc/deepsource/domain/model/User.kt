package cl.duoc.deepsource.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val userRole: UserRole = UserRole.DEV
)
