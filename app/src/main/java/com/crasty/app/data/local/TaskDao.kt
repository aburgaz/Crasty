package com.crasty.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateEpochDay = :day AND repeatUnit IS NULL")
    fun observeOneOffByDay(day: Long): Flow<List<TaskEntity>>

    /** Recurrentes que ya han empezado en [day]; cuáles caen ese día se filtra en Kotlin. */
    @Query("SELECT * FROM tasks WHERE repeatUnit IS NOT NULL AND dateEpochDay <= :day")
    fun observeRecurringStartedBy(day: Long): Flow<List<TaskEntity>>

    @Query("SELECT taskId FROM recurring_completions WHERE dateEpochDay = :day")
    fun observeCompletedRecurringIds(day: Long): Flow<List<Long>>

    @Query(
        "SELECT * FROM tasks WHERE isCompleted = 1 AND repeatUnit IS NULL " +
            "AND COALESCE(completedEpochDay, dateEpochDay) BETWEEN :fromDay AND :toDay",
    )
    fun observeCompleted(fromDay: Long, toDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY isCompleted, dateEpochDay, reminderMinuteOfDay")
    fun observeByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    suspend fun getByProject(projectId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY dateEpochDay, createdAtMillis")
    suspend fun getAll(): List<TaskEntity>

    @Query(
        "SELECT * FROM tasks WHERE reminderMinuteOfDay IS NOT NULL " +
            "AND (repeatUnit IS NOT NULL OR (isCompleted = 0 AND dateEpochDay >= :fromDay))",
    )
    suspend fun getWithUpcomingReminder(fromDay: Long): List<TaskEntity>

    /** Devuelve el id nuevo al insertar, o -1 si actualiza una fila existente. */
    @Upsert
    suspend fun upsert(task: TaskEntity): Long

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Transaction
    suspend fun delete(id: Long) {
        deleteCompletionsOf(id)
        deleteById(id)
    }

    // --- Repeticiones completadas de tareas recurrentes ---

    @Query("SELECT EXISTS(SELECT 1 FROM recurring_completions WHERE taskId = :taskId AND dateEpochDay = :day)")
    suspend fun isRecurringCompleted(taskId: Long, day: Long): Boolean

    @Query("SELECT * FROM recurring_completions")
    suspend fun getAllCompletions(): List<RecurringCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: RecurringCompletionEntity)

    @Query("DELETE FROM recurring_completions WHERE taskId = :taskId AND dateEpochDay = :day")
    suspend fun deleteCompletion(taskId: Long, day: Long)

    @Query("DELETE FROM recurring_completions WHERE taskId = :taskId")
    suspend fun deleteCompletionsOf(taskId: Long)

    @Query("DELETE FROM recurring_completions")
    suspend fun deleteAllCompletions()
}
