package com.unaiIgorRuben.diacero

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.unaiIgorRuben.diacero.databinding.ActivityMainBinding
import com.unaiIgorRuben.diacero.ui.BaseActivity
import com.unaiIgorRuben.diacero.ui.ajustes.AjustesActivity
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioFragment
import com.unaiIgorRuben.diacero.ui.habitos.HabitosFragment
import com.unaiIgorRuben.diacero.ui.rutinas.GeneradorRutinasFragment
import com.unaiIgorRuben.diacero.util.FontResizer

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actualizarIconoTema()
        setupMenuActions(savedInstanceState)
        setupSettingsButton()
        
        supportFragmentManager.addOnBackStackChangedListener {
            actualizarVisibilidadSegunBackStack()
        }

        checkIntentExtras(intent)
        actualizarVisibilidadSegunBackStack()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntentExtras(intent)
    }

    private fun checkIntentExtras(intent: Intent?) {
        if (intent?.getBooleanExtra("GO_HOME", false) == true) {
            volverAlMenuPrincipal()
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarIconoTema()
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
            cargarHabitos()
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

    private fun cargarHabitos() {
        binding.menuContainer.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, HabitosFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    fun volverAlMenuPrincipal() {
        // Limpiamos todo el backstack de fragmentos
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        
        // Eliminamos fragmentos que puedan haber quedado sin estar en el backstack
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment != null) {
            supportFragmentManager.commit {
                remove(fragment)
            }
        }

        binding.menuContainer.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        binding.fabSettings.visibility = View.VISIBLE
    }

    private fun actualizarVisibilidadSegunBackStack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            binding.menuContainer.visibility = View.GONE
            binding.fragmentContainer.visibility = View.VISIBLE
            
            // Verificamos qué fragmento está visible actualmente
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (currentFragment is GeneradorRutinasFragment) {
                binding.fabSettings.visibility = View.GONE
            } else {
                binding.fabSettings.visibility = View.VISIBLE
            }
        } else {
            binding.menuContainer.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
            binding.fabSettings.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // Pequeño delay o post para asegurar que la visibilidad se actualiza tras el pop
            binding.root.post { actualizarVisibilidadSegunBackStack() }
        } else {
            super.onBackPressed()
        }
    }
}
