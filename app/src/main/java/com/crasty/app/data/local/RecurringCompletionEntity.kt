package com.crasty.app.data.local

import androidx.room.Entity

/** Marca una repetición concreta (un día) de una tarea recurrente como completada. */
@Entity(tableName = "recurring_completions", primaryKeys = ["taskId", "dateEpochDay"])
data class RecurringCompletionEntity(
    val taskId: Long,
    val dateEpochDay: Long,
)
