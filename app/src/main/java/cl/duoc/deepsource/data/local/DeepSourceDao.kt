package cl.duoc.deepsource.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeepSourceDao {
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

    @Query("UPDATE projects SET isCompleted = 1 WHERE id = :projectId AND isCompleted = 0")
    suspend fun finishProject(projectId: String): Int

    @Query("UPDATE projects SET registeredMinutes = registeredMinutes + :minutes WHERE id = :projectId")
    suspend fun addMinutes(projectId: String, minutes: Int): Int
}
