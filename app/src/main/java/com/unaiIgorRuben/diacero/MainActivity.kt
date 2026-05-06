package com.unaiIgorRuben.diacero

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.unaiIgorRuben.diacero.databinding.ActivityMainBinding
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actualizarIconoTema()
        setupMenuActions(savedInstanceState)
        setupSettingsButton()
    }

    private fun setupSettingsButton() {
        binding.fabSettings.setOnClickListener {
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun actualizarIconoTema() {
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            binding.fabSettings.setImageResource(R.drawable.ic_sun)
        } else {
            binding.fabSettings.setImageResource(R.drawable.ic_moon)
        }
    }

    private fun setupMenuActions(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null) {
                binding.menuContainer.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
            }
        }

        binding.btnCalendario.setOnClickListener {
            cargarCalendario()
        }

        binding.btnHabitos.setOnClickListener {
            Toast.makeText(this, "Próximamente: Módulo de Hábitos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarCalendario() {
        binding.menuContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CalendarioFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            binding.menuContainer.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}
