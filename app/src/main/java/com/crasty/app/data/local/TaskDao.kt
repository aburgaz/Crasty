package com.crasty.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateEpochDay = :day ORDER BY isCompleted, createdAtMillis")
    fun observeByDay(day: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY dateEpochDay, createdAtMillis")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND reminderMinuteOfDay IS NOT NULL AND dateEpochDay >= :fromDay")
    suspend fun getPendingWithReminder(fromDay: Long): List<TaskEntity>

    /** Devuelve el id nuevo al insertar, o -1 si actualiza una fila existente. */
    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(tasks: List<TaskEntity>) {
        deleteAll()
        insertAll(tasks)
    }
}
