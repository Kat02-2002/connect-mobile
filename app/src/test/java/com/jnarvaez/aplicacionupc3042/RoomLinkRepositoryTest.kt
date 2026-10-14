package com.jnarvaez.aplicacionupc3042

import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.data.repository.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.jnarvaez.aplicacionupc3042.data.remote.RoomLinkApi
import org.junit.*
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class RoomLinkRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: RoomLinkRepository
    private val room = """{"salaId":"s","nombre":"Sala Lima","sede":"Oficinas","piso":3,"capacidad":8,"equipamiento":["Pantalla"],"activa":true}"""
    private val booking = """{"reservaId":"r","usuarioId":"u","salaId":"s","fecha":"2026-10-24","horaInicio":"14:00","horaFin":"15:30","estado":"CONFIRMADA"}"""
    @Before fun setup() { server = MockWebServer(); server.start(); repo = RoomLinkRepository(RoomLinkRepository.createApi(server.url("/").toString())) }
    @After fun teardown() { server.shutdown() }
    private fun respond(body: String, code: Int = 200) { server.enqueue(MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")) }
    private fun failure(block: suspend () -> Unit): ApiFailure = runBlocking {
        try { block(); fail("Expected API failure"); error("unreachable") } catch (e: ApiFailure) { e }
    }
    @Test fun realRetrofitLoginContractWithoutAuthorization() = runBlocking {
        respond("""{"success":true,"usuario":{"usuarioId":"u","nombre":"Demo","correo":"demo@example.com"}}""")
        assertEquals("u", repo.login("demo@example.com", "test-input").usuarioId)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method); assertEquals("/login", request.path)
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("\"correo\":\"demo@example.com\""))
    }
    @Test fun allHttpErrorsBecomeReadableFailures() {
        listOf(400, 401, 404, 409, 500).forEach { code ->
            respond("""{"success":false,"error":"internal trace"}""", code)
            val error = failure { repo.login("a@b.com", "x") }
            assertEquals(code, error.status); assertFalse(error.userMessage.contains("internal trace"))
        }
    }
    @Test fun malformedAndMissingJsonAreRejected() {
        listOf("not json", "{}", "{\"success\":true}", "{\"success\":true,\"usuario\":{}}").forEach {
            respond(it); assertNotNull(failure { repo.login("a@b.com", "x") })
        }
    }
    @Test fun searchFiltersAndRoomDetails() = runBlocking {
        respond("{\"total\":1,\"salas\":[$room]}")
        assertEquals("s", repo.salas(mapOf("sede" to "Oficinas", "capacidad" to "6", "equipamiento" to "Pantalla")).single().salaId)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertTrue(request.path!!.contains("capacidad=6")); assertTrue(request.path!!.contains("equipamiento=Pantalla"))
        respond(room); assertEquals("Sala Lima", repo.sala("s").nombre)
        assertEquals("/salas/s", server.takeRequest(2, TimeUnit.SECONDS)!!.path)
    }
    @Test fun emptyListAndInvalidRoomAreHandled() = runBlocking {
        respond("{\"total\":0,\"salas\":[]}"); assertTrue(repo.salas().isEmpty())
        respond("{\"total\":1,\"salas\":[{}]}"); assertNotNull(failure { repo.salas() })
    }
    @Test fun createHistoryDetailAndSequentialConflict() = runBlocking {
        val body = ReservaRequest("u", "s", "2026-10-24", "14:00", "15:30")
        respond("{\"success\":true,\"reserva\":$booking}", 201)
        assertEquals("r", repo.reservar(body).reservaId)
        val post = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/reservas", post.path); assertTrue(post.body.readUtf8().contains("\"usuarioId\":\"u\""))
        respond("{\"success\":false}", 409); assertEquals(409, failure { repo.reservar(body) }.status)
        server.takeRequest(2, TimeUnit.SECONDS)
        respond("{\"total\":1,\"reservas\":[$booking]}")
        assertEquals("r", repo.reservas("u").single().reservaId)
        assertEquals("/reservas?usuarioId=u", server.takeRequest(2, TimeUnit.SECONDS)!!.path)
        respond(booking); assertEquals("s", repo.reserva("r", "u").salaId)
        assertEquals("/reservas/r", server.takeRequest(2, TimeUnit.SECONDS)!!.path)
    }
    @Test fun foreignUserBookingsAreRejected() {
        respond("{\"total\":1,\"reservas\":[$booking]}"); assertNotNull(failure { repo.reservas("another") })
        respond(booking); assertNotNull(failure { repo.reserva("r", "another") })
    }
    @Test fun disconnectIsReportedWithoutTechnicalTrace() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        val error = failure { repo.salas() }
        assertTrue(error.userMessage.contains("conexión"))
    }
    @Test fun timeoutIsReported() {
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().readTimeout(100, TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(RoomLinkApi::class.java)
        val shortTimeoutRepo = RoomLinkRepository(api)
        server.enqueue(MockResponse().setBody("{\"salas\":[]}").setHeadersDelay(500, TimeUnit.MILLISECONDS))
        assertTrue(failure { shortTimeoutRepo.salas() }.userMessage.contains("tiempo de espera"))
    }
}
