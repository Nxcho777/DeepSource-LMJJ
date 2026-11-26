package cl.duoc.deepsource.data.remote

import cl.duoc.deepsource.BuildConfig
import cl.duoc.deepsource.data.local.ProjectEntity
import cl.duoc.deepsource.data.local.ProjectTaskEntity
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class ProjectDto(
    val id: String,
    val name: String,
    val description: String,
    val client: String,
    val totalMinutes: Int,
    val iconIndex: Int,
    val isCompleted: Boolean,
    val archived: Boolean = false
) {
    fun toEntity(registeredMinutes: Int) =
        ProjectEntity(id, name, description, client, registeredMinutes, totalMinutes, isCompleted, iconIndex, archived)
}

data class TaskDto(
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val estimatedMinutes: Int,
    val archived: Boolean = false
) {
    fun toEntity() = ProjectTaskEntity(id, projectId, name, description, estimatedMinutes, archived)
}

fun ProjectEntity.toDto() = ProjectDto(id, name, description, client, totalMinutes, iconIndex, isCompleted, archived)
fun ProjectTaskEntity.toDto() = TaskDto(id, projectId, name, description, estimatedMinutes, archived)

interface ProjectsApi {
    @GET("api/v1/projects")
    suspend fun list(@Query("includeArchived") includeArchived: Boolean = true): List<ProjectDto>

    @GET("api/v1/projects/{id}")
    suspend fun get(@Path("id") id: String): ProjectDto

    @POST("api/v1/projects")
    suspend fun create(@Body project: ProjectDto): ProjectDto

    @PUT("api/v1/projects/{id}")
    suspend fun update(@Path("id") id: String, @Body project: ProjectDto): ProjectDto

    @DELETE("api/v1/projects/{id}")
    suspend fun delete(@Path("id") id: String)
}

interface TasksApi {
    @GET("api/v1/tasks")
    suspend fun listAll(@Query("includeArchived") includeArchived: Boolean = true): List<TaskDto>

    @GET("api/v1/projects/{projectId}/tasks/{id}")
    suspend fun get(@Path("projectId") projectId: String, @Path("id") id: String): TaskDto

    @POST("api/v1/projects/{projectId}/tasks")
    suspend fun create(@Path("projectId") projectId: String, @Body task: TaskDto): TaskDto

    @PUT("api/v1/projects/{projectId}/tasks/{id}")
    suspend fun update(@Path("projectId") projectId: String, @Path("id") id: String, @Body task: TaskDto): TaskDto

    @DELETE("api/v1/projects/{projectId}/tasks/{id}")
    suspend fun delete(@Path("projectId") projectId: String, @Path("id") id: String)
}

data class HolidayDto(
    val date: String,
    val name: String,
    val nationalHoliday: Boolean,
    val holidayTypes: List<String>
)

interface HolidaysApi {
    @GET("api/v4/Holidays/CL/{year}")
    suspend fun list(@Path("year") year: Int): List<HolidayDto>
}

object NetworkClients {
    private val http = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .callTimeout(6, TimeUnit.SECONDS)
        .build()

    fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder().baseUrl(baseUrl)
        .client(http).addConverterFactory(GsonConverterFactory.create()).build()

    val projects by lazy { BuildConfig.PROJECTS_URL.takeIf(String::isNotBlank)?.let { retrofit(it).create(ProjectsApi::class.java) } }
    val tasks by lazy { BuildConfig.TASKS_URL.takeIf(String::isNotBlank)?.let { retrofit(it).create(TasksApi::class.java) } }
    val holidays by lazy { retrofit("https://nagerholidays.com/").create(HolidaysApi::class.java) }
}
