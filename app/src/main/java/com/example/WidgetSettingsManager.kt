package com.example

import android.content.Context
import android.content.SharedPreferences

class WidgetSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

    var showLunarDate: Boolean
        get() = prefs.getBoolean("show_lunar", true)
        set(value) = prefs.edit().putBoolean("show_lunar", value).apply()

    var useDynamicColor: Boolean
        get() = prefs.getBoolean("use_dynamic_color", true)
        set(value) = prefs.edit().putBoolean("use_dynamic_color", value).apply()

    var startOnMonday: Boolean
        get() = prefs.getBoolean("start_on_monday", true)
        set(value) = prefs.edit().putBoolean("start_on_monday", value).apply()

    var widgetThemeColor: Int
        get() = prefs.getInt("theme_color", 0xFF8C1D18.toInt())
        set(value) = prefs.edit().putInt("theme_color", value).apply()
}
