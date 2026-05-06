package com.unaiIgorRuben.diacero.ui.ajustes

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.ActivityAjustesBinding
import com.unaiIgorRuben.diacero.ui.BaseActivity
import kotlinx.coroutines.launch

class AjustesActivity : BaseActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var repository: EventoRepository
    private val PREFS_NAME = "ajustes_prefs"
    private val KEY_FONT_SIZE = "font_size"
    private val KEY_NOTIFICACIONES = "notificaciones_enabled"
    private val KEY_EVENTOS_PASADOS_NEGRO = "eventos_pasados_negro"
    private val KEY_COLOR_CLARO = "color_claro"
    private val KEY_COLOR_OSCURO = "color_oscuro"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getDatabase(this).eventoDao()
        repository = EventoRepository(dao)

        setupToolbar()
        setupListeners()
        cargarPreferencias()
    }

    private fun setupToolbar() {
        binding.toolbarAjustes.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnIdioma.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de idioma próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnTamanoLetra.setOnClickListener {
            mostrarDialogoTamanoLetra()
        }

        binding.btnLimpiarDatos.setOnClickListener {
            mostrarConfirmacionEliminar()
        }
        
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            guardarPreferenciaNotificaciones(isChecked)
            val mensaje = if (isChecked) "Notificaciones activadas" else "Notificaciones desactivadas"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        binding.switchEventosPasados.setOnCheckedChangeListener { _, isChecked ->
            guardarPreferenciaEventosPasados(isChecked)
        }

        binding.btnColorClaro.setOnClickListener {
            mostrarSelectorColor(true)
        }

        binding.btnColorOscuro.setOnClickListener {
            mostrarSelectorColor(false)
        }
    }

    private fun cargarPreferencias() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Cargar tamaño de letra
        val tamano = prefs.getString(KEY_FONT_SIZE, "Normal")
        binding.tvTamanoLetraActual.text = tamano
        
        // Cargar estado de notificaciones
        val notificacionesEnabled = prefs.getBoolean(KEY_NOTIFICACIONES, true)
        binding.switchNotificaciones.isChecked = notificacionesEnabled

        // Cargar estado de eventos pasados
        val eventosPasadosNegro = prefs.getBoolean(KEY_EVENTOS_PASADOS_NEGRO, false)
        binding.switchEventosPasados.isChecked = eventosPasadosNegro

        // Cargar colores
        val colorClaro = prefs.getInt(KEY_COLOR_CLARO, Color.parseColor("#0F6CBD"))
        val colorOscuro = prefs.getInt(KEY_COLOR_OSCURO, Color.parseColor("#66B5FF"))
        
        binding.viewColorClaro.backgroundTintList = ColorStateList.valueOf(colorClaro)
        binding.viewColorOscuro.backgroundTintList = ColorStateList.valueOf(colorOscuro)
    }

    private fun mostrarSelectorColor(esClaro: Boolean) {
        val colores = arrayOf(
            "#0F6CBD", "#00A884", "#D32F2F", "#7B1FA2", "#FBC02D", "#E64A19", "#388E3C", "#1976D2"
        )
        val nombres = arrayOf(
            "Azul", "Verde", "Rojo", "Púrpura", "Amarillo", "Naranja", "Verde Oscuro", "Azul Intenso"
        )

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Color")
            .setItems(nombres) { _, which ->
                val colorHex = colores[which]
                val colorInt = Color.parseColor(colorHex)
                guardarColor(esClaro, colorInt)
                
                // Actualizar la vista previa
                if (esClaro) {
                    binding.viewColorClaro.backgroundTintList = ColorStateList.valueOf(colorInt)
                } else {
                    binding.viewColorOscuro.backgroundTintList = ColorStateList.valueOf(colorInt)
                }

                // Aplicar en tiempo real si el modo cambiado es el actual
                val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                if (esClaro != isNightMode) {
                    recreate()
                } else {
                    Toast.makeText(this, "Color guardado para el otro modo", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun guardarColor(esClaro: Boolean, color: Int) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (esClaro) KEY_COLOR_CLARO else KEY_COLOR_OSCURO
        prefs.edit().putInt(key, color).apply()
    }

    private fun mostrarDialogoTamanoLetra() {
        val opciones = arrayOf("Pequeño", "Normal", "Grande")
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val actual = prefs.getString(KEY_FONT_SIZE, "Normal")
        val checkedItem = opciones.indexOf(actual)

        AlertDialog.Builder(this)
            .setTitle("Seleccionar tamaño de letra")
            .setSingleChoiceItems(opciones, checkedItem) { dialog, which ->
                val seleccionado = opciones[which]
                guardarTamanoLetra(seleccionado)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarTamanoLetra(tamano: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FONT_SIZE, tamano).apply()
    }

    private fun guardarPreferenciaNotificaciones(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICACIONES, enabled).apply()
    }

    private fun guardarPreferenciaEventosPasados(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EVENTOS_PASADOS_NEGRO, enabled).apply()
    }

    private fun mostrarConfirmacionEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar todos los eventos")
            .setMessage("¿Estás seguro de que quieres borrar todos los eventos? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarTodosLos_eventos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTodosLos_eventos() {
        lifecycleScope.launch {
            try {
                repository.eliminarTodosLosEventos()
                Toast.makeText(this@AjustesActivity, "Todos los eventos han sido eliminados", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AjustesActivity, "Error al eliminar eventos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
