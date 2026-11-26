package cl.duoc.deepsource.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.remote.NetworkClients
import cl.duoc.deepsource.data.repository.CatalogRepository
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class CatalogScreensLiveTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun administratorCanManageProjectsAndTasksFromTheScreens() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("deepSourceLive") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val local = DeepSourceRepository(database)
            runBlocking {
                local.seedIfNeeded()
                database.dao().markSeeded(SeedStateEntity(2))
            }
            val vm = WorkspaceViewModel(local, SavedStateHandle(), {}, TimeEntryNotifier { _, _ -> false },
                context, CatalogRepository(database, checkNotNull(NetworkClients.projects), checkNotNull(NetworkClients.tasks)))
            val showRegister = mutableStateOf(false)
            compose.setContent {
                DeepSourceTheme {
                    if (showRegister.value) RegisterTimeScreen(vm, {}, {})
                    else ProjectsScreen(vm, { showRegister.value = true }, {})
                }
            }
            compose.waitUntil(10_000) { !vm.uiState.value.isLoading }
            compose.runOnIdle { vm.setCurrentUserByEmail("admin@deepsource.cl") }
            val name = "EV4 Pantalla " + System.nanoTime().toString()
            compose.onNodeWithText("Crear", substring = false).performClick()
            field("Nombre del proyecto").performTextInput(name)
            field("Cliente o empresa").performTextInput("DeepSource")
            field("Descripción").performTextInput("Operación desde Android")
            compose.onAllNodesWithText("Crear proyecto").onLast().performScrollTo().performClick()
            compose.waitUntil(15_000) { vm.uiState.value.projects.any { it.name == name } }
            val projectId = vm.uiState.value.projects.first { it.name == name }.id
            projectCard(name).performTouchInput { longClick() }
            compose.onNodeWithText("Editar proyecto").performClick()
            val edited = "$name editado"
            field("Nombre del proyecto").performTextReplacement(edited)
            compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
            compose.waitUntil(15_000) { vm.uiState.value.projects.any { it.id == projectId && it.name == edited } }

            projectCard(edited).performClick()
            taskPicker("Seleccionar tarea").performClick()
            compose.onNodeWithText("+ Crear nueva tarea").performClick()
            field("Nombre de la tarea").performTextInput("ZZ Tarea Android")
            compose.onNodeWithText("Crear tarea").performScrollTo().performClick()
            compose.waitUntil(15_000) {
                vm.uiState.value.tasks.any { it.projectId == projectId && it.name == "ZZ Tarea Android" }
            }
            taskPicker("ZZ Tarea Android").performClick()
            compose.onAllNodesWithText("Editar", substring = false).onLast().performScrollTo().performClick()
            field("Nombre de la tarea").performTextReplacement("ZZ Tarea editada")
            compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
            compose.waitUntil(15_000) {
                vm.uiState.value.tasks.any { it.projectId == projectId && it.name == "ZZ Tarea editada" }
            }
            taskPicker("ZZ Tarea editada").performClick()
            compose.onAllNodesWithText("Eliminar", substring = false).onLast().performScrollTo().performClick()
            compose.onNodeWithText("Eliminar", substring = false).performClick()
            compose.waitUntil(15_000) {
                vm.uiState.value.tasks.any { it.projectId == projectId && it.name == "ZZ Tarea editada" && it.archived }
            }
            compose.runOnIdle { showRegister.value = false }
            projectCard(edited).performTouchInput { longClick() }
            compose.onNodeWithText("Eliminar proyecto").performClick()
            compose.onNodeWithText("Eliminar", substring = false).performClick()
            compose.waitUntil(15_000) { vm.uiState.value.projects.any { it.id == projectId && it.archived } }
        } finally {
            database.close()
        }
    }

    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label)).performScrollTo()

    private fun projectCard(name: String): SemanticsNodeInteraction {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(name))
        return compose.onNodeWithText(name, substring = false)
    }

    private fun taskPicker(name: String) = compose.onNodeWithText(name, substring = false).performScrollTo()
}
