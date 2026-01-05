package com.unaiIgorRuben.diacero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.unaiIgorRuben.diacero.databinding.ActivityMainBinding
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioFragment

/**
 * ACTIVIDAD PRINCIPAL
 *
 * Contiene el calendario como pantalla inicial.
 * Aquí puedes añadir:
 * - BottomNavigationView para navegar entre secciones
 * - Toolbar con menú
 * - Navigation Drawer (cajón lateral)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar el CalendarioFragment al iniciar
        if (savedInstanceState == null) {
            cargarCalendario()
        }
    }

    /**
     * Carga el fragment del calendario
     */
    private fun cargarCalendario() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CalendarioFragment())
            setReorderingAllowed(true)
        }
    }
}