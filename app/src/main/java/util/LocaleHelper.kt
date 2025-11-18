package com.example.skolar20.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun applySavedLocale(context: Context): Context {
        val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
        return setLocale(context, language)
    }

    fun setLocale(context: Context, languageCode: String): Context {
        persist(context, languageCode)

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    private fun persist(context: Context, languageCode: String) {
        val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }
}