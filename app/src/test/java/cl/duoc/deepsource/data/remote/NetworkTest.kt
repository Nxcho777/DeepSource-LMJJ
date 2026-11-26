package cl.duoc.deepsource.data.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import java.time.LocalDate

class NetworkTest {
    @Test
    fun holidayReadsActualResponseFiltersAndCachesOnlySuccessfulYears() = runBlocking {
        MockWebServer().use { server ->
            val api = NetworkClients.retrofit(server.url("/").toString()).create(HolidaysApi::class.java)
            val repository = HolidayRepository(api)
            server.enqueue(MockResponse().setResponseCode(503))
            assertTrue(runCatching { repository.forYear(2026) }.exceptionOrNull() is HttpException)
            server.enqueue(MockResponse().setBody("""
                [
                  {"date":"2026-09-18","name":"National holiday","nationalHoliday":true,"holidayTypes":["Public"]},
                  {"date":"2026-08-20","name":"Regional","nationalHoliday":false,"holidayTypes":["Public"]},
                  {"date":"2026-09-10","name":"Bank","nationalHoliday":true,"holidayTypes":["Bank"]},
                  {"date":"bad","name":"Invalid","nationalHoliday":true,"holidayTypes":["Public"]}
                ]
            """.trimIndent()))
            val result = repository.forYear(2026)
            assertEquals(mapOf(LocalDate.parse("2026-09-18") to "National holiday"), result)
            assertEquals(result, repository.forYear(2026))
            assertEquals(2, server.requestCount)
            assertEquals("/api/v4/Holidays/CL/2026", server.takeRequest().path)
            assertEquals("/api/v4/Holidays/CL/2026", server.takeRequest().path)
        }
    }

    @Test
    fun clientsUseCrudRoutesAndDeserializeArchiveFlags() = runBlocking {
        MockWebServer().use { server ->
            val retrofit = NetworkClients.retrofit(server.url("/").toString())
            val projects = retrofit.create(ProjectsApi::class.java)
            val tasks = retrofit.create(TasksApi::class.java)
            val project = ProjectDto("one", "Proyecto", "Detalle", "Cliente", 60, 3, false)
            val task = TaskDto("two", "one", "Tarea", "", 60, true)
            val active = """{"id":"one","name":"Proyecto","description":"Detalle","client":"Cliente","totalMinutes":60,"iconIndex":3,"isCompleted":false,"archived":false}"""
            val archived = """{"id":"two","projectId":"one","name":"Tarea","description":"","estimatedMinutes":60,"archived":true}"""
            listOf("[$active]", active, active, active, "[$archived]", archived, archived, archived).forEach {
                server.enqueue(MockResponse().setBody(it))
            }
            repeat(2) { server.enqueue(MockResponse().setResponseCode(204)) }
            assertEquals(listOf(project), projects.list())
            assertEquals(project, projects.get("one"))
            projects.create(project)
            projects.update("one", project)
            assertEquals(listOf(task), tasks.listAll())
            assertTrue(tasks.get("one", "two").archived)
            tasks.create("one", task)
            tasks.update("one", "two", task)
            tasks.delete("one", "two")
            projects.delete("one")
            assertEquals(listOf(
                "GET /api/v1/projects?includeArchived=true", "GET /api/v1/projects/one",
                "POST /api/v1/projects", "PUT /api/v1/projects/one",
                "GET /api/v1/tasks?includeArchived=true", "GET /api/v1/projects/one/tasks/two",
                "POST /api/v1/projects/one/tasks", "PUT /api/v1/projects/one/tasks/two",
                "DELETE /api/v1/projects/one/tasks/two", "DELETE /api/v1/projects/one"
            ), List(10) { server.takeRequest().let { "${it.method} ${it.path}" } })
        }
    }
}
