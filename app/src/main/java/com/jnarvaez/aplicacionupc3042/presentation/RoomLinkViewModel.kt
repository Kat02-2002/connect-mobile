package com.jnarvaez.aplicacionupc3042.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoadState<T>(val value: T? = null, val loading: Boolean = false, val error: String? = null)
data class RoomLinkState(
    val user: Usuario? = null, val loginLoading: Boolean = false, val loginError: String? = null,
    val criteria: SearchCriteria = SearchCriteria(fecha = BookingRules.now().toLocalDate().toString()),
    val catalog: LoadState<List<Sala>> = LoadState(),
    val rooms: LoadState<List<Sala>> = LoadState(),
    val bookings: LoadState<List<Reserva>> = LoadState(),
    val selectedRoom: LoadState<Sala> = LoadState(), val selectedRoomId: String? = null,
    val bookingDetail: LoadState<Reserva> = LoadState(),
    val searchError: String? = null, val posting: Boolean = false,
    val postError: String? = null, val conflict: Boolean = false,
    val uncertainPost: Boolean = false, val createdId: String? = null
)

class RoomLinkViewModel(private val repository: RoomLinkRepository = RoomLinkRepository()) : ViewModel() {
    private val mutable = MutableStateFlow(RoomLinkState())
    val state = mutable.asStateFlow()
    // All jobs are cancelled on logout; late responses cannot restore a previous user's state.
    private var sessionJob = SupervisorJob()
    private fun launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(sessionJob, block = block)
    private fun message(e: Exception) = (e as? ApiFailure)?.userMessage ?: "No se pudo completar la operación. Inténtalo de nuevo."
    private suspend fun <T> load(set: (LoadState<T>) -> Unit, action: suspend () -> T) {
        set(LoadState(loading = true))
        try { set(LoadState(value = action())) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { set(LoadState(error = message(e))) }
    }
    fun login(email: String, password: String) {
        if (state.value.loginLoading) return
        if (email.isBlank() || password.isBlank()) {
            mutable.update { it.copy(loginError = "Ingresa tu correo y contraseña.") }; return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            mutable.update { it.copy(loginError = "Ingresa un correo válido.") }; return
        }
        mutable.update { it.copy(loginLoading = true, loginError = null) }
        launch {
            try {
                val user = repository.login(email.trim(), password)
                mutable.update { it.copy(user = user, loginLoading = false) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutable.update { it.copy(loginLoading = false, loginError = message(e)) } }
        }
    }
    fun logout() {
        sessionJob.cancel(); sessionJob = SupervisorJob()
        mutable.value = RoomLinkState()
    }
    fun criteria(value: SearchCriteria) { mutable.update { it.copy(criteria = value, searchError = null) } }
    fun catalog() {
        if (state.value.catalog.loading) return
        launch { load({ s -> mutable.update { it.copy(catalog = s) } }) { repository.salas() } }
    }
    fun search(): Boolean {
        val c = state.value.criteria
        val error = BookingRules.validate(c)
        mutable.update { it.copy(searchError = error) }
        if (error != null) return false
        if (!state.value.rooms.loading) launch {
            load({ s -> mutable.update { it.copy(rooms = s) } }) { repository.salas(c.query()) }
        }
        return true
    }
    fun bookings() {
        val user = state.value.user ?: return
        if (state.value.bookings.loading) return
        launch { load({ s -> mutable.update { it.copy(bookings = s) } }) { repository.reservas(user.usuarioId) } }
    }
    fun select(id: String) {
        mutable.update { it.copy(selectedRoom = LoadState(loading = true), selectedRoomId = id, postError = null,
            conflict = false, uncertainPost = false, createdId = null) }
        launch { load({ s -> mutable.update { it.copy(selectedRoom = s) } }) { repository.sala(id) } }
    }
    fun detail(id: String) {
        val user = state.value.user ?: return
        launch {
            load({ s -> mutable.update { it.copy(bookingDetail = s) } }) {
                val r = repository.reserva(id, user.usuarioId)
                if (r.sala != null) r else r.copy(sala = repository.sala(r.salaId))
            }
        }
    }
    fun confirm() {
        val s = state.value
        if (s.posting || s.conflict || s.uncertainPost || s.createdId != null) return
        val user = s.user ?: return
        val room = s.selectedRoom.value ?: return
        val error = BookingRules.validate(s.criteria)
        if (error != null) { mutable.update { it.copy(postError = error) }; return }
        mutable.update { it.copy(posting = true, postError = null) }
        launch {
            try {
                val c = s.criteria
                val r = repository.reservar(ReservaRequest(user.usuarioId, room.salaId, c.fecha, c.horaInicio, c.horaFin))
                mutable.update { it.copy(posting = false, createdId = r.reservaId) }
                bookings()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                val code = (e as? ApiFailure)?.status
                val uncertain = code == null || code >= 500
                mutable.update { it.copy(posting = false, conflict = code == 409,
                    uncertainPost = uncertain, postError = message(e) +
                        if (uncertain) " Consulta Mis reservas antes de volver a reservar; la solicitud pudo haberse registrado." else "") }
            }
        }
    }
    fun acknowledgeCreated() { mutable.update { it.copy(createdId = null) } }
    override fun onCleared() { sessionJob.cancel() }
}
