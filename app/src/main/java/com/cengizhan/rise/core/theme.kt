package com.cengizhan.rise.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class RiseThemeColors(
    val background: Color,
    val card: Color,
    val cardAlt: Color,
    val completedHabitCard: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val primaryButton: Color,
    val primaryButtonText: Color,
    val outline: Color,
    val indicator: Color,
    val navIndicator: Color
)

object RiseThemeSettings {
    const val PREFS_NAME = "rise_profile_settings"
    const val KEY_GENERAL_NOTIFICATIONS = "generalNotificationsEnabled"
    const val KEY_GENERAL_NOTIFICATION_START_TIME = "generalNotificationStartTime"
    const val KEY_GENERAL_NOTIFICATION_END_TIME = "generalNotificationEndTime"
    const val KEY_SMART_COACH_NOTIFICATIONS = "smartCoachNotificationsEnabled"
    const val KEY_SMART_COACH_NOTIFICATION_START_TIME = "smartCoachNotificationStartTime"
    const val KEY_SMART_COACH_NOTIFICATION_END_TIME = "smartCoachNotificationEndTime"
    const val KEY_SELECTED_THEME_MODE = "selectedThemeMode"

    const val THEME_DARK_PREMIUM = "dark_premium"
    const val THEME_LIGHT = "light"

    var selectedThemeMode by mutableStateOf(THEME_DARK_PREMIUM)

    fun loadThemeMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_THEME_MODE, THEME_DARK_PREMIUM) ?: THEME_DARK_PREMIUM
    }

    fun saveThemeMode(context: Context, themeMode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_THEME_MODE, themeMode)
            .apply()
        selectedThemeMode = themeMode
    }

    fun colorsFor(themeMode: String): RiseThemeColors {
        return if (themeMode == THEME_LIGHT) {
            RiseThemeColors(
                background = Color(0xFFF8FAFC),
                card = Color(0xFFF1F5F9),
                cardAlt = Color(0xFFE5E7EB),
                completedHabitCard = Color(0xFFDCFCE7),
                primaryText = Color(0xFF0F172A),
                secondaryText = Color(0xFF475569),
                mutedText = Color(0xFF64748B),
                primaryButton = Color(0xFF0F172A),
                primaryButtonText = Color.White,
                outline = Color(0xFFCBD5E1),
                indicator = Color(0xFFE2E8F0),
                navIndicator = Color(0xFFE2E8F0)
            )
        } else {
            RiseThemeColors(
                background = Color(0xFF070B14),
                card = Color(0xFF0B1120),
                cardAlt = Color(0xFF111827),
                completedHabitCard = Color(0xFF102018),
                primaryText = Color.White,
                secondaryText = Color(0xFF9CA3AF),
                mutedText = Color(0xFF6B7280),
                primaryButton = Color.White,
                primaryButtonText = Color.Black,
                outline = Color(0xFF1F2937),
                indicator = Color(0xFF1F2937),
                navIndicator = Color(0xFF1F2937)
            )
        }
    }
}

val LocalRiseColors = staticCompositionLocalOf {
    RiseThemeSettings.colorsFor(RiseThemeSettings.THEME_DARK_PREMIUM)
}
