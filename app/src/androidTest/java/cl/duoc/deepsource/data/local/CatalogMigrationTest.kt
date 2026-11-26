package cl.duoc.deepsource.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogMigrationTest {
    private val name = "catalog-migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), DeepSourceDatabase::class.java
    )

    @Test
    fun migrationPreservesIdsAssociationsAndAccumulatedMinutes() {
        helper.createDatabase(name, 8).use { db ->
            db.execSQL("INSERT INTO projects VALUES ('p','Atlas','Detalle','Cliente',75,120,0,1)")
            db.execSQL("INSERT INTO users VALUES ('u','Marina','example@example.invalid','USER')")
            db.execSQL("INSERT INTO project_tasks VALUES ('t','p','Diseño','',120)")
            db.execSQL("INSERT INTO time_entries VALUES ('e','p','u',75,'2026-11-26','detalle','t')")
        }
        helper.runMigrationsAndValidate(name, 9, true, DeepSourceDatabase.MIGRATION_8_9).use { db ->
            db.query("SELECT p.registeredMinutes,p.archived,t.archived,e.durationMinutes FROM time_entries e " +
                "JOIN projects p ON p.id=e.projectId JOIN project_tasks t ON t.id=e.taskId WHERE e.id='e'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(75, cursor.getInt(0))
                assertEquals(0, cursor.getInt(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(75, cursor.getInt(3))
            }
        }
    }
}
