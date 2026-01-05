package com.unaiIgorRuben.diacero.ui.evento

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.ActivityDetalleEventoBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEventoBinding
    private var eventoId: Int = -1

    private val viewModel: DetalleEventoViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        DetalleEventoViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventoId = intent.getIntExtra("EVENTO_ID", -1)
        if (eventoId == -1) {
            finish() // No hay evento que mostrar
            return
        }

        setupToolbar()
        observeEvento()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun observeEvento() {
        viewModel.obtenerEvento(eventoId).observe(this) { evento ->
            evento?.let { mostrarDetalles(it) }
        }
    }

    private fun setupListeners() {
        binding.btnEliminar.setOnClickListener {
            viewModel.eliminarEvento(eventoId)
            finish()
        }
    }

    private fun mostrarDetalles(evento: Evento) {
        binding.tvTitulo.text = evento.titulo

        if (evento.descripcion?.isNotEmpty() == true) {
            binding.tvDescripcion.text = evento.descripcion
            binding.tvDescripcion.visibility = View.VISIBLE
        } else {
            binding.tvDescripcion.visibility = View.GONE
        }

        val sdfFecha = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.tvFecha.text = sdfFecha.format(evento.fecha)
        binding.tvHora.text = "${sdfHora.format(evento.horaInicio)} - ${sdfHora.format(evento.horaFin)}"

        val duracionMs = evento.horaFin.time - evento.horaInicio.time
        val duracionMinutos = duracionMs / 60000
        binding.tvDuracion.text = "Duración: $duracionMinutos min"

        if (evento.ubicacion?.isNotEmpty() == true) {
            binding.tvUbicacion.text = evento.ubicacion
            binding.layoutUbicacion.visibility = View.VISIBLE
        } else {
            binding.layoutUbicacion.visibility = View.GONE
        }

        if (evento.color.isNotEmpty()) {
            binding.colorIndicator.setBackgroundColor(android.graphics.Color.parseColor(evento.color))
        } else {
            binding.colorIndicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        binding.chipPrioritario.visibility = if (evento.esPrioritario) View.VISIBLE else View.GONE

        binding.btnVerFoto.visibility = if (evento.fotoRuta != null) View.VISIBLE else View.GONE
        binding.btnReproducirAudio.visibility = if (evento.audioRuta != null) View.VISIBLE else View.GONE
    }
}
