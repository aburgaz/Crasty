package com.crasty.app

import com.crasty.app.data.backup.toBackup
import com.crasty.app.data.local.toEntity
import com.crasty.app.data.local.toTask
import com.crasty.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import com.crasty.app.data.backup.toTask as backupToTask

class TaskMappingTest {

    private val task = Task(
        id = 7,
        title = "Comprar pan",
        notes = "integral",
        date = LocalDate.of(2026, 10, 10),
        reminderTime = LocalTime.of(18, 30),
        postponedCount = 2,
        createdAt = Instant.parse("2026-10-01T08:00:00Z"),
    )

    @Test
    fun `entity round trip keeps all fields`() {
        assertEquals(task, task.toEntity().toTask())
    }

    @Test
    fun `backup round trip keeps everything except id`() {
        assertEquals(task.copy(id = 0), task.toBackup().backupToTask())
    }
}
