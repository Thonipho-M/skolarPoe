package com.example.skolar20.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.model.Tutor
import com.example.skolar20.data.remote.FirestoreService
import com.example.skolar20.navigation.NavDestination
import com.example.skolar20.ui.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewBookingScreen(
    navController: NavController,
    preselectedTutorId: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    var tutors by remember { mutableStateOf<List<Tutor>>(emptyList()) }
    var loadingTutors by remember { mutableStateOf(true) }
    var tutorError by remember { mutableStateOf<String?>(null) }

    var selectedTutorId by remember { mutableStateOf(preselectedTutorId) }
    var subject by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    var formBusy by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load tutors from backend
    LaunchedEffect(Unit) {
        try {
            tutors = FirestoreService.fetchTutors()
            if (preselectedTutorId != null && tutors.none { it.tutorId == preselectedTutorId }) {
                selectedTutorId = null
            }
        } catch (e: Exception) {
            tutorError = e.localizedMessage ?: "Failed to load tutors"
        } finally {
            loadingTutors = false
        }
    }

    fun tutorNameFor(id: String?): String? =
        tutors.firstOrNull { it.tutorId == id }?.name

    val dateLabel = dateTime?.toLocalDate().toString()
    val timeLabel = dateTime?.toLocalTime()?.withSecond(0)?.withNano(0).toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New booking", style = MaterialTheme.typography.titleLarge)

        if (loadingTutors) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (tutorError != null) {
            Text("Error loading tutors: $tutorError", color = MaterialTheme.colorScheme.error)
        }

        // Tutor dropdown
        var tutorExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = tutorExpanded,
            onExpandedChange = { tutorExpanded = !tutorExpanded }
        ) {
            TextField(
                value = tutorNameFor(selectedTutorId) ?: "Select tutor",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Tutor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tutorExpanded) }
            )

            ExposedDropdownMenu(
                expanded = tutorExpanded,
                onDismissRequest = { tutorExpanded = false }
            ) {
                tutors.forEach { tutor ->
                    DropdownMenuItem(
                        text = { Text(tutor.name) },
                        onClick = {
                            selectedTutorId = tutor.tutorId
                            tutorExpanded = false
                        }
                    )
                }
            }
        }

        // Subject
        TextField(
            value = subject,
            onValueChange = { subject = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Subject") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Date
        Button(
            onClick = {
                val cal = Calendar.getInstance()
                val dlg = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val current = dateTime ?: LocalDateTime.now()
                        dateTime = LocalDateTime.of(
                            year, month + 1, day,
                            current.hour, current.minute
                        )
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                dlg.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (dateTime == null) "Pick date" else "Date: $dateLabel")
        }

        // Time
        Button(
            onClick = {
                val now = LocalDateTime.now()
                val dlg = TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val current = dateTime ?: LocalDateTime.now()
                        dateTime = LocalDateTime.of(
                            current.year, current.month, current.dayOfMonth,
                            hour, minute
                        )
                    },
                    now.hour,
                    now.minute,
                    true
                )
                dlg.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (dateTime == null) "Pick time" else "Time: $timeLabel")
        }

        // Notes
        TextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        if (info != null) {
            Text(info!!, color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = {
                val user = auth.currentUser
                if (user == null) {
                    error = "You must be signed in"
                    return@Button
                }

                if (selectedTutorId == null || dateTime == null || subject.isBlank()) {
                    error = "Please select tutor, date/time and subject"
                    return@Button
                }

                error = null
                info = null
                formBusy = true

                scope.launch {
                    val instantUtc = dateTime!!.atZone(ZoneId.systemDefault()).toInstant()
                    val bookingCreate = BookingCreate(
                        tutorId = selectedTutorId!!,
                        tutorName = tutorNameFor(selectedTutorId),
                        userId = user.uid,
                        subject = subject.trim(),
                        bookingTime = instantUtc,
                        notes = notes.ifBlank { null }
                    )

                    try {
                        val token = user.getIdToken(false).await()?.token
                            ?: throw Exception("Could not get auth token")

                        val createdId = FirestoreService.createBooking(
                            bookingCreate,
                            idToken = token
                        )
                        info = "Booking created (#$createdId)"

                        // Send notification
                        NotificationHelper.sendBookingNotification(
                            context = context,
                            title = "Booking Confirmed! 🎓",
                            message = "Your booking for $subject with ${tutorNameFor(selectedTutorId)} has been confirmed"
                        )

                        navController.popBackStack(NavDestination.Bookings.route, false)
                    } catch (e: Exception) {
                        // Offline mode - just show message, no offline DB for now
                        info = "Could not create booking. Please check your internet connection."
                        error = e.localizedMessage ?: "Failed to create booking"
                    } finally {
                        formBusy = false
                    }
                }
            },
            enabled = !formBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (formBusy) "Submitting..." else "Submit booking")
        }
    }
}