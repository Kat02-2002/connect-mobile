package com.jnarvaez.aplicacionupc3042.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.jnarvaez.aplicacionupc3042.data.model.*
import com.jnarvaez.aplicacionupc3042.presentation.LoadState

@Composable
fun Heading(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun PrimaryAction(text: String, enabled: Boolean = true, loading: Boolean = false, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
        }
        Text(text)
    }
}

@Composable
fun Panel(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun ErrorNotice(message: String, retry: (() -> Unit)? = null) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
            if (retry != null) TextButton(onClick = retry) { Text("Reintentar") }
        }
    }
}

@Composable
fun <T> RemoteContent(state: LoadState<T>, retry: () -> Unit, content: @Composable (T) -> Unit) {
    when {
        state.loading -> Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> ErrorNotice(state.error, retry)
        state.value != null -> content(state.value)
    }
}

@Composable
fun EmptyNotice(title: String, text: String) {
    Panel {
        Icon(Icons.Outlined.MeetingRoom, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RoomIllustration() {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.MeetingRoom, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text("Ilustración de sala", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RoomImage(room: Sala) {
    val url = room.imagenUrl?.takeIf {
        runCatching { val uri = java.net.URI(it); uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank() }.getOrDefault(false)
    }
    Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp))) {
        if (url == null) RoomIllustration()
        else SubcomposeAsyncImage(model = url, contentDescription = "Imagen de ${room.nombre}",
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            loading = { RoomIllustration() }, error = { RoomIllustration() })
    }
}

@Composable
fun RoomMetadata(room: Sala, image: Boolean = false) {
    if (image) RoomImage(room)
    Text(room.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    Text("${room.sede} · Piso ${room.piso}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text("Capacidad: ${room.capacidad} personas")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        room.equipamiento.forEach { MetaBadge(it) }
    }
    if (room.equipamiento.isEmpty()) Text("Sin equipamiento informado", style = MaterialTheme.typography.bodySmall)
}

@Composable
fun BookingSummary(r: Reserva, now: java.time.LocalDateTime, onDetail: () -> Unit) {
    Panel {
        StatusBadge(BookingRules.status(r, now))
        Text(r.sala?.nombre ?: "Sala ${r.salaId}", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        r.sala?.let { Text("${it.sede} · Piso ${it.piso}") }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(BookingRules.dateLabel(r.fecha))
                Text("${r.horaInicio} – ${r.horaFin}", fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        TextButton(onClick = onDetail, modifier = Modifier.align(Alignment.End)) { Text("Ver detalle →") }
    }
}

@Composable
fun StatusBadge(status: String) {
    val confirmed = status == "CONFIRMADA"
    Surface(shape = RoundedCornerShape(24.dp),
        color = if (confirmed) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant) {
        Text(if (confirmed) "● Confirmada" else "Finalizada", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (confirmed) Color(0xFF006E2D) else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium)
    }
}
