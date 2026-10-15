package com.jnarvaez.aplicacionupc3042.presentation.screens.buscar

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(state: RoomLinkState, update: (SearchCriteria) -> Unit, catalog: () -> Unit, search: () -> Unit) {
    LaunchedEffect(Unit) { if (state.catalog.value == null) catalog() }
    val c = state.criteria
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    fun chooseDate() {
        val current = runCatching { LocalDate.parse(c.fecha) }.getOrDefault(BookingRules.now().toLocalDate())
        val dialog = DatePickerDialog(context, { _, year, month, day ->
            update(c.copy(fecha = LocalDate.of(year, month + 1, day).toString()))
        }, current.year, current.monthValue - 1, current.dayOfMonth)
        dialog.datePicker.minDate = BookingRules.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dialog.show()
    }
    fun chooseTime(start: Boolean) {
        val value = if (start) c.horaInicio else c.horaFin
        val current = runCatching { LocalTime.parse(value) }.getOrDefault(LocalTime.of(9, 0))
        TimePickerDialog(context, { _, hour, minute ->
            val selected = "%02d:%02d".format(hour, minute)
            update(if (start) c.copy(horaInicio = selected) else c.copy(horaFin = selected))
        }, current.hour, current.minute, true).show()
    }
    ScreenColumn {
        Heading("Buscar sala disponible", "Completa los criterios de tu reunión para encontrar el espacio ideal.")
        RemoteContent(state.catalog, catalog) { rooms ->
            if (rooms.isEmpty()) EmptyNotice("No hay salas registradas", "Vuelve a consultar cuando se hayan cargado salas en el servicio.")
            else {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                            OutlinedTextField(c.sede, {}, readOnly = true, label = { Text("Sede de trabajo *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                            ExposedDropdownMenu(expanded, { expanded = false }) {
                                rooms.map { it.sede }.distinct().sorted().forEach { sede ->
                                    DropdownMenuItem(text = { Text(sede) }, onClick = { update(c.copy(sede = sede)); expanded = false })
                                }
                            }
                        }
                        OutlinedButton(onClick = { chooseDate() }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Icon(Icons.Outlined.CalendarMonth, null); Spacer(Modifier.width(8.dp))
                            Text("Fecha: ${BookingRules.dateLabel(c.fecha)}")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { chooseTime(true) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Text("Inicio: ${c.horaInicio.ifBlank { "Elegir" }}")
                            }
                            OutlinedButton(onClick = { chooseTime(false) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Text("Fin: ${c.horaFin.ifBlank { "Elegir" }}")
                            }
                        }
                        OutlinedTextField(c.capacidad, { value -> if (value.all(Char::isDigit)) update(c.copy(capacidad = value)) },
                            label = { Text("Número de asistentes *") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        Text("Equipamiento requerido", fontWeight = FontWeight.SemiBold)
                        val equipment = rooms.flatMap { it.equipamiento }.distinct().sorted()
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            equipment.forEach { item ->
                                FilterChip(selected = item in c.equipamiento, onClick = {
                                    update(c.copy(equipamiento = if (item in c.equipamiento) c.equipamiento - item else c.equipamiento + item))
                                }, label = { Text(item) }, colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                            }
                        }
                        if (equipment.isEmpty()) Text("Sin equipamiento registrado")
                    }
                }
                state.searchError?.let { ErrorNotice(it) }
                PrimaryAction("Buscar salas", enabled = !state.rooms.loading, onClick = search)
                Text("Horarios en Perú", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
