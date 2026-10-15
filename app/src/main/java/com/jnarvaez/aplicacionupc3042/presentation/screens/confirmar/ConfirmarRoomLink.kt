package com.jnarvaez.aplicacionupc3042.presentation.screens.confirmar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.presentation.*
import com.jnarvaez.aplicacionupc3042.presentation.common.*


@Composable
fun ConfirmScreen(state: RoomLinkState, retry: () -> Unit, confirm: () -> Unit, back: () -> Unit,
                  search: () -> Unit, bookings: () -> Unit) {
    BackHandler(enabled = state.posting) { }
    ScreenColumn {
        Heading("Confirmar reserva", "Revisa los detalles antes de finalizar tu reserva.")
        RemoteContent(state.selectedRoom, retry) { room ->
            Panel {
                RoomMetadata(room, image = true)
                HorizontalDivider()
                Text("Detalle de la reunión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(BookingRules.dateLabel(state.criteria.fecha))
                Text("${state.criteria.horaInicio} – ${state.criteria.horaFin} · Hora de Perú")
                Text("Asistentes: ${state.criteria.capacidad}")
                state.user?.let { Text("Responsable: ${it.nombre}"); Text(it.correo) }
            }
            state.postError?.let { ErrorNotice(it) }
            if (state.conflict) PrimaryAction("Volver a buscar disponibilidad", onClick = search)
            else if (state.uncertainPost) PrimaryAction("Consultar Mis reservas", onClick = bookings)
            else PrimaryAction("Confirmar reserva", loading = state.posting, onClick = confirm)
            OutlinedButton(onClick = back, enabled = !state.posting, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Volver sin reservar") }
        }
    }
}
