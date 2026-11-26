package cl.duoc.deepsource.ui.screen

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.data.remote.HolidayDto
import cl.duoc.deepsource.data.remote.HolidayRepository
import cl.duoc.deepsource.data.remote.HolidaysApi
import cl.duoc.deepsource.domain.model.RegistrationDates
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import org.junit.Rule
import org.junit.Test
import java.time.format.DateTimeFormatter
import java.util.Locale

class RegisterTaskValidationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun anActualTaskSelectionIsRequiredBeforeSaving() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val viewModel = WorkspaceViewModel(
                DeepSourceRepository(database), SavedStateHandle(), {}, TimeEntryNotifier { _, _ -> false }, context
            )
            viewModel.setHours("01")
            composeRule.setContent { DeepSourceTheme { RegisterTimeScreen(viewModel, {}, {}) } }
            composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }

            val save = composeRule.onNodeWithText("Guardar registro")
            save.performScrollTo().assertIsNotEnabled()
            val picker = composeRule.onNodeWithText("Seleccionar tarea")
            picker.performScrollTo().performClick()
            composeRule.onNodeWithText("Tareas de Proyecto Atlas").assertExists()
            composeRule.runOnIdle { check(viewModel.registerState.value.selectedTaskId == null) }
            composeRule.onNodeWithText("Diseño de interfaz").performClick()
            composeRule.onNodeWithText("Diseño de interfaz").assertExists()
            save.performScrollTo().assertIsEnabled()
        } finally {
            database.close()
        }
    }

    @Test
    fun showsNationalHolidayInTheExistingCalendarAndKeepsConfirmationEnabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val date = RegistrationDates().earliest
            val api = object : HolidaysApi {
                override suspend fun list(year: Int) = listOf(
                    HolidayDto(date.toString(), "Feriado de prueba", true, listOf("Public"))
                )
            }
            val vm = WorkspaceViewModel(DeepSourceRepository(database), SavedStateHandle(), {},
                TimeEntryNotifier { _, _ -> false }, context, holidays = HolidayRepository(api))
            vm.setDate(date.toString())
            composeRule.setContent { DeepSourceTheme { RegisterTimeScreen(vm, {}, {}) } }
            composeRule.waitUntil(5_000) { !vm.uiState.value.isLoading }
            val calendarDate = date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale("es", "CL")))
            composeRule.onNodeWithText(calendarDate).performScrollTo().performClick()
            composeRule.waitUntil(5_000) { vm.holidayDates.value.containsKey(date) }
            composeRule.onNodeWithText("Feriado nacional: Feriado de prueba").assertIsDisplayed()
            composeRule.onNodeWithText("Confirmar").assertIsEnabled()
        } finally {
            database.close()
        }
    }
}
