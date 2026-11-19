package cl.duoc.deepsource.domain.model

enum class UserRole { ADMIN, USER }

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.USER
)
