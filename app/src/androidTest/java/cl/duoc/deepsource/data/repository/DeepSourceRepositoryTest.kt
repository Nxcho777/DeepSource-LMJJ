package cl.duoc.deepsource.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import androidx.lifecycle.SavedStateHandle
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import cl.duoc.deepsource.notification.AndroidTimeEntryNotifier
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.domain.model.TimeEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepSourceRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: DeepSourceDatabase
    private val databaseName = "deepsource-repository-test.db"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(databaseName)
        database = openDatabase()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun seedRunsOnceAndSurvivesReopen() = runBlocking {
        val repository = DeepSourceRepository(database)
        repository.seedIfNeeded()
        repository.seedIfNeeded()
        assertEquals(4, repository.observeProjects().first().size)
        assertEquals(1, repository.observeTimeEntries().first().size)

        database.close()
        database = openDatabase()
        val reopened = DeepSourceRepository(database)
        reopened.seedIfNeeded()
        assertEquals(4, reopened.observeProjects().first().size)
        assertEquals(1, reopened.observeTimeEntries().first().size)
        assertEquals(600, reopened.getProjects().first { it.id == "project-1" }.registeredMinutes)
    }

    @Test
    fun saveUpdatesEntryAndProjectAndInvalidUserChangesNeither() = runBlocking {
        val repository = DeepSourceRepository(database)
        val entry = repository.addTimeEntry("project-1", "user-2", 45, "2026-11-02", "Prueba")
        assertEquals("user-2", entry.userId)
        assertEquals(645, repository.getProjects().first { it.id == "project-1" }.registeredMinutes)
        assertEquals(2, repository.observeTimeEntries().first().size)

        val failure = runCatching {
            repository.addTimeEntry("project-1", "missing-user", 30, "2026-11-02", "Inválido")
        }
        assertTrue(failure.isFailure)
        assertEquals(645, repository.getProjects().first { it.id == "project-1" }.registeredMinutes)
        assertEquals(2, repository.observeTimeEntries().first().size)
    }

    @Test
    fun viewModelSavesThroughRepositoryAndRetainsFormFields() = runBlocking {
        val handle = SavedStateHandle()
        val repository = DeepSourceRepository(database)
        val notifications = mutableListOf<Pair<TimeEntry, String>>()
        val viewModel = WorkspaceViewModel(repository, handle, {}, TimeEntryNotifier { entry, projectName ->
            notifications += entry to projectName
            true
        })
        withTimeout(5_000) { viewModel.uiState.first { !it.isLoading && it.projects.isNotEmpty() } }
        viewModel.selectProject("project-2")
        viewModel.selectUser("user-2")
        viewModel.setHours("01")
        viewModel.setMinutes("15")
        viewModel.setTask("Diseño")
        val restored = WorkspaceViewModel(repository, handle, {}, TimeEntryNotifier { _, _ -> false })
        assertEquals("project-2", restored.uiState.value.selectedProjectId)
        assertEquals("user-2", restored.registerState.value.userId)
        assertEquals(75, restored.registerState.value.durationMinutes)
        assertEquals("Diseño", restored.registerState.value.task)
        viewModel.saveTimeEntry()
        viewModel.saveTimeEntry()
        val receipt = withTimeout(5_000) { viewModel.registerState.first { it.receipt != null }.receipt!! }
        assertEquals("App Boreal", receipt.projectName)
        assertEquals("Diseño", receipt.taskName)
        assertEquals("2026-11-02", receipt.date)
        assertEquals(75, receipt.durationMinutes)
        assertEquals("1 h 15 min", receipt.durationLabel)
        viewModel.saveTimeEntry()
        assertEquals(1_575, repository.getProjects().first { it.id == "project-2" }.registeredMinutes)
        val entries = repository.observeTimeEntries().first()
        assertEquals(2, entries.size)
        assertTrue(entries.any { it.id == receipt.entryId && it.projectId == "project-2" && it.userId == "user-2" && it.durationMinutes == 75 })
        assertEquals(1, notifications.size)
        assertEquals(receipt.entryId, notifications.single().first.id)
        assertEquals("App Boreal", notifications.single().second)
        viewModel.acceptReceipt()
        assertNull(viewModel.registerState.value.receipt)
    }

    @Test
    fun failedSaveDoesNotNotify() = runBlocking {
        var notificationCount = 0
        val repository = DeepSourceRepository(database)
        val viewModel = WorkspaceViewModel(repository, SavedStateHandle(), {}, TimeEntryNotifier { _, _ ->
            notificationCount++
            true
        })
        withTimeout(5_000) { viewModel.uiState.first { !it.isLoading && it.projects.isNotEmpty() } }
        viewModel.setHours("01")
        viewModel.setTask("Prueba")
        viewModel.setDate("") // Repository rejects the invalid date before inserting.
        viewModel.saveTimeEntry()
        withTimeout(5_000) { viewModel.registerState.first { it.isError } }
        assertNull(viewModel.registerState.value.receipt)
        assertEquals(0, notificationCount)
        assertEquals(1, repository.observeTimeEntries().first().size)
    }

    @Test
    fun deniedNotificationDoesNotInterruptSavedRecord() = runBlocking {
        val repository = DeepSourceRepository(database)
        val viewModel = WorkspaceViewModel(repository, SavedStateHandle(), {}, TimeEntryNotifier { _, _ ->
            throw SecurityException("POST_NOTIFICATIONS denied")
        })
        withTimeout(5_000) { viewModel.uiState.first { !it.isLoading && it.projects.isNotEmpty() } }
        viewModel.setMinutes("30")
        viewModel.setTask("Prueba sin permiso")
        viewModel.saveTimeEntry()
        withTimeout(5_000) { viewModel.registerState.first { it.receipt != null } }
        assertFalse(viewModel.registerState.value.isError)
        assertEquals(630, repository.getProjects().first { it.id == "project-1" }.registeredMinutes)
    }

    @Test
    fun notifierSkipsPostingWhenPermissionIsUnavailable() {
        val notifier = AndroidTimeEntryNotifier(context) { false }
        assertFalse(notifier.notifySaved(TimeEntry("test-1", "project-1", "user-1", 30, "2026-11-02", "Prueba"), "Proyecto Atlas"))
    }

    private fun openDatabase(): DeepSourceDatabase =
        Room.databaseBuilder(context, DeepSourceDatabase::class.java, databaseName).build()
}
