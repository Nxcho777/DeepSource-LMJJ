package cl.duoc.deepsource.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import cl.duoc.deepsource.domain.model.UserRole

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val client: String,
    val registeredMinutes: Int,
    val totalMinutes: Int,
    val isCompleted: Boolean = false,
    val iconIndex: Int = 0,
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String = "USER"
)

@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"]),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"])
    ],
    indices = [Index("projectId"), Index("userId")]
)
data class TimeEntryEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val userId: String,
    val durationMinutes: Int,
    val date: String,
    val note: String,
    val taskId: String? = null
)

/** Durable marker: deleting every project later must not cause a new seed. */
@Entity(tableName = "seed_state")
data class SeedStateEntity(@PrimaryKey val id: Int = 1)

fun ProjectEntity.toDomain() = Project(id, name, description, client, registeredMinutes, totalMinutes, isCompleted, iconIndex, archived)
fun Project.toEntity() = ProjectEntity(id, name, description, client, registeredMinutes, totalMinutes, isCompleted, iconIndex, archived)
fun UserEntity.toDomain() = User(id, name, email, runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER))
fun User.toEntity() = UserEntity(id, name, email, role.name)
fun TimeEntryEntity.toDomain() = TimeEntry(id, projectId, userId, durationMinutes, date, note, taskId)
fun TimeEntry.toEntity() = TimeEntryEntity(id, projectId, userId, durationMinutes, date, note, taskId)

@Entity(tableName = "project_tasks", indices = [Index("projectId")],
    foreignKeys = [ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"])])
data class ProjectTaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val estimatedMinutes: Int,
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false
)

fun ProjectTaskEntity.toDomain() = cl.duoc.deepsource.domain.model.ProjectTask(id, projectId, name, description, estimatedMinutes, archived)
fun cl.duoc.deepsource.domain.model.ProjectTask.toEntity() = ProjectTaskEntity(id, projectId, name, description, estimatedMinutes, archived)
