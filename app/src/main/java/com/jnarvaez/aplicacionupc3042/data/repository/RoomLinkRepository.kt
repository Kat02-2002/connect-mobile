package com.jnarvaez.aplicacionupc3042.data.repository

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.data.remote.RoomLinkApi
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ApiFailure(val userMessage: String, val status: Int? = null) : Exception(userMessage)

class RoomLinkRepository(private val api: RoomLinkApi = createApi()) {
    companion object {
        const val BASE_URL = "https://ujh0myzrbb.execute-api.us-east-1.amazonaws.com/"
        fun createApi(baseUrl: String = BASE_URL): RoomLinkApi = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
                // An interrupted response may already have created a reservation.
                .retryOnConnectionFailure(false).build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(RoomLinkApi::class.java)
        fun httpMessage(code: Int): String = when (code) {
            400 -> "Los datos enviados no son válidos. Revisa los campos."
            401 -> "Correo o contraseña incorrectos."
            404 -> "No se encontró el recurso solicitado."
            409 -> "La sala ya fue reservada en ese horario. Vuelve a buscar disponibilidad."
            in 500..599 -> "El servidor no está disponible. Inténtalo de nuevo más tarde."
            else -> "No se pudo completar la solicitud. Inténtalo de nuevo."
        }
    }
    private suspend fun <T> request(block: suspend () -> Response<T>): T {
        try {
            val response = block()
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                throw ApiFailure(httpMessage(response.code()), response.code())
            }
            return response.body() ?: throw ApiFailure("El servidor devolvió una respuesta vacía.")
        } catch (e: CancellationException) { throw e
        } catch (e: ApiFailure) { throw e
        } catch (e: SocketTimeoutException) { throw ApiFailure("Se agotó el tiempo de espera. Comprueba tu conexión.")
        } catch (e: JsonParseException) { throw invalid()
        } catch (e: IOException) { throw ApiFailure("No pudimos conectar. Comprueba tu conexión a Internet.")
        } catch (e: RuntimeException) { throw invalid() }
    }
    private fun invalid() = ApiFailure("La respuesta del servidor no tiene el formato esperado.")
    private fun valid(s: Sala): Sala {
        val equipment: List<String>? = s.equipamiento
        if (s.salaId.isNullOrBlank() || s.nombre.isNullOrBlank() || s.sede.isNullOrBlank() ||
            s.piso == null || (s.capacidad ?: 0) <= 0 || s.activa == null || equipment == null ||
            equipment.any { it.isNullOrBlank() }) throw invalid()
        return s
    }
    private fun valid(r: Reserva): Reserva {
        if (r.reservaId.isNullOrBlank() || r.usuarioId.isNullOrBlank() || r.salaId.isNullOrBlank() || r.estado !in listOf("CONFIRMADA", "FINALIZADA")) throw invalid()
        try {
            LocalDate.parse(r.fecha)
            if (!LocalTime.parse(r.horaFin).isAfter(LocalTime.parse(r.horaInicio))) throw invalid()
        } catch (e: RuntimeException) { throw invalid() }
        r.sala?.let { valid(it) }
        return r
    }
    suspend fun login(correo: String, password: String): Usuario {
        val response = request { api.login(LoginRequest(correo, password)) }
        if (response.success == null) throw invalid()
        if (response.success != true) throw ApiFailure("Correo o contraseña incorrectos.", 401)
        val u = response.usuario ?: throw invalid()
        if (u.usuarioId.isNullOrBlank() || u.nombre.isNullOrBlank() || u.correo.isNullOrBlank()) throw invalid()
        return u
    }
    suspend fun salas(filters: Map<String, String> = emptyMap()): List<Sala> {
        val response = request { api.salas(filters) }
        val rooms = response.salas ?: throw invalid()
        return rooms.map(::valid).filter { it.activa == true }
    }
    suspend fun sala(id: String): Sala = valid(request { api.sala(id) }).also {
        if (it.salaId != id) throw invalid()
        if (it.activa != true) throw ApiFailure("Esta sala ya no está activa.")
    }
    suspend fun reservas(userId: String): List<Reserva> {
        val response = request { api.reservas(userId) }
        val bookings = response.reservas ?: throw invalid()
        return bookings.map(::valid).also { if (it.any { r -> r.usuarioId != userId }) throw invalid() }
    }
    suspend fun reserva(id: String, userId: String): Reserva = valid(request { api.reserva(id) }).also {
        if (it.reservaId != id || it.usuarioId != userId) throw invalid()
    }
    suspend fun reservar(body: ReservaRequest): Reserva {
        val response = request { api.reservar(body) }
        if (response.success == null) throw invalid()
        if (response.success != true) throw ApiFailure("No se pudo confirmar la reserva. Consulta Mis reservas antes de reintentar.")
        val r = valid(response.reserva ?: throw invalid())
        if (r.usuarioId != body.usuarioId || r.salaId != body.salaId || r.fecha != body.fecha || r.horaInicio != body.horaInicio || r.horaFin != body.horaFin) throw invalid()
        return r
    }
}
