package com.jnarvaez.aplicacionupc3042.presentation.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.presentation.*
import com.jnarvaez.aplicacionupc3042.presentation.common.*
import java.time.LocalDateTime


@Composable
fun DashboardScreen(state: RoomLinkState, now: LocalDateTime, reload: () -> Unit,
                    search: () -> Unit, bookings: () -> Unit, detail: (String) -> Unit) {
    LaunchedEffect(Unit) { reload() }
    ScreenColumn {
        Heading("Hola, ${state.user?.nombre?.substringBefore(' ') ?: ""} 👋", "Bienvenido a tu panel de reuniones")
        Text("PRÓXIMA REUNIÓN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        RemoteContent(state.bookings, reload) { list ->
            val upcoming = list.filter { BookingRules.upcoming(it, now) }.sortedBy { "${it.fecha} ${it.horaInicio}" }
            val next = upcoming.firstOrNull()
            if (next == null) EmptyNotice("Tu próxima reunión empieza aquí", "Todavía no tienes reservas próximas. Encuentra una sala para tu equipo.")
            else BookingSummary(next, now) { detail(next.reservaId) }
            PrimaryAction("Buscar sala", onClick = search)
            Panel {
                Text("Mis reservas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Tienes ${upcoming.size} reservas próximas.")
                OutlinedButton(onClick = bookings, modifier = Modifier.fillMaxWidth()) { Text("Ver mis reservas →") }
            }
        }
        if (state.bookings.value == null) {
            PrimaryAction("Buscar sala", onClick = search)
            OutlinedButton(onClick = bookings, modifier = Modifier.fillMaxWidth()) { Text("Mis reservas") }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cultura de colaboración", fontWeight = FontWeight.SemiBold)
                Text("Finaliza tu reunión a tiempo y deja el espacio listo para el siguiente equipo.")
            }
        }
    }
}
