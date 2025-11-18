package com.example.skolar20.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.skolar20.data.remote.FirestoreService
import com.example.skolar20.data.model.Booking
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingsScreen(navController: NavController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user?.uid) {
        if (user == null) {
            error = "You must be signed in."
            loading = false
            return@LaunchedEffect
        }
        try {
            val token = user.getIdToken(false).await()?.token
            bookings = FirestoreService.fetchBookingsForUser(user.uid, token)
            error = null
        } catch (e: Exception) {
            error = e.message ?: "Failed to load bookings"
        } finally {
            loading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("booking_new") }) {
                Icon(Icons.Default.Add, contentDescription = "New booking")
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("My Bookings", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            when {
                loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                bookings.isEmpty() -> Text("No bookings yet.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(bookings) { BookingCard(it) }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BookingCard(b: Booking) {
    val dtLocal = remember(b.bookingTime) {
        b.bookingTime.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(b.subject, style = MaterialTheme.typography.titleMedium)
            Text("Tutor: ${b.tutorName ?: b.tutorId}")
            Text("When: $dtLocal")
            AssistChip(
                onClick = {},
                label = { Text(b.status.replaceFirstChar { it.uppercase() }) }
            )
            if (!b.notes.isNullOrBlank()) Text("Notes: ${b.notes}")
        }
    }
}