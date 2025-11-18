package com.example.skolar20.navigation

enum class NavDestination(val route: String, val label: String) {
    Home(route = "home", label = "Home"),
    Tutors(route = "tutors", label = "Tutors"),
    Bookings(route = "bookings", label = "Bookings"),
    Chatbot(route = "chatbot", label = "Chatbot"),
    Settings(route = "settings", label = "Settings");

    companion object {
        // Extra route (not in bottom bar)
        const val ROUTE_NEW_BOOKING = "booking_new"
    }
}