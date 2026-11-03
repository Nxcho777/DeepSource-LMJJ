package cl.duoc.deepsource.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, UserEntity::class, TimeEntryEntity::class, SeedStateEntity::class],
    version = 1,
    exportSchema = true
)
abstract class DeepSourceDatabase : RoomDatabase() {
    abstract fun dao(): DeepSourceDao

    companion object {
        private const val DATABASE_NAME = "deepsource.db"

        @Volatile private var instance: DeepSourceDatabase? = null

        fun getInstance(context: Context): DeepSourceDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, DeepSourceDatabase::class.java, DATABASE_NAME)
                .build()
                .also { instance = it }
        }
    }
}
