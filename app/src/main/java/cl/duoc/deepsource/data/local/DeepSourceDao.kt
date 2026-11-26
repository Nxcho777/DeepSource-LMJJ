package cl.duoc.deepsource.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DeepSourceDao {
    @Query("SELECT * FROM project_tasks ORDER BY name")
    fun observeTasks(): Flow<List<ProjectTaskEntity>>

    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId AND archived = 0 AND EXISTS (SELECT 1 FROM projects WHERE id = :projectId AND archived = 0) ORDER BY name")
    suspend fun getTasks(projectId: String): List<ProjectTaskEntity>

    @Query("SELECT * FROM project_tasks WHERE id = :id")
    suspend fun getTask(id: String): ProjectTaskEntity?

    @Query("SELECT * FROM project_tasks ORDER BY id")
    suspend fun getCatalogTasks(): List<ProjectTaskEntity>

    @Upsert
    suspend fun upsertProject(project: ProjectEntity)

    @Upsert
    suspend fun upsertTask(task: ProjectTaskEntity)

    @Query("UPDATE projects SET archived = 1 WHERE id = :id")
    suspend fun archiveProject(id: String)

    @Query("UPDATE project_tasks SET archived = 1 WHERE id = :id")
    suspend fun archiveTask(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM seed_state WHERE id = 2)")
    suspend fun isCatalogAdopted(): Boolean

    @Insert
    suspend fun insertTask(task: ProjectTaskEntity)

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM time_entries WHERE taskId = :taskId")
    suspend fun getTaskMinutes(taskId: String): Int

    @Query("SELECT * FROM projects ORDER BY id")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM users ORDER BY id")
    fun observeUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM time_entries ORDER BY date DESC, id DESC")
    fun observeTimeEntries(): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM projects ORDER BY id")
    suspend fun getProjects(): List<ProjectEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: String): UserEntity?

    @Query("UPDATE users SET name = :name WHERE id = :id")
    suspend fun renameUser(id: String, name: String): Int

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProject(id: String): ProjectEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM seed_state WHERE id = 1)")
    suspend fun isSeeded(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM projects) + (SELECT COUNT(*) FROM users) + (SELECT COUNT(*) FROM time_entries)")
    suspend fun businessRowCount(): Int

    @Insert
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Insert
    suspend fun insertProject(project: ProjectEntity)

    @Insert
    suspend fun insertTimeEntries(entries: List<TimeEntryEntity>)

    @Insert
    suspend fun insertTimeEntry(entry: TimeEntryEntity)

    @Insert
    suspend fun markSeeded(marker: SeedStateEntity)

    @Query("UPDATE projects SET isCompleted = 1 WHERE id = :projectId AND archived = 0 AND isCompleted = 0 AND registeredMinutes >= totalMinutes")
    suspend fun finishProject(projectId: String): Int

    @Query("UPDATE projects SET registeredMinutes = registeredMinutes + :minutes WHERE id = :projectId")
    suspend fun addMinutes(projectId: String, minutes: Int): Int
}
