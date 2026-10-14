package com.jnarvaez.aplicacionupc3042

import com.jnarvaez.aplicacionupc3042.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class BookingRulesTest {
    private val now = LocalDateTime.of(2026, 10, 14, 10, 0)
    private val criteria = SearchCriteria("Oficinas", "2026-10-15", "14:00", "15:30", "6", setOf("Pantalla", "Videoconferencia"))
    private fun booking(date: String, end: String, status: String = "CONFIRMADA") =
        Reserva("r", "u", "s", date, "09:00", end, status)

    @Test fun validFutureSearch() { assertNull(BookingRules.validate(criteria, now)) }
    @Test fun missingFields() { assertNotNull(BookingRules.validate(criteria.copy(sede = ""), now)) }
    @Test fun pastDateAndTimeAreRejected() {
        assertNotNull(BookingRules.validate(criteria.copy(fecha = "2026-10-13"), now))
        assertNotNull(BookingRules.validate(criteria.copy(fecha = "2026-10-14", horaInicio = "09:00"), now))
    }
    @Test fun invalidRangesAndFormatsAreRejected() {
        listOf(criteria.copy(horaFin = "14:00"), criteria.copy(horaFin = "13:00"),
            criteria.copy(horaInicio = "25:00"), criteria.copy(fecha = "2026-02-30"),
            criteria.copy(capacidad = "0"), criteria.copy(capacidad = ""), criteria.copy(capacidad = "999999999999"))
            .forEach { assertNotNull(BookingRules.validate(it, now)) }
    }
    @Test fun ongoingMeetingRemainsUpcomingUntilEnd() {
        assertTrue(BookingRules.upcoming(booking("2026-10-14", "10:01"), now))
        assertFalse(BookingRules.upcoming(booking("2026-10-14", "10:00"), now))
        assertEquals("FINALIZADA", BookingRules.status(booking("2026-10-13", "11:00"), now))
        assertFalse(BookingRules.upcoming(booking("2026-10-15", "11:00", "FINALIZADA"), now))
    }
    @Test fun peruTimezoneAndQueryContract() {
        assertEquals(LocalDate.of(2026, 10, 13), Instant.parse("2026-10-14T03:00:00Z").atZone(BookingRules.peru).toLocalDate())
        assertEquals("Pantalla,Videoconferencia", criteria.query()["equipamiento"])
        assertFalse(criteria.copy(equipamiento = emptySet()).query().containsKey("equipamiento"))
    }
}
