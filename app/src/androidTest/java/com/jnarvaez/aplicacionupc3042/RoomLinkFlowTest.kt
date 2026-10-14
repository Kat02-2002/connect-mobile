package com.jnarvaez.aplicacionupc3042

import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import java.io.File
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.data.remote.RoomLinkApi
import com.jnarvaez.aplicacionupc3042.data.repository.RoomLinkRepository
import com.jnarvaez.aplicacionupc3042.presentation.RoomLinkViewModel
import com.jnarvaez.aplicacionupc3042.presentation.navigation.AppNavigation
import com.jnarvaez.aplicacionupc3042.ui.theme.AplicacionUPC3042Theme
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

/** Fixtures are confined to tests; production always uses the AWS Retrofit API. */
class RoomLinkFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val api = TestApi()
    private val vm = RoomLinkViewModel(RoomLinkRepository(api))
    private val criteria = SearchCriteria("Oficinas", BookingRules.now().toLocalDate().plusDays(1).toString(), "14:00", "15:30", "6", setOf("Pantalla"))

    private fun screenshot(name: String) {
        val directory = File(rule.activity.getExternalFilesDir(null), "qa")
        directory.mkdirs()
        rule.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun start() {
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            AplicacionUPC3042Theme { AppNavigation(vm = vm) }
        } }
        screenshot("01-login")
        rule.onNodeWithText("Correo corporativo").performTextInput("test@example.com")
        rule.onNodeWithText("Contraseña").performTextInput("test-input")
        rule.onNodeWithText("Iniciar sesión →").performScrollTo().performClick()
        rule.waitUntil(5_000) { vm.state.value.user != null }
        rule.onNodeWithText("Hola, Demo 👋").assertIsDisplayed()
        screenshot("02-dashboard")
    }
    private fun searchAndSelect() {
        rule.onNodeWithText("Buscar sala").performScrollTo().performClick()
        rule.runOnIdle { vm.criteria(criteria) }
        rule.waitUntil(5_000) { vm.state.value.catalog.value != null }
        screenshot("03-search")
        rule.onNodeWithText("Buscar salas").performScrollTo().performClick()
        rule.waitUntil(5_000) { vm.state.value.rooms.value != null }
        screenshot("04-rooms")
        rule.onNodeWithText("Seleccionar sala →").performScrollTo().performClick()
        rule.waitUntil(5_000) { vm.state.value.selectedRoom.value != null }
        screenshot("05-confirm")
    }
    @Test fun bookingFlowPreservesFiltersAndLogoutClearsBackStack() {
        start(); searchAndSelect()
        rule.onNodeWithText("Volver sin reservar").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(criteria, vm.state.value.criteria); assertEquals(0, api.postCount) }
        rule.onNodeWithText("Seleccionar sala →").performScrollTo().performClick()
        rule.onNode(hasText("Confirmar reserva") and hasClickAction()).performScrollTo().performClick()
        rule.waitUntil(5_000) { vm.state.value.bookingDetail.value != null }
        rule.onNodeWithText("Identificador de reserva").performScrollTo().assertIsDisplayed()
        screenshot("07-detail")
        rule.runOnIdle { assertEquals(1, api.postCount); assertEquals("u", api.lastRequest?.usuarioId) }
        rule.onNodeWithContentDescription("Volver").performClick()
        rule.onNodeWithText("Próximas (1)").assertIsDisplayed()
        screenshot("06-bookings")
        rule.onNodeWithText("Anteriores (0)").performClick()
        rule.onNodeWithText("Todavía no tienes historial").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Cerrar sesión").performClick()
        rule.onNodeWithText("Correo corporativo").assertIsDisplayed()
        rule.runOnIdle { assertNull(vm.state.value.user); assertNull(vm.state.value.bookings.value) }
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.runOnIdle { assertNull(vm.state.value.user) }
    }
    @Test fun conflictNeverShowsSuccessfulReservation() {
        api.conflict = true
        start(); searchAndSelect()
        rule.onNode(hasText("Confirmar reserva") and hasClickAction()).performScrollTo().performClick()
        rule.waitUntil(5_000) { vm.state.value.conflict }
        rule.onNodeWithText("La sala ya fue reservada en ese horario. Vuelve a buscar disponibilidad.")
            .performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Volver a buscar disponibilidad").performScrollTo().performClick()
        rule.onNodeWithText("Buscar sala disponible").assertIsDisplayed()
        rule.runOnIdle { assertEquals(criteria, vm.state.value.criteria); assertNull(vm.state.value.createdId) }
    }
    private class TestApi : RoomLinkApi {
        val room = Sala("s", "Sala de prueba", "Oficinas", 3, 8, listOf("Pantalla"))
        val list = mutableListOf<Reserva>()
        var postCount = 0
        var lastRequest: ReservaRequest? = null
        var conflict = false
        override suspend fun login(body: LoginRequest) = Response.success(LoginResponse(true, Usuario("u", "Demo Prueba", body.correo)))
        override suspend fun salas(filters: Map<String, String>) = Response.success(SalasResponse(1, listOf(room)))
        override suspend fun sala(id: String) = Response.success(room)
        override suspend fun reservar(body: ReservaRequest): Response<CrearReservaResponse> {
            postCount++; lastRequest = body
            if (conflict) return Response.error(409, "{}".toResponseBody())
            val r = Reserva("r", body.usuarioId, body.salaId, body.fecha, body.horaInicio, body.horaFin, "CONFIRMADA", sala = room)
            list.add(r)
            return Response.success(201, CrearReservaResponse(true, "Confirmada", r))
        }
        override suspend fun reservas(usuarioId: String) = Response.success(ReservasResponse(list.size, list.toList()))
        override suspend fun reserva(id: String) = Response.success(list.first { it.reservaId == id })
    }
}
