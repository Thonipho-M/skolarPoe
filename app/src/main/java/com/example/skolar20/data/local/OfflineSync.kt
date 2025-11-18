// app/src/main/java/com/example/skolar20/data/local/OfflineSync.kt
package com.example.skolar20.data.local

import android.content.Context
import android.util.Log
import com.example.skolar20.data.model.BookingCreate
import com.example.skolar20.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "OfflineSync"

suspend fun syncPendingBookings(context: Context, auth: FirebaseAuth): Int =
    withContext(Dispatchers.IO) {
        val db = OfflineDbHelper(context)
        val pending = db.getPendingBookings()
        Log.d(TAG, "Attempting to sync ${pending.size} pending bookings")

        if (pending.isEmpty()) {
            Log.d(TAG, "No pending bookings to sync")
            return@withContext 0
        }

        // Ensure we have a signed-in user
        val user = auth.currentUser
        if (user == null) {
            Log.w(TAG, "User not signed in - cannot sync pending bookings")
            return@withContext 0
        }

        // Obtain ID token once (refresh if needed)
        val idToken: String? = try {
            user.getIdToken(false).await()?.token
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to obtain ID token for sync", ex)
            null
        }

        if (idToken == null) {
            Log.w(TAG, "ID token is null - aborting sync")
            return@withContext 0
        }

        var successCount = 0

        for (p in pending) {
            try {
                val booking = BookingCreate(
                    tutorId = p.tutorId,
                    tutorName = p.tutorName,
                    userId = p.userId,
                    subject = p.subject,
                    bookingTime = p.bookingTime,
                    notes = p.notes
                )

                // Call createBooking with idToken (required by your FirestoreService)
                val remoteId = FirestoreService.createBooking(booking, idToken)

                Log.d(TAG, "Successfully synced local id=${p.id} -> remoteId=$remoteId")
                db.deletePendingBooking(p.id)
                successCount++
            } catch (ex: Exception) {
                // Log and continue with other pending rows
                Log.e(TAG, "Failed to sync pending id=${p.id}: ${ex.message}", ex)
            }
        }

        Log.d(TAG, "Sync complete: success=$successCount out of ${pending.size}")
        successCount
    }
