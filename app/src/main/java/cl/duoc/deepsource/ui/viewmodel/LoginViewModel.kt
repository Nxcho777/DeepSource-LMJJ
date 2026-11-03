package cl.duoc.deepsource.ui.viewmodel

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
    val rememberMe: Boolean = true
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
        _uiState.update { it.copy(username = value) }
    }

    fun setPassword(value: String) {
        savedStateHandle["password"] = value
        _uiState.update { it.copy(password = value) }
    }

    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun setRememberMe(value: Boolean) {
        savedStateHandle["rememberMe"] = value
        _uiState.update { it.copy(rememberMe = value) }
    }
}
