package com.jnarvaez.aplicacionupc3042.presentation.screens.reservas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.presentation.*
import com.jnarvaez.aplicacionupc3042.presentation.common.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


@Composable
fun BookingsScreen(state: RoomLinkState, now: LocalDateTime, refresh: () -> Unit,
                   search: () -> Unit, detail: (String) -> Unit) {
    LaunchedEffect(Unit) { refresh() }
    var previous by rememberSaveable { mutableStateOf(false) }
    ScreenColumn {
        Text("Organiza tus próximas reuniones y consulta tu historial.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = refresh, enabled = !state.bookings.loading) { Text("Actualizar reservas") }
        RemoteContent(state.bookings, refresh) { list ->
            val next = list.filter { BookingRules.upcoming(it, now) }.sortedBy { "${it.fecha} ${it.horaInicio}" }
            val past = list.filterNot { BookingRules.upcoming(it, now) }.sortedByDescending { "${it.fecha} ${it.horaInicio}" }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val colors = SegmentedButtonDefaults.colors(activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary)
                SegmentedButton(selected = !previous, onClick = { previous = false }, shape = SegmentedButtonDefaults.itemShape(0, 2), colors = colors) { Text("Próximas (${next.size})") }
                SegmentedButton(selected = previous, onClick = { previous = true }, shape = SegmentedButtonDefaults.itemShape(1, 2), colors = colors) { Text("Anteriores (${past.size})") }
            }
            Text(if (previous) "Reservas anteriores" else "Próximas reservas", style = MaterialTheme.typography.titleLarge)
            val visible = if (previous) past else next
            if (visible.isEmpty()) {
                EmptyNotice(if (previous) "Todavía no tienes historial" else "No tienes reservas próximas",
                    if (previous) "Aquí aparecerán tus reuniones finalizadas." else "Busca una sala para tu próxima reunión.")
                if (!previous) PrimaryAction("Buscar sala", onClick = search)
            }
            visible.forEach { r -> BookingSummary(r, now) { detail(r.reservaId) } }
        }
    }
}
