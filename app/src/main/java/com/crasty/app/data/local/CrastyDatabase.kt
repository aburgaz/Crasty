package com.crasty.app.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, ProjectEntity::class, RecurringCompletionEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // v2: proyectos, tareas recurrentes y fecha de completado.
        AutoMigration(from = 1, to = 2),
        // v3: color de los proyectos.
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class CrastyDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao

    companion object {
        fun create(context: Context): CrastyDatabase =
            Room.databaseBuilder(context, CrastyDatabase::class.java, "crasty.db").build()
    }
}
