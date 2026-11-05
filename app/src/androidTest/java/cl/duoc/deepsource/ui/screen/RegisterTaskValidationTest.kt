package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import org.junit.Rule
import org.junit.Test

class RegisterTaskValidationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun blankTaskShowsErrorOnDoneAndValidTextClearsIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val viewModel = WorkspaceViewModel(
                DeepSourceRepository(database), SavedStateHandle(), {}, TimeEntryNotifier { _, _ -> false }
            )
            composeRule.setContent {
                DeepSourceTheme { RegisterTimeScreen(viewModel, {}, {}) }
            }
            composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }

            val taskField = composeRule.onNode(hasSetTextAction())
            taskField.performClick()
            composeRule.onNodeWithText("Proyecto Atlas").performClick()
            composeRule.onNodeWithText("Cancelar").performClick()
            composeRule.onNodeWithText("Debes ingresar el nombre de la tarea.").assertIsDisplayed()

            taskField.performClick()
            taskField.performTextInput("   ")
            taskField.performImeAction()
            composeRule.onNodeWithText("Debes ingresar el nombre de la tarea.").assertIsDisplayed()

            taskField.performClick()
            taskField.performTextInput("Diseño")
            composeRule.onNodeWithText("Debes ingresar el nombre de la tarea.").assertDoesNotExist()
        } finally {
            database.close()
        }
    }
}
