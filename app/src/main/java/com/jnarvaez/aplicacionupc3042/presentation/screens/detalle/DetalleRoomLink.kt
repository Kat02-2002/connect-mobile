package com.jnarvaez.aplicacionupc3042.presentation.screens.detallereserva

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
fun BookingDetailScreen(state: RoomLinkState, now: LocalDateTime, retry: () -> Unit) {
    ScreenColumn {
        Heading("Detalle de reserva")
        RemoteContent(state.bookingDetail, retry) { r ->
            Panel {
                StatusBadge(BookingRules.status(r, now))
                r.sala?.let { RoomMetadata(it, image = true) }
                HorizontalDivider()
                Text(BookingRules.dateLabel(r.fecha), fontWeight = FontWeight.SemiBold)
                Text("${r.horaInicio} – ${r.horaFin} · Hora de Perú")
                Text("Identificador de reserva", style = MaterialTheme.typography.labelMedium)
                Text(r.reservaId, style = MaterialTheme.typography.bodySmall)
                state.user?.let { Text("Responsable: ${it.nombre}") }
            }
        }
    }
}
