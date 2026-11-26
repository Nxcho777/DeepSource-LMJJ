package cl.duoc.deepsource.navigation

import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.WidgetNavigationRequest
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w411dp-h891dp")
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationFlowsTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var database: DeepSourceDatabase
    private lateinit var workspace: WorkspaceViewModel
    private lateinit var navigation: NavHostController
    private val request = mutableStateOf<WidgetNavigationRequest?>(null)

    @Before
    fun createWorkspace() {
        val context = RuntimeEnvironment.getApplication()
        Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        workspace = WorkspaceViewModel(DeepSourceRepository(database), SavedStateHandle(), {},
            TimeEntryNotifier { _, _ -> false }, context)
        runBlocking { withTimeout(5_000) { workspace.uiState.first { !it.isLoading } } }
    }

    @After
    fun closeWorkspace() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun localLoginAndWidgetNavigationReachAllWorkspaceTabs() {
        showHost(AppDestination.Login.route)
        compose.onNodeWithText("Iniciar sesión con Google").performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithText("Esta versión usa cuentas locales de DeepSource.", substring = true).assertExists()
        compose.onNodeWithText("LOGIN").performScrollTo().performClick()
        compose.mainClock.advanceTimeBy(600)
        compose.waitUntil(10_000) { navigation.currentDestination?.route == "deepsource_main" }
        assertEquals("user-1", workspace.uiState.value.currentUserId)

        compose.runOnIdle { request.value = WidgetNavigationRequest(1, "project-2") }
        compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithText("NUEVO REGISTRO").assertExists()
        assertEquals("project-2", workspace.uiState.value.selectedProjectId)
        compose.runOnIdle { request.value = WidgetNavigationRequest(2, null, AppDestination.Profile.route) }
        compose.mainClock.advanceTimeBy(600)
        compose.onAllNodesWithText("Editar perfil").onFirst().assertExists()
        compose.onNodeWithText("Cerrar sesión").assertExists()
    }

    @Test
    fun aColdWidgetRequestOpensTheIntendedProject() {
        request.value = WidgetNavigationRequest(1, "project-4")
        showHost(AppDestination.RegisterTime.route)
        compose.onNodeWithText("NUEVO REGISTRO").assertExists()
        assertEquals("project-4", workspace.uiState.value.selectedProjectId)
    }

    private fun showHost(start: String) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            navigation = rememberNavController()
            DeepSourceTheme { DeepSourceNavHost(navigation, start, workspace, request.value) }
        }
        compose.mainClock.advanceTimeBy(600)
    }
}
