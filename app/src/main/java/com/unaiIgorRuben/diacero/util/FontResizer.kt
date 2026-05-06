package com.unaiIgorRuben.diacero.util

import android.content.Context
import android.content.res.Configuration

object FontResizer {
    private const val PREFS_NAME = "ajustes_prefs"
    private const val KEY_FONT_SIZE = "font_size"

    fun wrap(context: Context): Context {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tamano = prefs.getString(KEY_FONT_SIZE, "Normal") ?: "Normal"
        
        // Valores más agresivos para que el cambio sea muy evidente
        val scale = when (tamano) {
            "Pequeño" -> 0.7f
            "Grande" -> 1.3f
            else -> 1.0f
        }

        val config = Configuration(context.resources.configuration)
        config.fontScale = scale
        return context.createConfigurationContext(config)
    }

    fun getSavedSize(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FONT_SIZE, "Normal") ?: "Normal"
    }
}
