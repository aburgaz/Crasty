package com.crasty.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query(
        "SELECT p.*, COUNT(t.id) AS totalTasks, COALESCE(SUM(t.isCompleted), 0) AS doneTasks " +
            "FROM projects p LEFT JOIN tasks t ON t.projectId = p.id " +
            "WHERE p.isCompleted = :completed GROUP BY p.id " +
            "ORDER BY p.deadlineEpochDay, p.deadlineMinuteOfDay",
    )
    fun observeSummaries(completed: Boolean): Flow<List<ProjectSummaryRow>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects")
    suspend fun getAll(): List<ProjectEntity>

    /** Devuelve el id nuevo al insertar, o -1 si actualiza una fila existente. */
    @Upsert
    suspend fun upsert(project: ProjectEntity): Long

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM projects")
    suspend fun deleteAll()
}
