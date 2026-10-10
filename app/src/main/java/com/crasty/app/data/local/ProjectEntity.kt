package com.crasty.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.ProjectSummary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val deadlineEpochDay: Long,
    val deadlineMinuteOfDay: Int,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
    @ColumnInfo(defaultValue = "0") val colorIndex: Int = 0,
)

/** Id y color de un proyecto. */
data class ProjectColorRow(val id: Long, val colorIndex: Int)

/** Proyecto con el recuento de sus tareas, para la lista de proyectos. */
data class ProjectSummaryRow(
    @Embedded val project: ProjectEntity,
    val totalTasks: Int,
    val doneTasks: Int,
)

fun ProjectEntity.toProject() = Project(
    id = id,
    name = name,
    deadline = LocalDate.ofEpochDay(deadlineEpochDay).atTime(LocalTime.ofSecondOfDay(deadlineMinuteOfDay * 60L)),
    isCompleted = isCompleted,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    colorIndex = colorIndex,
)

fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    deadlineEpochDay = deadline.toLocalDate().toEpochDay(),
    deadlineMinuteOfDay = deadline.hour * 60 + deadline.minute,
    isCompleted = isCompleted,
    createdAtMillis = createdAt.toEpochMilli(),
    colorIndex = colorIndex,
)

fun ProjectSummaryRow.toSummary() = ProjectSummary(project.toProject(), totalTasks, doneTasks)
