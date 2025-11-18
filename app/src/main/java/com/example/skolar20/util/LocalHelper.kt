package com.example.skolar20.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    /**
     * Apply the language to the provided context and return a wrapped context.
     * Use language codes like "en", "zu", "af", "xh" etc.
     */
    fun setLocale(base: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(android.os.LocaleList(locale))
            base.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            base
        }
    }

    /** Convenience: read saved language from Preferences and apply to context */
    fun applySavedLocale(base: Context): Context {
        val lang = Preferences.getSelectedLanguage(base) ?: "en"
        return setLocale(base, lang)
    }
}
