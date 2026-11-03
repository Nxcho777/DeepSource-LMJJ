package cl.duoc.deepsource.data.repository

import android.content.Context
import androidx.room.withTransaction
import cl.duoc.deepsource.data.local.DeepSourceDatabase
import cl.duoc.deepsource.data.local.SeedStateEntity
import cl.duoc.deepsource.data.local.toDomain
import cl.duoc.deepsource.data.local.toEntity
import cl.duoc.deepsource.data.mock.MockData
import cl.duoc.deepsource.domain.model.Project
import cl.duoc.deepsource.domain.model.TimeEntry
import cl.duoc.deepsource.domain.model.User
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DeepSourceRepository(private val database: DeepSourceDatabase) {
    private val dao = database.dao()

    fun observeProjects(): Flow<List<Project>> = flow {
        seedIfNeeded()
        emitAll(dao.observeProjects().map { rows -> rows.map { it.toDomain() } })
    }

    fun observeUsers(): Flow<List<User>> = flow {
        seedIfNeeded()
        emitAll(dao.observeUsers().map { rows -> rows.map { it.toDomain() } })
    }

    fun observeTimeEntries(): Flow<List<TimeEntry>> = flow {
        seedIfNeeded()
        emitAll(dao.observeTimeEntries().map { rows -> rows.map { it.toDomain() } })
    }

    suspend fun getProjects(): List<Project> {
        seedIfNeeded()
        return dao.getProjects().map { it.toDomain() }
    }

    suspend fun getUser(userId: String): User? {
        seedIfNeeded()
        return dao.getUser(userId)?.toDomain()
    }

    suspend fun addTimeEntry(projectId: String, userId: String, durationMinutes: Int, date: String, note: String): TimeEntry {
        require(durationMinutes > 0) { "La duración debe ser mayor que cero." }
        require(date.isNotBlank()) { "La fecha es obligatoria." }
        seedIfNeeded()
        return database.withTransaction {
            val project = requireNotNull(dao.getProject(projectId)) { "Proyecto no encontrado." }
            requireNotNull(dao.getUser(userId)) { "Usuario no encontrado." }
            require(project.registeredMinutes <= Int.MAX_VALUE - durationMinutes) { "El total de minutos excede el límite." }
            val entry = TimeEntry(UUID.randomUUID().toString(), projectId, userId, durationMinutes, date, note)
            dao.insertTimeEntry(entry.toEntity())
            check(dao.addMinutes(projectId, durationMinutes) == 1) { "No se pudieron actualizar los minutos del proyecto." }
            entry
        }
    }

    suspend fun seedIfNeeded() {
        database.withTransaction {
            if (!dao.isSeeded()) {
                if (dao.businessRowCount() == 0) {
                    // These minutes already include the historical seed entry; do not add it again.
                    dao.insertUsers(MockData.users.map { it.toEntity() })
                    dao.insertProjects(MockData.projects.map { it.toEntity() })
                    dao.insertTimeEntries(MockData.timeEntries.map { it.toEntity() })
                }
                dao.markSeeded(SeedStateEntity())
            }
        }
    }

    companion object {
        const val DEFAULT_USER_ID = "user-1"

        fun getInstance(context: Context): DeepSourceRepository =
            DeepSourceRepository(DeepSourceDatabase.getInstance(context))
    }
}
