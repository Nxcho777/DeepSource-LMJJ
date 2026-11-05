package cl.duoc.deepsource.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val username: String = "marina.soto@deepsource.cl",
    val password: String = "deepsource",
    val passwordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null
)

/** The demo login has no authentication service yet; navigation stays in the NavHost. */
class LoginViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        val Factory = viewModelFactory { initializer { LoginViewModel(createSavedStateHandle()) } }
    }

    private val _uiState = MutableStateFlow(LoginUiState(
        username = savedStateHandle["username"] ?: "marina.soto@deepsource.cl",
        password = savedStateHandle["password"] ?: "deepsource",
        rememberMe = savedStateHandle["rememberMe"] ?: true
    ))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun setUsername(value: String) {
        savedStateHandle["username"] = value
        _uiState.update { it.copy(username = value, usernameError = null, loginError = null) }
    }

    fun setPassword(value: String) {
        savedStateHandle["password"] = value
        _uiState.update { it.copy(password = value, passwordError = null, loginError = null) }
    }

    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun setRememberMe(value: Boolean) {
        savedStateHandle["rememberMe"] = value
        _uiState.update { it.copy(rememberMe = value) }
    }

    fun validateLogin(): Boolean {
        val email = _uiState.value.username.trim()
        val password = _uiState.value.password
        val usernameError = when {
            email.isBlank() -> "Ingresa tu correo electrónico."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo electrónico válido."
            else -> null
        }
        val passwordError = if (password.isBlank()) "Ingresa tu contraseña." else null

        if (usernameError != null || passwordError != null) {
            _uiState.update {
                it.copy(usernameError = usernameError, passwordError = passwordError, loginError = null)
            }
            return false
        }

        // Este proyecto solo tiene las credenciales demostrativas existentes.
        if (!email.equals("marina.soto@deepsource.cl", ignoreCase = true) || password != "deepsource") {
            _uiState.update {
                it.copy(usernameError = null, passwordError = null, loginError = "Correo o contraseña incorrectos.")
            }
            return false
        }

        _uiState.update {
            it.copy(usernameError = null, passwordError = null, loginError = null)
        }
        return true
    }
}
