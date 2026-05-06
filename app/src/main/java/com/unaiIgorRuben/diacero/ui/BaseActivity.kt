package com.unaiIgorRuben.diacero.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.util.FontResizer
import com.unaiIgorRuben.diacero.util.LocaleHelper

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        // Combinar LocaleHelper y FontResizer
        val localeContext = LocaleHelper.wrap(newBase)
        super.attachBaseContext(FontResizer.wrap(localeContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        aplicarTemaPersonalizado()
        super.onCreate(savedInstanceState)
    }

    private fun aplicarTemaPersonalizado() {
        val prefs = getSharedPreferences("ajustes_prefs", Context.MODE_PRIVATE)
        
        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        val key = if (isNightMode) "color_oscuro" else "color_claro"
        val defaultColor = if (isNightMode) Color.parseColor("#66B5FF") else Color.parseColor("#0F6CBD")
        val colorSeleccionado = prefs.getInt(key, defaultColor)

        val themeRes = when (String.format("#%06X", 0xFFFFFF and colorSeleccionado).uppercase()) {
            "#00A884" -> R.style.Theme_DiaCero_Green
            "#D32F2F" -> R.style.Theme_DiaCero_Red
            "#7B1FA2" -> R.style.Theme_DiaCero_Purple
            "#FBC02D" -> R.style.Theme_DiaCero_Yellow
            "#E64A19" -> R.style.Theme_DiaCero_Orange
            else -> R.style.Theme_DiaCero_Blue
        }
        
        setTheme(themeRes)
    }
}
