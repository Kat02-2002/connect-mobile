package com.jnarvaez.aplicacionupc3042.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.jnarvaez.aplicacionupc3042.data.model.BookingRules
import com.jnarvaez.aplicacionupc3042.presentation.RoomLinkViewModel
import com.jnarvaez.aplicacionupc3042.presentation.screens.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navHostController: NavHostController = rememberNavController(), vm: RoomLinkViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val entry by navHostController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    var now by remember { mutableStateOf(BookingRules.now()) }
    LaunchedEffect(Unit) { while (true) { now = BookingRules.now(); delay(30_000) } }
    fun root(destination: String) {
        navHostController.navigate(destination) {
            popUpTo("dashboard") { inclusive = destination == "dashboard" }
            launchSingleTop = true
        }
    }
    fun logout() {
        vm.logout()
        navHostController.navigate("login") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
    }
    fun detail(id: String) { navHostController.navigate("reserva/${android.net.Uri.encode(id)}") }
    LaunchedEffect(state.user, route) {
        if (state.user != null && route == "login") {
            navHostController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
        } else if (state.user == null && route != null && route != "login") {
            navHostController.navigate("login") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
        }
    }
    LaunchedEffect(state.createdId) {
        val id = state.createdId ?: return@LaunchedEffect
        root("bookings")
        detail(id)
        vm.acknowledgeCreated()
    }
    val loggedIn = state.user != null && route != "login"
    val mainRoute = route in listOf("dashboard", "search", "bookings")
    BackHandler(enabled = route == "dashboard") { /* Stay in the authenticated home. */ }
    Scaffold(
        topBar = {
            if (loggedIn) TopAppBar(
                title = { Text(when (route) {
                    "dashboard" -> "RoomLink · Inicio"
                    "search" -> "Reservar"
                    "rooms" -> "Resultados"
                    "confirm/{id}" -> "Confirmación"
                    "bookings" -> "Mis reservas"
                    else -> "Reserva"
                }) },
                navigationIcon = {
                    if (!mainRoute) IconButton(onClick = { navHostController.popBackStack() }, enabled = !state.posting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                } else Icon(Icons.Outlined.Domain, null, tint = MaterialTheme.colorScheme.primary)
                },
                actions = { IconButton(onClick = { logout() }, enabled = !state.posting) { Icon(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (loggedIn && mainRoute) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(Triple("dashboard", "Inicio", Icons.Outlined.Home),
                    Triple("search", "Reservar", Icons.Outlined.CalendarMonth),
                    Triple("bookings", "Mis reservas", Icons.AutoMirrored.Outlined.EventNote)).forEach { (r, label, icon) ->
                    NavigationBarItem(selected = route == r, onClick = { root(r) },
                        icon = { Icon(icon, null) }, label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer))
                }
            }
        }
    ) { padding ->
        NavHost(navHostController, startDestination = "login", modifier = Modifier.padding(padding)) {
            composable("login") { LoginScreen(state, vm::login) }
            composable("dashboard") {
                if (state.user != null) DashboardScreen(state, now, vm::bookings, { root("search") }, { root("bookings") }, ::detail)
            }
            composable("search") {
                if (state.user != null) SearchScreen(state, vm::criteria, vm::catalog) {
                    if (vm.search()) navHostController.navigate("rooms") { launchSingleTop = true }
                }
            }
            composable("rooms") {
                if (state.user != null) AvailableRoomsScreen(state, { vm.search() },
                    { navHostController.popBackStack() }, { id ->
                        vm.select(id)
                        navHostController.navigate("confirm/${android.net.Uri.encode(id)}")
                    })
            }
            composable("confirm/{id}") { backEntry ->
                val id = backEntry.arguments?.getString("id") ?: return@composable
                LaunchedEffect(id) { if (state.user != null && state.selectedRoomId != id) vm.select(id) }
                if (state.user != null) ConfirmScreen(state, { vm.select(id) }, vm::confirm,
                    { navHostController.popBackStack() }, { root("search") }, { root("bookings") })
            }
            composable("bookings") {
                if (state.user != null) BookingsScreen(state, now, vm::bookings, { root("search") }, ::detail)
            }
            composable("reserva/{id}") { backEntry ->
                val id = backEntry.arguments?.getString("id") ?: return@composable
                LaunchedEffect(id) { if (state.user != null) vm.detail(id) }
                if (state.user != null) BookingDetailScreen(state, now, { vm.detail(id) })
            }
        }
    }
}
