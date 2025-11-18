// app/src/main/java/com/example/skolar20/util/Preferences.kt
package com.example.skolar20.util

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private const val PREFS_NAME = "skolar_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_SELECTED_LANG = "selected_language"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isBiometricEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getSelectedLanguage(context: Context): String? =
        prefs(context).getString(KEY_SELECTED_LANG, "en")

    fun setSelectedLanguage(context: Context, lang: String) {
        prefs(context).edit().putString(KEY_SELECTED_LANG, lang).apply()
    }
}
