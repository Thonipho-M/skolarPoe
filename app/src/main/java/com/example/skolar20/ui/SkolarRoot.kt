package com.example.skolar20.ui


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skolar20.navigation.*


@Composable
fun SkolarRoot() {
    val navController = rememberNavController()
    val items = listOf(
        NavDestination.Home,
        NavDestination.Tutors,     // ✅ active feature
        NavDestination.Bookings,   // 🚧 placeholder
        NavDestination.Chatbot,   // 🚧 placeholder
        NavDestination.Settings    // ✅ simple settings UI
    )
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: NavDestination.Home.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { dest ->
                    val selected = current == dest.route
                    val icon = when (dest) {
                        NavDestination.Home -> Icons.Default.Home
                        NavDestination.Tutors -> Icons.Default.AccountCircle
                        NavDestination.Bookings -> Icons.Default.Add
                        NavDestination.Chatbot -> Icons.Default.Email
                        NavDestination.Settings -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigate(dest.route) },
                        icon = { Icon(icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding -> Surface(Modifier.padding(padding)) { SkolarNavGraph(navController) } }
}
