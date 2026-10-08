
package cl.duoc.deepsource.domain.model

enum class UserRole {
    LEADER,
    DEV
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.DEV
)
