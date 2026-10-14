package com.jnarvaez.aplicacionupc3042.presentation.screens

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
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}

@Composable
fun LoginScreen(state: RoomLinkState, login: (String, String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    // Password is only transient input: never saved to bundles, preferences or ViewModel.
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    ScreenColumn {
        Spacer(Modifier.height(32.dp))
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Domain, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RoomLink", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Gestión corporativa de salas", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Correo corporativo") },
            leadingIcon = { Icon(Icons.Outlined.Mail, null) }, singleLine = true,
            enabled = !state.loginLoading, shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) }, singleLine = true,
            enabled = !state.loginLoading, shape = RoundedCornerShape(12.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = { IconButton(onClick = { visible = !visible }) {
                Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    if (visible) "Ocultar contraseña" else "Mostrar contraseña")
            } }, modifier = Modifier.fillMaxWidth())
        state.loginError?.let { ErrorNotice(it) }
        PrimaryAction("Iniciar sesión →", loading = state.loginLoading) { login(email, password) }
        Text("Acceso para colaboradores autorizados", Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

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
