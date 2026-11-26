package cl.duoc.deepsource.data.repository

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.toDomain
import cl.duoc.deepsource.data.remote.*
import cl.duoc.deepsource.domain.model.RegistrationDates
import cl.duoc.deepsource.notification.NotificationPermissionStore
import cl.duoc.deepsource.notification.TimeEntryNotifier
import cl.duoc.deepsource.ui.viewmodel.LoginViewModel
import cl.duoc.deepsource.ui.viewmodel.WorkspaceViewModel
import cl.duoc.deepsource.widget.WidgetTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class LocalPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: DeepSourceDatabase

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java)
            .allowMainThreadQueries().build()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun close() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun localRepositoryEnforcesRoleRelationshipsAndAtomicMinutes() = runBlocking {
        val repo = DeepSourceRepository(database)
        repo.seedIfNeeded()
        repo.seedIfNeeded()
        assertEquals(4, repo.observeProjects().first().size)
        assertEquals(4, repo.observeUsers().first().size)
        assertEquals(5, repo.observeTimeEntries().first().size)
        assertTrue(runCatching { repo.renameUser("user-1", " ") }.isFailure)
        assertTrue(runCatching { repo.renameUser("missing", "Nombre") }.isFailure)
        repo.renameUser("user-1", "  Nombre visible ")
        assertEquals("Nombre visible", repo.getUser("user-1")?.name)
        assertTrue(repo.observeTasks().first().isNotEmpty())
        assertTrue(runCatching { repo.createProject("user-1", "A", "B", "C", 1, 1) }.isFailure)
        assertTrue(runCatching { repo.createProject("user-admin", " ", "B", "C", 1, 1) }.isFailure)
        assertTrue(runCatching { repo.createProject("user-admin", "A", "B", "C", 1, 1,
            listOf(NewProjectTask("Inicio", "", 2))) }.isFailure)
        val project = repo.createProject("user-admin", " A ", " B ", " C ", 2, 0,
            listOf(NewProjectTask("Inicio", "", 1)))
        assertEquals("A", project.name)
        assertEquals(2, repo.getTasks(project.id).size)
        assertTrue(runCatching { repo.createTask("user-1", project.id, "Nueva", "", 1) }.isFailure)
        assertTrue(runCatching { repo.createTask("user-admin", "missing", "Nueva", "", 1) }.isFailure)
        assertTrue(runCatching { repo.createTask("user-admin", project.id, "Nueva", "", 0) }.isFailure)
        val task = repo.createTask("user-admin", project.id, "Nueva", "", 1)
        assertTrue(runCatching { repo.addTimeEntry(project.id, "user-1", 0, "2026-11-26", "") }.isFailure)
        assertTrue(runCatching { repo.addTimeEntry(project.id, "user-1", 20, "", "") }.isFailure)
        assertTrue(runCatching { repo.addTimeEntry("project-1", "user-1", 120, "2026-11-26", "", task.id) }.isFailure)
        assertTrue(runCatching { repo.addTimeEntry(project.id, "unknown", 120, "2026-11-26", "", task.id) }.isFailure)
        assertTrue(runCatching { repo.finishProject("user-admin", project.id) }.isFailure)
        repo.addTimeEntry(project.id, "user-1", 120, "2026-11-26", "", task.id)
        assertEquals(120, database.dao().getTaskMinutes(task.id))
        repo.finishProject("user-admin", project.id)
        assertTrue(repo.getProjects().single { it.id == project.id }.isCompleted)
    }

    @Test
    fun catalogAdoptionRetriesWithoutOverwritingRemoteOrLosingLocalHours() = runBlocking {
        val projects = FakeProjects()
        val tasks = FakeTasks()
        val catalog = CatalogRepository(database, projects, tasks)
        val local = DeepSourceRepository(database)
        local.seedIfNeeded()
        val atlas = database.dao().getProject("project-1")!!.toDto()
        projects.rows[atlas.id] = atlas.copy(name = "Nombre remoto")
        tasks.offline = true
        assertTrue(runCatching { catalog.refresh() }.isFailure)
        assertFalse(database.dao().isCatalogAdopted())
        assertEquals(4, projects.rows.size)
        assertEquals("Proyecto Atlas", database.dao().getProject(atlas.id)?.name)
        tasks.offline = false
        catalog.refresh()
        assertTrue(database.dao().isCatalogAdopted())
        assertEquals("Nombre remoto", database.dao().getProject(atlas.id)?.name)
        assertEquals(600, database.dao().getProject(atlas.id)?.registeredMinutes)
        val task = catalog.saveTask("user-admin", atlas.id, null, "Tarea", "Detalle", 1)
        val edited = catalog.saveTask("user-admin", atlas.id, task, "Editada", "", 2)
        local.addTimeEntry(atlas.id, "user-1", 45, "2026-11-26", "", edited.id)
        catalog.deleteTask("user-admin", edited)
        catalog.deleteProject("user-admin", database.dao().getProject(atlas.id)!!.id)
        assertEquals(45, database.dao().getTaskMinutes(edited.id))
        assertEquals(645, database.dao().getProject(atlas.id)?.registeredMinutes)
        assertTrue(local.getTasks(atlas.id).isEmpty())
        assertTrue(runCatching { catalog.saveProject("user-1", null, "Otro", "B", "C", 2, 0) }.isFailure)
        val next = catalog.saveProject("user-admin", null, "Otro", "B", "C", 2, 7)
        val changed = catalog.saveProject("user-admin", next, "Actualizado", "B", "C", 1, 7)
        local.addTimeEntry(changed.id, "user-1", 60, "2026-11-26", "")
        catalog.finishProject("user-admin", changed.id)
        assertTrue(database.dao().getProject(changed.id)!!.isCompleted)
        assertEquals(60, database.dao().getProject(changed.id)?.registeredMinutes)
    }

    @Test
    fun loginAndWorkspaceIdentityAreRestoredAndLogoutClearsTheForm() = runBlocking {
        val login = LoginViewModel(SavedStateHandle())
        assertTrue(login.validateLogin())
        login.setUsername("bad")
        login.setPassword("")
        assertFalse(login.validateLogin())
        assertNotNull(login.uiState.value.usernameError)
        assertNotNull(login.uiState.value.passwordError)
        login.setUsername("marina.soto@deepsource.cl")
        login.setPassword("incorrecta")
        assertFalse(login.validateLogin())
        assertNotNull(login.uiState.value.loginError)
        login.togglePasswordVisibility()
        assertTrue(login.uiState.value.passwordVisible)

        val repo = DeepSourceRepository(database)
        val state = SavedStateHandle()
        val vm = WorkspaceViewModel(repo, state, {}, TimeEntryNotifier { _, _ -> false }, context)
        withTimeout(5_000) { vm.uiState.first { !it.isLoading } }
        vm.setCurrentUserByEmail("admin@deepsource.cl")
        assertTrue(vm.uiState.value.isAdmin)
        val profileResult = CompletableDeferred<String?>()
        vm.saveProfileName("Nombre administrador") { profileResult.complete(it) }
        assertNull(withTimeout(5_000) { profileResult.await() })
        withTimeout(5_000) { vm.uiState.first { it.currentUser?.name == "Nombre administrador" } }
        val invalidName = CompletableDeferred<String?>()
        vm.saveProfileName(" ") { invalidName.complete(it) }
        assertTrue(withTimeout(5_000) { invalidName.await() }!!.contains("100 caracteres"))
        vm.selectProject("project-2")
        vm.selectTask(repo.getTasks("project-2").first())
        vm.setDescription("detalle")
        vm.setHours("01")
        vm.setMinutes("15")
        vm.setDate("invalid")
        vm.markTaskTouched()
        vm.markDurationTouched()
        vm.saveTimeEntry()
        assertTrue(vm.registerState.value.isError)
        assertEquals(75, vm.registerState.value.durationMinutes)
        assertEquals("detalle", state.get<String>("description"))
        vm.logout()
        assertNull(state.get<String>("selectedTaskId"))
        assertNull(vm.uiState.value.currentUser)
        assertEquals(0, vm.registerState.value.durationMinutes)
    }

    @Test
    fun workspaceUsesCatalogAndStillRegistersHoursLocallyWhenOffline() = runBlocking {
        val projects = FakeProjects()
        val tasks = FakeTasks()
        var notifications = 0
        val local = DeepSourceRepository(database)
        val vm = WorkspaceViewModel(local, SavedStateHandle(), {}, TimeEntryNotifier { _, _ ->
            notifications++
            true
        }, context, CatalogRepository(database, projects, tasks), HolidayRepository(object : HolidaysApi {
            override suspend fun list(year: Int) = listOf(HolidayDto("$year-09-18", "Feriado", true, listOf("Public")))
        }))
        withTimeout(5_000) { vm.uiState.first { !it.isLoading } }
        vm.setCurrentUserByEmail("admin@deepsource.cl")
        vm.createProject("Nuevo", "Cliente", "Descripción", 1, 1)
        val createdId = withTimeout(5_000) { vm.createProjectState.first { it.createdProjectId != null }.createdProjectId!! }
        vm.resetCreateProjectState()
        withTimeout(5_000) { vm.uiState.first { state -> state.projects.any { it.id == createdId } } }
        vm.selectProject(createdId)
        vm.createTask("Diseño", "Detalle", 1)
        val taskId = withTimeout(5_000) { vm.taskState.first { it.createdProjectId != null }.createdProjectId!! }
        val task = withTimeout(5_000) { vm.uiState.first { it.tasks.any { item -> item.id == taskId } } }
            .tasks.single { it.id == taskId }
        vm.selectTask(task)
        vm.setDate(RegistrationDates().earliest.toString())
        vm.setHours("01")
        vm.setMinutes("00")
        vm.setDescription("Registro")
        projects.offline = true
        vm.refreshCatalog()
        assertTrue(withTimeout(5_000) { vm.catalogError.first { it != null } }!!.contains("Sin conexión"))
        vm.saveTimeEntry()
        val receipt = withTimeout(5_000) { vm.registerState.first { it.receipt != null }.receipt!! }
        assertEquals("Registro", receipt.taskName)
        assertEquals(60, receipt.durationMinutes)
        assertEquals(1, notifications)
        vm.acceptReceipt()
        projects.offline = false
        vm.finishProject(createdId)
        val completed = withTimeout(5_000) {
            vm.uiState.first { state -> state.projects.any { it.id == createdId && it.isCompleted } }
        }.projects.single { it.id == createdId }
        vm.deleteTask(task)
        withTimeout(5_000) { vm.uiState.first { state -> state.tasks.any { it.id == taskId && it.archived } } }
        vm.deleteProject(completed)
        withTimeout(5_000) { vm.uiState.first { state -> state.projects.any { it.id == createdId && it.archived } } }
        assertEquals(60, database.dao().getProject(createdId)?.registeredMinutes)
        vm.loadHolidays(2026..2026)
        assertEquals("Feriado", withTimeout(5_000) { vm.holidayDates.first { it.isNotEmpty() } }.values.single())
    }

    @Test
    fun widgetTimerRetainsPausedTimeAndNotificationPreferenceIsOptIn() {
        val instance = 17
        WidgetTimer.select(context, instance, "a", "Atlas", 1)
        WidgetTimer.setTask(context, instance, "task-a", "Tarea Atlas")
        WidgetTimer.start(context, instance)
        org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofSeconds(65))
        assertTrue(WidgetTimer.elapsed(context, instance) >= 65_000L)
        WidgetTimer.selectOrRestore(context, instance, "b", "Boreal", 2)
        assertEquals("a", WidgetTimer.project(context, instance))
        WidgetTimer.pause(context, instance)
        WidgetTimer.selectOrRestore(context, instance, "b", "Boreal", 2)
        WidgetTimer.selectOrRestore(context, instance, "a", "Atlas", 1)
        assertEquals("paused", WidgetTimer.status(context, instance))
        assertEquals("task-a", WidgetTimer.taskId(context, instance))
        assertTrue(WidgetTimer.elapsed(context, instance) >= 65_000L)
        WidgetTimer.finish(context, instance)
        assertEquals("saving", WidgetTimer.status(context, instance))
        WidgetTimer.restore(context, instance)
        WidgetTimer.clear(context, instance)
        assertNull(WidgetTimer.project(context, instance))
        val notifications = NotificationPermissionStore(context)
        assertFalse(notifications.notificationsEnabled())
        assertTrue(notifications.setNotificationsEnabled(true))
        assertTrue(NotificationPermissionStore(context).notificationsEnabled())
        assertFalse(notifications.setNotificationsEnabled(false))
    }

    private class FakeProjects : ProjectsApi {
        val rows = linkedMapOf<String, ProjectDto>()
        var offline = false
        override suspend fun list(includeArchived: Boolean): List<ProjectDto> {
            if (offline) throw IOException("offline")
            return rows.values.filter { includeArchived || !it.archived }
        }
        override suspend fun get(id: String) = rows.getValue(id)
        override suspend fun create(project: ProjectDto): ProjectDto {
            if (rows.containsKey(project.id)) conflict()
            return update(project.id, project)
        }
        override suspend fun update(id: String, project: ProjectDto) = project.also { rows[id] = it }
        override suspend fun delete(id: String) { rows[id] = get(id).copy(archived = true) }
    }

    private class FakeTasks : TasksApi {
        private val rows = linkedMapOf<String, TaskDto>()
        var offline = false
        override suspend fun listAll(includeArchived: Boolean) = rows.values.filter { includeArchived || !it.archived }
        override suspend fun get(projectId: String, id: String) = rows.getValue(id)
        override suspend fun create(projectId: String, task: TaskDto): TaskDto {
            if (offline) throw IOException("offline")
            if (rows.containsKey(task.id)) conflict()
            return update(projectId, task.id, task)
        }
        override suspend fun update(projectId: String, id: String, task: TaskDto) = task.also { rows[id] = it }
        override suspend fun delete(projectId: String, id: String) { rows[id] = get(projectId, id).copy(archived = true) }
    }

    companion object {
        private fun conflict(): Nothing = throw HttpException(Response.error<Unit>(409, "".toResponseBody()))
    }
}
