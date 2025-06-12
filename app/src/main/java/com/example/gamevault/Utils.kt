package com.example.gamevault

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AppCompatDelegate;

fun convertTimestamp(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.format(date)
}

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK = "is_dark_theme"

    fun saveThemePreference(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
    }

    fun loadThemePreference(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_DARK, false) // false = Light by default
    }

    fun applySavedTheme(context: Context) {
        val isDark = loadThemePreference(context)
        val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun validatePassword(password: String): String? {
    return when {
        password.length < 8 -> "Password must be at least 8 characters"
        !password.any { it.isDigit() } -> "Password must contain at least 1 digit"
        !password.any { it.isLetter() } -> "Password must contain at least 1 letter"
        else -> null
    }
}
