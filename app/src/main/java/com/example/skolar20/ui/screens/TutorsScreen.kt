package com.example.skolar20.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skolar20.data.remote.FirestoreService
import com.example.skolar20.data.model.Tutor

@Composable
fun TutorsScreen(navController: NavController) {
    var tutors by remember { mutableStateOf<List<Tutor>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            tutors = FirestoreService.fetchTutors()
            error = null
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tutors", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        when {
            loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tutors) { tutor ->
                    TutorCard(tutor) {
                        navController.navigate("booking_new?tutorId=${tutor.tutorId}")
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorCard(tutor: Tutor, onRequest: () -> Unit) {
    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(tutor.name, style = MaterialTheme.typography.titleMedium)
            Text("Expertise: ${tutor.expertise.joinToString()}")
            Text("Qualifications: ${tutor.qualifications}")
            Text("Rate: R${tutor.rate}/hr")
            Text("Location: ${tutor.location}")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRequest) { Text("Request Session") }
                OutlinedButton(onClick = { /* TODO: view profile */ }) { Text("View Profile") }
            }
        }
    }
}