package com.crasty.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class RepeatUnit { DAYS, WEEKS, MONTHS }

/** Cada cuánto se repite una tarea recurrente, contando desde su día de inicio. */
data class Recurrence(
    val interval: Int = 1,
    val unit: RepeatUnit = RepeatUnit.DAYS,
) {
    init {
        require(interval >= 1)
    }

    fun occursOn(start: LocalDate, day: LocalDate): Boolean =
        !day.isBefore(start) && nextOnOrAfter(start, day) == day

    /** Primera repetición que cae en [from] o después. */
    fun nextOnOrAfter(start: LocalDate, from: LocalDate): LocalDate {
        if (!from.isAfter(start)) return start
        var k = when (unit) {
            RepeatUnit.DAYS -> ChronoUnit.DAYS.between(start, from)
            RepeatUnit.WEEKS -> ChronoUnit.WEEKS.between(start, from)
            RepeatUnit.MONTHS -> ChronoUnit.MONTHS.between(start.withDayOfMonth(1), from.withDayOfMonth(1))
        } / interval
        while (occurrence(start, k).isBefore(from)) k++
        return occurrence(start, k)
    }

    // Siempre se calcula desde el inicio para que, p. ej., el día 31 no se quede en 28 tras febrero.
    private fun occurrence(start: LocalDate, k: Long): LocalDate = when (unit) {
        RepeatUnit.DAYS -> start.plusDays(k * interval)
        RepeatUnit.WEEKS -> start.plusWeeks(k * interval)
        RepeatUnit.MONTHS -> start.plusMonths(k * interval)
    }
}
