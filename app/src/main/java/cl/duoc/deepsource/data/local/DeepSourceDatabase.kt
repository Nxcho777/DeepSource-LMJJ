package cl.duoc.deepsource.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ProjectEntity::class, UserEntity::class, TimeEntryEntity::class, SeedStateEntity::class],
    version = 3,
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

        @Volatile private var instance: DeepSourceDatabase? = null

        fun getInstance(context: Context): DeepSourceDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, DeepSourceDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { instance = it }
        }
    }
}
