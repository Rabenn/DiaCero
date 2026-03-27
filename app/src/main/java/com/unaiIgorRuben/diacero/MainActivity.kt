package com.unaiIgorRuben.diacero

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            cargarCalendario()
        }

        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        updateThemeIcon()

        binding.fabThemeToggle.setOnClickListener {
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun updateThemeIcon() {
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            binding.fabThemeToggle.setImageResource(R.drawable.ic_sun)
            binding.fabThemeToggle.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4ED9BC"))
            binding.fabThemeToggle.imageTintList = ColorStateList.valueOf(Color.parseColor("#08131C"))
        } else {
            binding.fabThemeToggle.setImageResource(R.drawable.ic_moon)
            binding.fabThemeToggle.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0F6CBD"))
            binding.fabThemeToggle.imageTintList = ColorStateList.valueOf(Color.WHITE)
        }
    }

    private fun cargarCalendario() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CalendarioFragment())
            setReorderingAllowed(true)
        }
    }
}
