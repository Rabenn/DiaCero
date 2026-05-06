package com.unaiIgorRuben.diacero

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commit
import com.unaiIgorRuben.diacero.databinding.ActivityMainBinding
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioFragment

/**
 * ACTIVIDAD PRINCIPAL
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSettingsButton()
        setupMenuActions(savedInstanceState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupSettingsButton() {
        updateSettingsButtonColors()

        binding.fabSettings.setOnClickListener {
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun updateSettingsButtonColors() {
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            binding.fabSettings.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4ED9BC"))
            binding.fabSettings.imageTintList = ColorStateList.valueOf(Color.parseColor("#08131C"))
        } else {
            binding.fabSettings.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0F6CBD"))
            binding.fabSettings.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
    }

    private fun cargarCalendario() {
        binding.menuContainer.visibility = android.view.View.GONE
        binding.fragmentContainer.visibility = android.view.View.VISIBLE

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CalendarioFragment())
            setReorderingAllowed(true)
        }
    }

    private fun setupMenuActions(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            binding.menuContainer.visibility = android.view.View.GONE
            binding.fragmentContainer.visibility = android.view.View.VISIBLE
            return
        }

        binding.btnCalendario.setOnClickListener {
            cargarCalendario()
        }

        binding.btnHabitos.setOnClickListener {
            Toast.makeText(this, "Habitos: proximo modulo", Toast.LENGTH_SHORT).show()
        }
    }

}
