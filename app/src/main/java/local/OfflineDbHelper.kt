package com.example.skolar20.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.skolar20.data.model.BookingCreate
import java.time.Instant

class OfflineDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "skolar_offline.db"
        private const val DB_VERSION = 1
        private const val TABLE_PENDING_BOOKINGS = "pending_bookings"

        // Columns
        private const val COL_ID = "id"
        private const val COL_TUTOR_ID = "tutor_id"
        private const val COL_TUTOR_NAME = "tutor_name"
        private const val COL_USER_ID = "user_id"
        private const val COL_SUBJECT = "subject"
        private const val COL_BOOKING_TIME = "booking_time"
        private const val COL_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PENDING_BOOKINGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TUTOR_ID TEXT NOT NULL,
                $COL_TUTOR_NAME TEXT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_SUBJECT TEXT NOT NULL,
                $COL_BOOKING_TIME TEXT NOT NULL,
                $COL_NOTES TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PENDING_BOOKINGS")
        onCreate(db)
    }

    fun insertPendingBooking(booking: BookingCreate): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TUTOR_ID, booking.tutorId)
            put(COL_TUTOR_NAME, booking.tutorName)
            put(COL_USER_ID, booking.userId)
            put(COL_SUBJECT, booking.subject)
            put(COL_BOOKING_TIME, booking.bookingTime.toString())
            put(COL_NOTES, booking.notes)
        }
        val id = db.insert(TABLE_PENDING_BOOKINGS, null, values)
        db.close()
        return id
    }

    fun getAllPendingBookings(): List<PendingBookingRow> {
        val bookings = mutableListOf<PendingBookingRow>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PENDING_BOOKINGS,
            null,
            null,
            null,
            null,
            null,
            "$COL_ID ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COL_ID))
                val tutorId = getString(getColumnIndexOrThrow(COL_TUTOR_ID))
                val tutorName = getString(getColumnIndexOrThrow(COL_TUTOR_NAME))
                val userId = getString(getColumnIndexOrThrow(COL_USER_ID))
                val subject = getString(getColumnIndexOrThrow(COL_SUBJECT))
                val bookingTimeStr = getString(getColumnIndexOrThrow(COL_BOOKING_TIME))
                val notes = getString(getColumnIndexOrThrow(COL_NOTES))

                bookings.add(
                    PendingBookingRow(
                        id = id,
                        tutorId = tutorId,
                        tutorName = tutorName,
                        userId = userId,
                        subject = subject,
                        bookingTime = Instant.parse(bookingTimeStr),
                        notes = notes
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return bookings
    }

    fun deletePendingBooking(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_PENDING_BOOKINGS, "$COL_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getPendingCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PENDING_BOOKINGS", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }
}

data class PendingBookingRow(
    val id: Long,
    val tutorId: String,
    val tutorName: String?,
    val userId: String,
    val subject: String,
    val bookingTime: Instant,
    val notes: String?
)