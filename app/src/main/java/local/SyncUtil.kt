package com.example.skolar20.data.local

import android.content.Context
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

suspend fun syncPendingBookings(context: Context, auth: FirebaseAuth): Int {
    val db = OfflineDbHelper(context)
    val pending = db.getAllPendingBookings()

    if (pending.isEmpty()) {
        return 0
    }

    val user = auth.currentUser ?: throw Exception("User not signed in")
    val token = user.getIdToken(false).await()?.token ?: throw Exception("Could not get auth token")

    var syncedCount = 0

    for (row in pending) {
        try {
            val bookingCreate = BookingCreate(
                tutorId = row.tutorId,
                tutorName = row.tutorName,
                userId = row.userId,
                subject = row.subject,
                bookingTime = row.bookingTime,
                notes = row.notes
            )

            FirestoreService.createBooking(bookingCreate, token)
            db.deletePendingBooking(row.id)
            syncedCount++
        } catch (e: Exception) {
            // If one fails, continue with others
            continue
        }
    }

    return syncedCount
}