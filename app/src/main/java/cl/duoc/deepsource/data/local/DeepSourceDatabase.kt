
package cl.duoc.deepsource.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ProjectEntity::class, UserEntity::class, TimeEntryEntity::class, SeedStateEntity::class, ProjectTaskEntity::class],
    version = 9,
    exportSchema = true
)
abstract class DeepSourceDatabase : RoomDatabase() {
    abstract fun dao(): DeepSourceDao

    companion object {
        private const val DATABASE_NAME = "deepsource.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN iconIndex INTEGER NOT NULL DEFAULT 0")

                // Keep the original icons for the four bundled sample projects.
                db.execSQL("UPDATE projects SET iconIndex = 1 WHERE id = 'project-2'")
                db.execSQL("UPDATE projects SET iconIndex = 2 WHERE id = 'project-3'")
                db.execSQL("UPDATE projects SET iconIndex = 3 WHERE id = 'project-4'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Restore the correct icons for the bundled sample projects.
                db.execSQL("UPDATE projects SET iconIndex = 0 WHERE id = 'project-1'")
                db.execSQL("UPDATE projects SET iconIndex = 1 WHERE id = 'project-2'")
                db.execSQL("UPDATE projects SET iconIndex = 2 WHERE id = 'project-3'")
                db.execSQL("UPDATE projects SET iconIndex = 3 WHERE id = 'project-4'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USER'")
                db.execSQL("INSERT OR IGNORE INTO users (id, name, email, role) VALUES ('user-admin', 'Administrador Demo', 'admin@deepsource.cl', 'ADMIN')")
            }
        }

        // Reconstruye únicamente las horas preexistentes sin historial en proyectos demo.
        // No suma minutos a projects: evita duplicar el avance y preserva registros reales.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                for (projectId in listOf("project-1", "project-2", "project-3", "project-4")) {
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO time_entries
                            (id, projectId, userId, durationMinutes, date, note)
                        SELECT 'demo-backfill-' || p.id, p.id, 'user-1',
                               p.registeredMinutes - COALESCE(
                                   (SELECT SUM(t.durationMinutes)
                                    FROM time_entries t WHERE t.projectId = p.id), 0
                               ),
                               '2026-10-03', 'Horas iniciales de ejemplo (registro reconstruido)'
                        FROM projects p
                        WHERE p.id = '$projectId'
                          AND EXISTS (SELECT 1 FROM users WHERE id = 'user-1')
                          AND p.registeredMinutes > COALESCE(
                              (SELECT SUM(t.durationMinutes)
                               FROM time_entries t WHERE t.projectId = p.id), 0
                          )
                        """.trimIndent()
                    )
                }
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `project_tasks` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `estimatedMinutes` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_project_tasks_projectId` ON `project_tasks` (`projectId`)")
                db.execSQL("ALTER TABLE time_entries ADD COLUMN taskId TEXT")
                // Cada proyecto existente recibe una tarea general. No altera sus minutos acumulados.
                db.execSQL("INSERT INTO project_tasks (id, projectId, name, description, estimatedMinutes) SELECT 'general-' || id, id, 'Trabajo general', 'Horas registradas antes de organizar tareas', totalMinutes FROM projects")
                db.execSQL("UPDATE time_entries SET taskId = 'general-' || projectId WHERE taskId IS NULL")
            }
        }

        // Tareas demostrativas para los cuatro proyectos incluidos en DeepSource.
        // No reasigna fichajes históricos ni modifica minutos de los proyectos.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""INSERT OR IGNORE INTO project_tasks (id, projectId, name, description, estimatedMinutes) VALUES ('demo-task-project-1-1', 'project-1', 'Diseño de interfaz', 'Diseñar pantallas y componentes de Atlas', 600),
                        ('demo-task-project-1-2', 'project-1', 'Desarrollo de funcionalidades', 'Implementar módulos de Atlas', 1200),
                        ('demo-task-project-1-3', 'project-1', 'Pruebas y documentación', 'Validar funcionalidades y documentar', 600),
                        ('demo-task-project-2-1', 'project-2', 'Diseño móvil', 'Diseñar la experiencia de App Boreal', 900),
                        ('demo-task-project-2-2', 'project-2', 'Desarrollo Android', 'Implementar pantallas y lógica móvil', 1800),
                        ('demo-task-project-2-3', 'project-2', 'Pruebas de aplicación', 'Validación y correcciones', 900),
                        ('demo-task-project-3-1', 'project-3', 'Diseño del portal', 'Diseñar vistas de Portal Nova', 600),
                        ('demo-task-project-3-2', 'project-3', 'Desarrollo web', 'Implementar funcionalidades del portal', 1800),
                        ('demo-task-project-3-3', 'project-3', 'Integración y pruebas', 'Integraciones y validación', 600),
                        ('demo-task-project-4-1', 'project-4', 'Modelado CRM', 'Definir entidades y procesos del CRM', 1200),
                        ('demo-task-project-4-2', 'project-4', 'Implementación CRM', 'Construir módulos y flujos', 2400),
                        ('demo-task-project-4-3', 'project-4', 'Pruebas y entrega', 'Verificar y documentar la solución', 1800)""".trimIndent())
            }
        }

        internal val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE project_tasks ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile private var instance: DeepSourceDatabase? = null

        fun getInstance(context: Context): DeepSourceDatabase = instance ?: synchronized(this) {
            instance ?: openDatabase(context, DATABASE_NAME).also { instance = it }
        }

        internal fun openDatabase(context: Context, name: String): DeepSourceDatabase =
            Room.databaseBuilder(context.applicationContext, DeepSourceDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build()
    }
}
