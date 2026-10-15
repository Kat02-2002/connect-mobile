package com.jnarvaez.aplicacionupc3042.presentation.screens.disponibles

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
fun AvailableRoomsScreen(state: RoomLinkState, retry: () -> Unit, edit: () -> Unit, select: (String) -> Unit) {
    ScreenColumn {
        Heading("Salas disponibles", "${state.criteria.sede} · ${BookingRules.dateLabel(state.criteria.fecha)}")
        Text("${state.criteria.horaInicio} – ${state.criteria.horaFin} · ${state.criteria.capacidad} asistentes")
        TextButton(onClick = edit) { Text("Editar búsqueda") }
        RemoteContent(state.rooms, retry) { rooms ->
            Text(if (rooms.size == 1) "1 sala encontrada" else "${rooms.size} salas encontradas", fontWeight = FontWeight.SemiBold)
            if (rooms.isEmpty()) EmptyNotice("No encontramos salas disponibles", "Prueba con otro horario, sede o equipamiento.")
            rooms.forEach { room -> Panel {
                RoomMetadata(room, image = true)
                PrimaryAction("Seleccionar sala →") { select(room.salaId) }
            } }
        }
    }
}
