package com.crasty.app.ui.components

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Los DatePicker de Material 3 trabajan con milisegundos en UTC. */
fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.utcMillisToLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/** Ej.: "sábado, 10 de octubre de 2026" (según el idioma del móvil). */
val fullDateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

/** Ej.: "10 oct 2026". */
val mediumDateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** Ej.: "10 oct 2026, 18:00". */
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

/** Ej.: "Octubre 2026". */
fun YearMonth.label(): String =
    format(DateTimeFormatter.ofPattern("LLLL yyyy")).replaceFirstChar { it.titlecase() }
