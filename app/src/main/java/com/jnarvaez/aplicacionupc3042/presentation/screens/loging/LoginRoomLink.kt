package com.jnarvaez.aplicacionupc3042.presentation.screens.loging

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.jnarvaez.aplicacionupc3042.presentation.*
import com.jnarvaez.aplicacionupc3042.presentation.common.*


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
