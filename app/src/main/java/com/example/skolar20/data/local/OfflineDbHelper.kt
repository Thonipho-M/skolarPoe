package com.example.skolar20.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import com.example.skolar20.data.model.BookingCreate
import java.time.Instant
import android.util.Log
import androidx.annotation.RequiresApi

private const val TAG = "OfflineDbHelper"

data class PendingBooking(
    val id: Long,
    val tutorId: String,
    val tutorName: String?,
    val userId: String,
    val subject: String,
    val bookingTime: Instant,
    val notes: String?
)

class OfflineDbHelper(context: Context) :
    SQLiteOpenHelper(context, "skolar_offline.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_bookings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tutor_id TEXT NOT NULL,
                tutor_name TEXT,
                user_id TEXT NOT NULL,
                subject TEXT NOT NULL,
                booking_time_epoch INTEGER NOT NULL,
                notes TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pending_bookings")
        onCreate(db)
    }

    /**
     * Insert a pending booking. Returns the inserted row id (>0) or -1 on failure.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertPendingBooking(b: BookingCreate): Long {
        return try {
            val values = ContentValues().apply {
                put("tutor_id", b.tutorId)
                put("tutor_name", b.tutorName)
                put("user_id", b.userId)
                put("subject", b.subject)
                put("booking_time_epoch", b.bookingTime.epochSecond)
                put("notes", b.notes)
            }
            val id = writableDatabase.insert("pending_bookings", null, values)
            Log.d(TAG, "Inserted pending booking id=$id for tutor=${b.tutorId}")
            id
        } catch (ex: Exception) {
            Log.e(TAG, "insertPendingBooking failed", ex)
            -1L
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPendingBookings(): List<PendingBooking> {
        val list = mutableListOf<PendingBooking>()
        val cursor = readableDatabase.query(
            "pending_bookings",
            arrayOf("id", "tutor_id", "tutor_name", "user_id", "subject", "booking_time_epoch", "notes"),
            null, null, null, null, "id ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val tutorId = it.getString(1)
                val tutorName = it.getString(2)
                val userId = it.getString(3)
                val subject = it.getString(4)
                val epoch = it.getLong(5)
                val notes = it.getString(6)

                list.add(
                    PendingBooking(
                        id = id,
                        tutorId = tutorId,
                        tutorName = tutorName,
                        userId = userId,
                        subject = subject,
                        bookingTime = Instant.ofEpochSecond(epoch),
                        notes = notes
                    )
                )
            }
        }
        Log.d(TAG, "getPendingBookings found ${list.size} items")
        return list
    }

    fun deletePendingBooking(id: Long) {
        try {
            writableDatabase.delete("pending_bookings", "id = ?", arrayOf(id.toString()))
            Log.d(TAG, "Deleted pending booking id=$id")
        } catch (ex: Exception) {
            Log.e(TAG, "deletePendingBooking failed for id=$id", ex)
        }
    }

    fun pendingCount(): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM pending_bookings", null)
        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }
        return 0
    }

    /** Debug helper: returns plain rows for quick inspection */
    @RequiresApi(Build.VERSION_CODES.O)
    fun debugListAsStrings(): List<String> {
        return getPendingBookings().map {
            "id=${it.id}, tutor=${it.tutorId}, user=${it.userId}, subj=${it.subject}, epoch=${it.bookingTime.epochSecond}"
        }
    }
}
