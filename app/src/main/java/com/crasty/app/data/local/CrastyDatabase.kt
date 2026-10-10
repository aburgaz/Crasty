package com.crasty.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
abstract class CrastyDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        fun create(context: Context): CrastyDatabase =
            Room.databaseBuilder(context, CrastyDatabase::class.java, "crasty.db").build()
    }
}
