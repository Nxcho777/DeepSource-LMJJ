package cl.duoc.deepsource.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.remote.NetworkClients
import cl.duoc.deepsource.data.remote.HolidayRepository
import cl.duoc.deepsource.data.remote.ProjectsApi
import cl.duoc.deepsource.data.remote.TasksApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CatalogLiveTest {
    @Test
    fun retrofitReadsPublicHolidaysForChileFromAndroid() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("deepSourceLive") == "true")
        val holidays = HolidayRepository(NetworkClients.holidays).forYear(2026)
        assertTrue(holidays.containsKey(LocalDate.parse("2026-09-18")))
        assertFalse(holidays.containsKey(LocalDate.parse("2026-08-20")))
    }

    @Test
    fun androidCallsBothServicesAndKeepsHoursAfterArchivingAndNetworkLoss() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("deepSourceLive") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val projects = checkNotNull(NetworkClients.projects)
        val tasks = checkNotNull(NetworkClients.tasks)
        val database = Room.inMemoryDatabaseBuilder(context, DeepSourceDatabase::class.java).build()
        try {
            val catalog = CatalogRepository(database, projects, tasks)
            val local = DeepSourceRepository(database)
            local.seedIfNeeded()
            database.dao().markSeeded(SeedStateEntity(2))
            val project = catalog.saveProject("user-admin", null, "EV4 Android", "Prueba", "CRUD Android", 2, 1)
            val edited = catalog.saveProject("user-admin", project, "EV4 Android editado", "Prueba", "CRUD Android", 3, 2)
            assertEquals("EV4 Android editado", projects.get(project.id).name)
            val task = catalog.saveTask("user-admin", project.id, null, "Tarea Android", "", 1)
            val updatedTask = catalog.saveTask("user-admin", project.id, task, "Tarea editada", "", 2)
            assertEquals(120, tasks.get(project.id, task.id).estimatedMinutes)
            val entry = local.addTimeEntry(project.id, "user-1", 35, "2026-11-26", "Prueba", task.id)
            catalog.refresh()
            assertEquals(35, database.dao().getProject(project.id)?.registeredMinutes)
            assertEquals(180, edited.totalMinutes)

            catalog.deleteTask("user-admin", updatedTask)
            assertTrue(tasks.get(project.id, task.id).archived)
            catalog.deleteProject("user-admin", project.id)
            assertTrue(projects.get(project.id).archived)
            assertEquals(entry.taskId, database.dao().getTask(task.id)?.id)

            val unavailable = NetworkClients.retrofit("http://10.0.2.2:1/")
            val offline = CatalogRepository(database, unavailable.create(ProjectsApi::class.java),
                unavailable.create(TasksApi::class.java))
            assertTrue(runCatching { offline.refresh() }.isFailure)
            assertEquals(35, database.dao().getProject(project.id)?.registeredMinutes)
            assertTrue(database.dao().getProject(project.id)?.archived == true)
        } finally {
            database.close()
        }
    }
}
