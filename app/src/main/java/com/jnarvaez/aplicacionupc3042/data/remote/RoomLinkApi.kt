package com.jnarvaez.aplicacionupc3042.data.remote

import com.jnarvaez.aplicacionupc3042.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface RoomLinkApi {
    @POST("login") suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
    @GET("salas") suspend fun salas(@QueryMap filters: Map<String, String> = emptyMap()): Response<SalasResponse>
    @GET("salas/{id}") suspend fun sala(@Path("id") id: String): Response<Sala>
    @POST("reservas") suspend fun reservar(@Body body: ReservaRequest): Response<CrearReservaResponse>
    @GET("reservas") suspend fun reservas(@Query("usuarioId") usuarioId: String): Response<ReservasResponse>
    @GET("reservas/{id}") suspend fun reserva(@Path("id") id: String): Response<Reserva>
}
