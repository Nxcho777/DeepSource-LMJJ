package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileFlowsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun savesNameLocallyShowsPrivacyAndConfirmsLogout() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val vm = WorkspaceViewModel(DeepSourceRepository(database), SavedStateHandle(), {},
                TimeEntryNotifier { _, _ -> false }, context)
            compose.setContent { DeepSourceTheme { ProfileScreen(vm, {}, {}, vm::logout) } }
            compose.waitUntil(5_000) { !vm.uiState.value.isLoading }
            compose.onAllNodesWithText("Editar perfil").onFirst().performScrollTo().performClick()
            compose.onNode(hasSetTextAction() and hasText("Nombre")).performTextReplacement("Nombre de evaluación")
            compose.onNodeWithText("Guardar nombre").performClick()
            compose.waitUntil(5_000) { vm.uiState.value.currentUser?.name == "Nombre de evaluación" }
            compose.onNodeWithText("Privacidad").performScrollTo().performClick()
            compose.onNodeWithText("Tu cuenta y las horas registradas se guardan en este dispositivo.", substring = true)
                .assertIsDisplayed()
            compose.onNodeWithText("Cerrar", substring = false).performClick()
            compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
            compose.onAllNodes(hasClickAction() and hasText("Cerrar sesión")).onLast().performClick()
            compose.waitUntil(5_000) { vm.uiState.value.currentUserId.isEmpty() }
            assertEquals("", vm.uiState.value.currentUserId)
        } finally {
            database.close()
        }
    }
}
