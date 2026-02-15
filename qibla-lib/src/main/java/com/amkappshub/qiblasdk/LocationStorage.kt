package com.amkappshub.qiblasdk

import android.content.Context
import android.location.Location

object LocationStorage {
    private const val PREF_NAME = "qibla_prefs"
    private const val KEY_LAT = "last_lat"
    private const val KEY_LNG = "last_lng"
    private const val KEY_CITY = "last_city" // 🆕 New Key

    // Save Location
    fun saveLocation(context: Context, location: Location) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_LAT, location.latitude.toFloat())
            putFloat(KEY_LNG, location.longitude.toFloat())
            apply()
        }
    }

    fun getSavedLocation(context: Context): Location? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LAT)) return null

        return Location("saved_memory").apply {
            latitude = prefs.getFloat(KEY_LAT, 0f).toDouble()
            longitude = prefs.getFloat(KEY_LNG, 0f).toDouble()
        }
    }

    // Save City
    fun saveCity(context: Context,cityName: String?) {
        if (cityName == null) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_CITY, cityName)
            apply()
        }
    }

    // 🆕 Helper to get City
    fun getSavedCity(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CITY, null)
    }
}