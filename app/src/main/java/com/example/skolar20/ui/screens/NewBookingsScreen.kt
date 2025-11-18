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
import com.example.skolar20.data.local.OfflineDbHelper
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.model.Tutor
import com.example.skolar20.data.remote.FirestoreService
import com.example.skolar20.navigation.NavDestination
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    // Dropdown state for tutors
    var tutorsExpanded by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

    val dateLabel = dateTime?.format(dateFormatter) ?: ""
    val timeLabel = dateTime?.format(timeFormatter) ?: ""

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

        // Tutor dropdown (properly expandable)
        Text(text = "Tutor", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = tutorsExpanded,
            onExpandedChange = { tutorsExpanded = !tutorsExpanded }
        ) {
            TextField(
                value = tutorNameFor(selectedTutorId) ?: "Select tutor",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Tutor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tutorsExpanded) }
            )
            ExposedDropdownMenu(
                expanded = tutorsExpanded,
                onDismissRequest = { tutorsExpanded = false }
            ) {
                if (tutors.isEmpty()) {
                    DropdownMenuItem(text = { Text("No tutors available") }, onClick = { tutorsExpanded = false })
                } else {
                    tutors.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name ?: "Unknown") },
                            onClick = {
                                selectedTutorId = t.tutorId
                                tutorsExpanded = false
                            }
                        )
                    }
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
                        // Online attempt: get token and call service (keeps your idToken param)
                        val token = user.getIdToken(false).await()?.token
                            ?: throw Exception("Could not get auth token")

                        val createdId = FirestoreService.createBooking(
                            bookingCreate,
                            idToken = token
                        )
                        info = "Booking created (#$createdId)"
                        // Optional: go back to bookings
                        navController.popBackStack(NavDestination.Bookings.route, false)
                    } catch (e: Exception) {
                        // Online failed -> save offline with feedback
                        try {
                            val db = OfflineDbHelper(context)
                            val rowId = db.insertPendingBooking(bookingCreate)
                            if (rowId > 0) {
                                info = "No internet. Booking saved offline (local id=$rowId) and will sync later."
                            } else {
                                error = "Failed to save booking offline"
                            }
                        } catch (e2: Exception) {
                            error = e2.localizedMessage ?: "Failed to save booking offline"
                        }
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
