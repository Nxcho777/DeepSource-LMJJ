
package cl.duoc.deepsource.data.auth

import androidx.compose.runtime.mutableStateOf
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.domain.model.UserRole
import cl.duoc.deepsource.domain.model.UserRole.*

data class DemoAccount(
    val user: User,
    val password: String
)

object AuthManager {

    private val accounts = listOf(
        DemoAccount(
            User(
                "user-1",
                "Marina Soto",
                "marina.soto@deepsource.cl",
                UserRole.LEADER
            ),
            "12345678"
        ),
        DemoAccount(
            User(
                "user-2",
                "Carlos Lopez",
                "carlos.lopez@deepsource.cl",
                UserRole.DEV
            ),
            "Carlos2026"
        ),
        DemoAccount(
            User(
                "user-3",
                "Juan Perez",
                "juan.perez@deepsource.cl",
                UserRole.DEV
            ),
            "Juan2026"
        )
    )

    val currentUser = mutableStateOf<User?>(null)

    fun login(email: String, password: String): Boolean {
        currentUser.value = null

        val account = accounts.firstOrNull {
            it.user.email.equals(email.trim(), ignoreCase = true) &&
                    it.password == password
        }

        currentUser.value = account?.user
        return account != null
    }

    fun logout() {
        currentUser.value = null
    }

    fun isAuthenticated(): Boolean {
        return currentUser.value != null
    }

    fun canRegisterTime(): Boolean {
        return currentUser.value != null
    }
}
