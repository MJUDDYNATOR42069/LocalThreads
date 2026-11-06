package com.example.ecommerceapp.helpers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LocalHelper {
    companion object {
        private const val PREF_NAME = "LanguagePref"
        private const val KEY_LANG = "selected_language"

        fun saveLanguage(context: Context, language: String) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_LANG, language).apply()
        }

        fun getSavedLanguage(context: Context): String {
            val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_LANG, "en") ?: "en"
        }

        fun setLocale(context: Context): Context {
            val language = getSavedLanguage(context)
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
                return context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                return context
            }
        }
    }
}