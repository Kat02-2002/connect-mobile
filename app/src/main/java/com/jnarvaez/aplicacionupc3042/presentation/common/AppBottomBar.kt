package com.jnarvaez.aplicacionupc3042.presentation.common

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jnarvaez.aplicacionupc3042.presentation.navigation.BottomNavItem

@Composable
fun AppBottomBar(navHostController: NavHostController){
    val opciones = listOf(BottomNavItem.Inicio, BottomNavItem.Form, BottomNavItem.Buscar)

    NavigationBar{
        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        val rutaActual = navBackStackEntry?.destination?.route

        opciones.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icono, "") },
                label = { Text(item.titulo) },
                selected = rutaActual?.startsWith(item.ruta.split("?")[0]) == true,
                onClick = {
                    navHostController.navigate(item.ruta){
                        popUpTo(navHostController.graph.findStartDestination().id){
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}