package com.jnarvaez.aplicacionupc3042.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jnarvaez.aplicacionupc3042.presentation.navigation.RutasNav

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navHostController: NavHostController){
    val navBackStackEntry = navHostController.currentBackStackEntryAsState()
    val ruta = navBackStackEntry.value?.destination?.route
    val titulo = RutasNav.getTitulo(ruta)
    val mostrarBotonBack = ruta == RutasNav.DETALLE


    Column{
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(mostrarBotonBack){
                IconButton(onClick = { navHostController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        tint = Color(0xFF0F0069),
                        modifier = Modifier.size(28.dp))
                }
            }else{
                Icon(
                    imageVector =  Icons.Default.Book,
                    contentDescription = "Logo",
                    tint = Color(0xFF0F0069),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = titulo,
                color = Color(0xFF0F0069),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif // Cambia según la fuente serif que uses
            )

            // Espaciador para empujar los botones de acción a la derecha
            Spacer(modifier = Modifier.weight(1f))

            // Botón de búsqueda
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color(0xFF0F0069)
                )
            }

            // Botón de perfil
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = Color(0xFF0F0069)
                )
            }
        }

        HorizontalDivider(thickness = 2.dp, color = Color(0xFFC8C4D5))
    }
}