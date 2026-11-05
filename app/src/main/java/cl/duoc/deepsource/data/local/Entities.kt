package cl.duoc.deepsource.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val client: String,
    val registeredMinutes: Int,
    val totalMinutes: Int,
    val isCompleted: Boolean = false,
    val iconIndex: Int = 0
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String
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
    val note: String
)

/** Durable marker: deleting every project later must not cause a new seed. */
@Entity(tableName = "seed_state")
data class SeedStateEntity(@PrimaryKey val id: Int = 1)

fun ProjectEntity.toDomain() = Project(id, name, description, client, registeredMinutes, totalMinutes, isCompleted, iconIndex)
fun Project.toEntity() = ProjectEntity(id, name, description, client, registeredMinutes, totalMinutes, isCompleted, iconIndex)
fun UserEntity.toDomain() = User(id, name, email)
fun User.toEntity() = UserEntity(id, name, email)
fun TimeEntryEntity.toDomain() = TimeEntry(id, projectId, userId, durationMinutes, date, note)
fun TimeEntry.toEntity() = TimeEntryEntity(id, projectId, userId, durationMinutes, date, note)
