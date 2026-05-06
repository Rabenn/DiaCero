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
import com.unaiIgorRuben.diacero.util.LocaleHelper
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
            mostrarDialogoIdioma()
        }

        binding.btnTamanoLetra.setOnClickListener {
            mostrarDialogoTamanoLetra()
        }

        binding.btnLimpiarDatos.setOnClickListener {
            mostrarConfirmacionEliminar()
        }
        
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            guardarPreferenciaNotificaciones(isChecked)
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
        
        actualizarTextoIdioma()
        
        val tamano = prefs.getString(KEY_FONT_SIZE, "Normal")
        binding.tvTamanoLetraActual.text = tamano
        
        val notificacionesEnabled = prefs.getBoolean(KEY_NOTIFICACIONES, true)
        binding.switchNotificaciones.isChecked = notificacionesEnabled

        val eventosPasadosNegro = prefs.getBoolean(KEY_EVENTOS_PASADOS_NEGRO, false)
        binding.switchEventosPasados.isChecked = eventosPasadosNegro

        val colorClaro = prefs.getInt(KEY_COLOR_CLARO, Color.parseColor("#0F6CBD"))
        val colorOscuro = prefs.getInt(KEY_COLOR_OSCURO, Color.parseColor("#66B5FF"))
        
        binding.viewColorClaro.backgroundTintList = ColorStateList.valueOf(colorClaro)
        binding.viewColorOscuro.backgroundTintList = ColorStateList.valueOf(colorOscuro)
    }

    private fun actualizarTextoIdioma() {
        val lang = LocaleHelper.getSelectedLanguage(this)
        val text = when (lang) {
            "es" -> "Español"
            "en" -> "English"
            "eu" -> "Euskara"
            else -> "Español"
        }
        binding.tvIdiomaActual.text = text
    }

    private fun mostrarDialogoIdioma() {
        val idiomas = arrayOf("Español", "English", "Euskara")
        val codigos = arrayOf("es", "en", "eu")
        val actual = LocaleHelper.getSelectedLanguage(this)
        val checkedItem = codigos.indexOf(actual)

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Idioma")
            .setSingleChoiceItems(idiomas, checkedItem) { dialog, which ->
                LocaleHelper.saveLanguage(this, codigos[which])
                dialog.dismiss()
                recreate() 
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarSelectorColor(esClaro: Boolean) {
        val colores = arrayOf("#0F6CBD", "#00A884", "#D32F2F", "#7B1FA2", "#FBC02D", "#E64A19", "#388E3C", "#1976D2")
        val nombres = arrayOf("Azul", "Verde", "Rojo", "Púrpura", "Amarillo", "Naranja", "Verde Oscuro", "Azul Intenso")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Color")
            .setItems(nombres) { _, which ->
                val colorHex = colores[which]
                val colorInt = Color.parseColor(colorHex)
                guardarColor(esClaro, colorInt)
                recreate()
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
                guardarTamanoLetra(opciones[which])
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
            .setMessage("¿Estás seguro de que quieres borrar todos los eventos?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarTodosLos_eventos() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTodosLos_eventos() {
        lifecycleScope.launch {
            try {
                repository.eliminarTodosLosEventos()
                Toast.makeText(this@AjustesActivity, "Eventos eliminados", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AjustesActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
