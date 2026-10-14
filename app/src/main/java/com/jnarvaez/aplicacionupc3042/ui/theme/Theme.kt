package com.jnarvaez.aplicacionupc3042.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val RoomLinkColors = lightColorScheme(
    primary = Color(0xFF0A66C2), onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EEFD), onPrimaryContainer = Color(0xFF082B57),
    secondary = Color(0xFF006E2D), secondaryContainer = Color(0xFFDCFCE7),
    background = Color(0xFFF9F9FF), surface = Color.White,
    surfaceVariant = Color(0xFFF0F3FF), onSurface = Color(0xFF111C2D),
    onSurfaceVariant = Color(0xFF414752), outlineVariant = Color(0xFFE2E8F0)
)
@Composable
fun AplicacionUPC3042Theme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RoomLinkColors, typography = Typography, content = content)
}
