package com.example.skolar20.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.skolar20.navigation.NavDestination

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Skolar", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Tutor app prototype – quick nav")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { navController.navigate(NavDestination.Tutors.route) }, modifier = Modifier.weight(1f)) { Text("Find Tutors") }
            Button(onClick = { navController.navigate(NavDestination.Bookings.route) }, modifier = Modifier.weight(1f)) { Text("My Bookings") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { navController.navigate(NavDestination.Chatbot.route) }, modifier = Modifier.weight(1f)) { Text("Chatbot") }
            Button(onClick = { navController.navigate(NavDestination.Settings.route) }, modifier = Modifier.weight(1f)) { Text("Settings") }
        }
    }
}
