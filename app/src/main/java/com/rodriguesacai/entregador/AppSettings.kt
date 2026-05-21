package com.rodriguesacai.entregador

import android.content.Context

object AppSettings {
    private const val PREFS = "rodrigues_entregador_settings"
    private const val KEY_NAV_APP = "navigation_app"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_HIDE_VALUES = "hide_values"

    const val NAV_AUTO = "auto"
    const val NAV_GOOGLE = "google_maps"
    const val NAV_WAZE = "waze"

    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"

    fun getNavigationApp(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NAV_APP, NAV_AUTO)
            ?: NAV_AUTO
    }

    fun setNavigationApp(context: Context, value: String) {
        val safe = when (value) {
            NAV_GOOGLE, NAV_WAZE, NAV_AUTO -> value
            else -> NAV_AUTO
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAV_APP, safe)
            .apply()
    }

    fun navigationLabel(value: String): String = when (value) {
        NAV_GOOGLE -> "Google Maps"
        NAV_WAZE -> "Waze"
        else -> "Padrão do celular"
    }

    fun getThemeMode(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_DARK)
            ?: THEME_DARK
    }

    fun setThemeMode(context: Context, value: String) {
        val safe = when (value) {
            THEME_LIGHT, THEME_DARK -> value
            else -> THEME_DARK
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, safe)
            .apply()
    }

    fun isDarkTheme(context: Context): Boolean = getThemeMode(context) != THEME_LIGHT

    fun themeLabel(value: String): String = when (value) {
        THEME_LIGHT -> "Claro"
        else -> "Escuro"
    }

    fun getHideValues(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIDE_VALUES, false)
    }

    fun setHideValues(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HIDE_VALUES, value)
            .apply()
    }
}
