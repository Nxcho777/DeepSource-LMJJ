package cl.duoc.deepsource.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import cl.duoc.deepsource.notification.notificationContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {
    @Test
    fun notificationTextUsesSavedDurationAndProject() {
        assertEquals("Se registraron 1 h 30 min en App Boreal.", notificationContent(90, "App Boreal"))
        assertEquals("Se registraron 45 min en Proyecto Atlas.", notificationContent(45, "Proyecto Atlas"))
    }

    @Test
    fun timeValidationRejectsEmptyTaskAndInvalidDuration() {
        assertFalse(RegisterTimeUiState(task = "Trabajo").isValid)
        assertFalse(RegisterTimeUiState(hours = "24", minutes = "00", task = "Trabajo").isValid)
        assertFalse(RegisterTimeUiState(hours = "01", minutes = "60", task = "Trabajo").isValid)
        assertFalse(RegisterTimeUiState(hours = "01", minutes = "30", task = "  ").isValid)
        assertTrue(RegisterTimeUiState(hours = "01", minutes = "30", task = "Trabajo").isValid)
        assertEquals(90, RegisterTimeUiState(hours = "01", minutes = "30").durationMinutes)
    }

    @Test
    fun loginFieldsSurviveViewModelRecreation() {
        val handle = SavedStateHandle()
        val first = LoginViewModel(handle)
        first.setUsername("marina@example.cl")
        first.setPassword("clave")
        first.setRememberMe(false)
        val restored = LoginViewModel(handle).uiState.value
        assertEquals("marina@example.cl", restored.username)
        assertEquals("clave", restored.password)
        assertFalse(restored.rememberMe)
    }
}
