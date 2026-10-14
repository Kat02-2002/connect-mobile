package com.jnarvaez.aplicacionupc3042.data.model

data class Usuario(val usuarioId: String, val nombre: String, val correo: String)
data class LoginRequest(val correo: String, val password: String)
data class LoginResponse(val success: Boolean?, val usuario: Usuario?)
data class Sala(
    val salaId: String, val nombre: String, val sede: String, val piso: Int?,
    val capacidad: Int?, val equipamiento: List<String> = emptyList(),
    val imagenUrl: String? = null, val activa: Boolean? = true
)
data class SalasResponse(val total: Int, val salas: List<Sala>)
data class Reserva(
    val reservaId: String, val usuarioId: String, val salaId: String,
    val fecha: String, val horaInicio: String, val horaFin: String,
    val estado: String, val fechaCreacion: String? = null, val sala: Sala? = null
)
data class ReservasResponse(val total: Int, val reservas: List<Reserva>)
data class ReservaRequest(
    val usuarioId: String, val salaId: String, val fecha: String,
    val horaInicio: String, val horaFin: String
)
data class CrearReservaResponse(val success: Boolean?, val mensaje: String?, val reserva: Reserva?)
data class SearchCriteria(
    val sede: String = "", val fecha: String = "", val horaInicio: String = "",
    val horaFin: String = "", val capacidad: String = "1", val equipamiento: Set<String> = emptySet()
) {
    fun query(): Map<String, String> = mapOf(
        "sede" to sede, "fecha" to fecha, "horaInicio" to horaInicio,
        "horaFin" to horaFin, "capacidad" to capacidad,
        "equipamiento" to equipamiento.joinToString(",")
    ).filterValues { it.isNotBlank() }
}
