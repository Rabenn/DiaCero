package com.unaiIgorRuben.diacero

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.unaiIgorRuben.diacero.databinding.ActivityMainBinding
import com.unaiIgorRuben.diacero.ui.BaseActivity
import com.unaiIgorRuben.diacero.ui.ajustes.AjustesActivity
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioFragment
import com.unaiIgorRuben.diacero.util.FontResizer

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFontSize: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentFontSize = FontResizer.getSavedSize(this)
        
        actualizarIconoTema()
        setupMenuActions(savedInstanceState)
        setupSettingsButton()
    }

    override fun onResume() {
        super.onResume()
        // Si el tamaño de letra ha cambiado mientras estábamos en otra pantalla, recreamos la actividad
        val newSize = FontResizer.getSavedSize(this)
        if (currentFontSize != null && currentFontSize != newSize) {
            recreate()
        }
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

        binding.btnAjustes.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
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
