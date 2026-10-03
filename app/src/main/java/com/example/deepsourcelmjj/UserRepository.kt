package com.example.deepsourcelmjj

private data class UserCredential(
    val email: String,
    val password: String,
    val user: AppUser
)

object UserRepository {
    val users = listOf(
        AppUser(
            userId = "user-marina-soto",
            userName = "Marina Soto",
            userAvatar = null,
            profile = UserRole.LEADER,
            email = "marina.soto@deepsource.cl"
        ),
        AppUser(
            userId = "user-carlos-lopez",
            userName = "Carlos Lopez",
            userAvatar = null,
            profile = UserRole.DEV,
            email = "carlos.lopez@deepsource.cl"
        ),
        AppUser(
            userId = "user-juan-perez",
            userName = "Juan Perez",
            userAvatar = null,
            profile = UserRole.DEV,
            email = "juan.perez@deepsource.cl"
        )
    )

    // Credenciales locales de prueba. El requerimiento no define un servicio de autenticación.
    private val credentials = listOf(
        UserCredential(
            email = "marina.soto@deepsource.cl",
            password = "Marina2026",
            user = users[0]
        ),
        UserCredential(
            email = "carlos.lopez@deepsource.cl",
            password = "Carlos2026",
            user = users[1]
        ),
        UserCredential(
            email = "juan.perez@deepsource.cl",
            password = "Juan2026",
            user = users[2]
        )
    )

    fun authenticate(email: String, password: String): AppUser? {
        val normalizedEmail = email.trim().lowercase()

        return credentials.firstOrNull { credential ->
            credential.email.lowercase() == normalizedEmail && credential.password == password
        }?.user
    }

    fun getUserById(userId: String): AppUser? {
        return users.firstOrNull { user -> user.userId == userId }
    }
}
