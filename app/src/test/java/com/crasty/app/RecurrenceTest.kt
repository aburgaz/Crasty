package com.crasty.app

import com.crasty.app.domain.model.Recurrence
import com.crasty.app.domain.model.RepeatUnit
import com.crasty.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RecurrenceTest {

    private val start = LocalDate.of(2026, 1, 31)

    @Test
    fun `daily repeats every day from start`() {
        val daily = Recurrence()
        assertFalse(daily.occursOn(start, start.minusDays(1)))
        assertTrue(daily.occursOn(start, start))
        assertTrue(daily.occursOn(start, start.plusDays(40)))
    }

    @Test
    fun `every 3 days skips days in between`() {
        val r = Recurrence(3, RepeatUnit.DAYS)
        assertTrue(r.occursOn(start, start.plusDays(6)))
        assertFalse(r.occursOn(start, start.plusDays(7)))
        assertEquals(start.plusDays(9), r.nextOnOrAfter(start, start.plusDays(7)))
    }

    @Test
    fun `every 2 weeks`() {
        val r = Recurrence(2, RepeatUnit.WEEKS)
        assertTrue(r.occursOn(start, start.plusWeeks(4)))
        assertFalse(r.occursOn(start, start.plusWeeks(1)))
    }

    @Test
    fun `monthly on day 31 falls on the last day of shorter months`() {
        val r = Recurrence(1, RepeatUnit.MONTHS)
        assertTrue(r.occursOn(start, LocalDate.of(2026, 2, 28)))
        assertTrue(r.occursOn(start, LocalDate.of(2026, 3, 31)))
        assertFalse(r.occursOn(start, LocalDate.of(2026, 3, 28)))
        assertEquals(LocalDate.of(2026, 4, 30), r.nextOnOrAfter(start, LocalDate.of(2026, 4, 1)))
    }

    @Test
    fun `task fits before project deadline`() {
        val deadline = LocalDateTime.of(2026, 3, 10, 18, 0)
        val day = LocalDate.of(2026, 3, 10)
        assertTrue(Task(title = "a", date = day).fitsBefore(deadline))
        assertTrue(Task(title = "a", date = day, reminderTime = LocalTime.of(17, 0)).fitsBefore(deadline))
        assertFalse(Task(title = "a", date = day, reminderTime = LocalTime.of(19, 0)).fitsBefore(deadline))
        assertFalse(Task(title = "a", date = day.plusDays(1)).fitsBefore(deadline))
    }
}
