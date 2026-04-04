package com.prantiux.milktick.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class ThemeMode {
    AUTO, LIGHT, DARK
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        
        // Default values
        private const val DEFAULT_THEME_MODE = "AUTO"
        private const val DEFAULT_ACCENT_COLOR = 0xFF146EBE // Azure default accent
    }
    
    fun getThemeMode(): ThemeMode {
        val modeString = prefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE
        return try {
            ThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.AUTO
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
    
    fun getAccentColor(): Color {
        val colorInt = prefs.getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR.toInt())
        return Color(colorInt)
    }
    
    fun setAccentColor(color: Color) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color.toArgb()).apply()
    }
    
    // Available accent colors
    fun getAvailableColors(): List<Pair<String, Color>> {
        return listOf(
            "Azure" to Color(0xFF146EBE),
            "Teal" to Color(0xFF26A69A),
            "Blue" to Color(0xFF42A5F5),
            "Green" to Color(0xFF66BB6A),
            "Purple" to Color(0xFFAB47BC),
            "Orange" to Color(0xFFFF7043),
            "Pink" to Color(0xFFEC407A)
        )
    }
}
