package com.crasty.app

import com.crasty.app.data.backup.toBackup
import com.crasty.app.data.backup.toSnapshot
import com.crasty.app.data.local.toEntity
import com.crasty.app.data.local.toProject
import com.crasty.app.data.local.toTask
import com.crasty.app.data.repository.Snapshot
import com.crasty.app.domain.model.Project
import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskMappingTest {

    private val task = Task(
        id = 7,
        title = "Comprar pan",
        notes = "integral",
        date = LocalDate.of(2026, 10, 10),
        reminderTime = LocalTime.of(18, 30),
        isCompleted = true,
        postponedCount = 2,
        createdAt = Instant.parse("2026-10-01T08:00:00Z"),
        projectId = 3,
        completedOn = LocalDate.of(2026, 10, 11),
    )

    private val recurring = Task(
        id = 8,
        title = "Regar plantas",
        date = LocalDate.of(2026, 10, 1),
        createdAt = Instant.parse("2026-10-01T08:00:00Z"),
        recurrence = Recurrence(2, RepeatUnit.WEEKS),
    )

    private val project = Project(
        id = 3,
        name = "Mudanza",
        deadline = LocalDateTime.of(2026, 11, 1, 18, 0),
        createdAt = Instant.parse("2026-10-01T08:00:00Z"),
    )

    @Test
    fun `entity round trip keeps all fields`() {
        assertEquals(task, task.toEntity().toTask())
        assertEquals(recurring, recurring.toEntity().toTask())
        assertEquals(project, project.toEntity().toProject())
    }

    @Test
    fun `backup round trip keeps everything`() {
        val snapshot = Snapshot(
            projects = listOf(project),
            tasks = listOf(task, recurring),
            recurringCompletions = listOf(8L to LocalDate.of(2026, 10, 15)),
        )
        assertEquals(snapshot, snapshot.toBackup(Instant.now()).toSnapshot())
    }
}
