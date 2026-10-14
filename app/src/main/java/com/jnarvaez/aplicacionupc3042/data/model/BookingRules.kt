package com.jnarvaez.aplicacionupc3042.data.model

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

object BookingRules {
    val peru: ZoneId = ZoneId.of("America/Lima")
    fun now(): LocalDateTime = LocalDateTime.now(peru)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)
    fun validate(c: SearchCriteria, current: LocalDateTime = now()): String? {
        if (c.sede.isBlank() || c.fecha.isBlank() || c.horaInicio.isBlank() || c.horaFin.isBlank())
            return "Completa la sede, fecha y horario de la reunión."
        val date = runCatching { LocalDate.parse(c.fecha) }.getOrNull()
            ?: return "Selecciona una fecha válida."
        val start = runCatching { LocalTime.parse(c.horaInicio, timeFormat) }.getOrNull()
            ?: return "La hora de inicio debe tener el formato HH:mm."
        val end = runCatching { LocalTime.parse(c.horaFin, timeFormat) }.getOrNull()
            ?: return "La hora de fin debe tener el formato HH:mm."
        if (date.isBefore(current.toLocalDate())) return "La fecha no puede ser anterior a hoy."
        if (!end.isAfter(start)) return "La hora de fin debe ser posterior a la de inicio."
        if (!date.atTime(start).isAfter(current)) return "Selecciona un horario de inicio futuro."
        if ((c.capacidad.toIntOrNull() ?: 0) <= 0) return "Ingresa una cantidad de asistentes mayor que cero."
        return null
    }
    fun end(r: Reserva): LocalDateTime = LocalDate.parse(r.fecha).atTime(LocalTime.parse(r.horaFin))
    fun upcoming(r: Reserva, current: LocalDateTime = now()): Boolean = r.estado == "CONFIRMADA" && end(r).isAfter(current)
    fun status(r: Reserva, current: LocalDateTime = now()): String = if (upcoming(r, current)) "CONFIRMADA" else "FINALIZADA"
    fun dateLabel(value: String): String = runCatching {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE")))
    }.getOrDefault(value)
}
