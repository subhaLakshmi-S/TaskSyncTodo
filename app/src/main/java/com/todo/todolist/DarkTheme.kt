package com.todo.todolist

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object DarkTheme {
    private const val PREF_NAME= "theme_pref"
    private const val  DARK_MODE_KEY = "dark_mode"

    fun applyTheme(context: Context){
        val prefs= context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(DARK_MODE_KEY, false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }
    fun toggleTheme(context: Context){
        val prefs= context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(DARK_MODE_KEY, false)

        prefs.edit().putBoolean(DARK_MODE_KEY, !isDark).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (!isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun isDarkMode(context: Context): Boolean{
        val prefs= context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(DARK_MODE_KEY, false)

        return prefs.getBoolean(isDark.toString(), false)
    }

}