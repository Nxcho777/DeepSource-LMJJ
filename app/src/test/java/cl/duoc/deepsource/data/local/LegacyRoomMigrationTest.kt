package cl.duoc.deepsource.data.local

import android.database.sqlite.SQLiteDatabase
import cl.duoc.deepsource.data.repository.DeepSourceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LegacyRoomMigrationTest {
    @Test
    fun allMigrationsPreserveLegacyHistoryAndProjectMinutes() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val name = "ev4-v1-migration.db"
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        val schema = javaClass.getResourceAsStream("/cl.duoc.deepsource.data.local.DeepSourceDatabase/1.json")!!
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { database ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val table = entities.getJSONObject(index)
                val nameInSchema = table.getString("tableName")
                database.execSQL(table.getString("createSql").replace("\${TABLE_NAME}", nameInSchema))
                val indices = table.getJSONArray("indices")
                for (i in 0 until indices.length()) {
                    database.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", nameInSchema))
                }
            }
            database.execSQL("INSERT INTO users (id,name,email) VALUES ('user-1','Usuario histórico','legacy@example.invalid')")
            for (id in 1..4) {
                database.execSQL("INSERT INTO projects (id,name,description,client,registeredMinutes,totalMinutes) VALUES (?,?,?,?,?,?)",
                    arrayOf("project-$id", "Proyecto $id", "Histórico", "Cliente", 600, 2400))
            }
            database.execSQL("INSERT INTO time_entries (id,projectId,userId,durationMinutes,date,note) VALUES ('legacy','project-1','user-1',100,'2026-11-26','Existente')")
            database.version = 1
        }
        val database = DeepSourceDatabase.openDatabase(context, name)
        try {
            val repository = DeepSourceRepository(database)
            val atlas = repository.getProjects().single { it.id == "project-1" }
            assertEquals(600, atlas.registeredMinutes)
            assertFalse(atlas.archived)
            val entries = repository.observeTimeEntries().first().filter { it.projectId == atlas.id }
            assertEquals(600, entries.sumOf { it.durationMinutes })
            assertEquals("general-project-1", entries.single { it.id == "legacy" }.taskId)
            assertEquals(100, entries.single { it.id == "legacy" }.durationMinutes)
            assertEquals("ADMIN", repository.getUser("user-admin")?.role?.name)
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
