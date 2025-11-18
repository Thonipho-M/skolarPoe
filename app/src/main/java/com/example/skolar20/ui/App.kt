package com.example.skolar20.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.skolar20.navigation.NavDestination
import com.example.skolar20.navigation.SkolarNavGraph
import com.example.skolar20.ui.screens.LoginScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    // Listen for auth changes
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user == null) {
        LoginScreen() // startup screen until signed in
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: NavDestination.Home.route

    // Compute a human-friendly title for the top bar
    val currentTitle = when {
        currentRoute == NavDestination.Home.route -> "Home"
        currentRoute == NavDestination.Tutors.route -> "Tutors"
        currentRoute == NavDestination.Bookings.route -> "My Bookings"
        currentRoute == NavDestination.Chatbot.route -> "Chatbot"
        currentRoute == NavDestination.Settings.route -> "Settings"
        // If you navigate to a create-booking route like "booking_new?...":
        currentRoute.startsWith("booking_new") -> "New Booking"
        else -> "Skolar"
    }

    val items = listOf(
        NavDestination.Home,
        NavDestination.Tutors,
        NavDestination.Bookings,
        NavDestination.Chatbot,
        NavDestination.Settings
    )

    Scaffold(
        topBar = { SkolarTopBar(title = currentTitle) },
        bottomBar = {
            NavigationBar {
                items.forEach { dest ->
                    val selected = currentRoute == dest.route
                    val icon = when (dest) {
                        NavDestination.Home -> Icons.Default.Home
                        NavDestination.Tutors -> Icons.Default.AccountCircle
                        NavDestination.Bookings -> Icons.Default.Add
                        NavDestination.Chatbot -> Icons.Default.Email
                        NavDestination.Settings -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = { Icon(icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { inner ->
        Surface(Modifier.padding(inner)) {
            SkolarNavGraph(navController)
        }
    }
}
