package com.unaiIgorRuben.diacero.ui.rutinas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.FragmentGeneradorRutinasBinding
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioViewModel
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class GeneradorRutinasFragment : Fragment() {

    private var _binding: FragmentGeneradorRutinasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarioViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = EventoRepository(database.eventoDao())
        CalendarioViewModelFactory(repository)
    }

    private val diasSeleccionados = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneradorRutinasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        configurarCheckboxDias()
        configurarBotonGenerar()
    }

    private fun configurarCheckboxDias() {
        binding.checkLunes.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.MONDAY, isChecked) }
        binding.checkMartes.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.TUESDAY, isChecked) }
        binding.checkMiercoles.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.WEDNESDAY, isChecked) }
        binding.checkJueves.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.THURSDAY, isChecked) }
        binding.checkViernes.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.FRIDAY, isChecked) }
        binding.checkSabado.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.SATURDAY, isChecked) }
        binding.checkDomingo.setOnCheckedChangeListener { _, isChecked -> actualizarDia(Calendar.SUNDAY, isChecked) }
    }

    private fun actualizarDia(dia: Int, seleccionado: Boolean) {
        if (seleccionado) {
            diasSeleccionados.add(dia)
        } else {
            diasSeleccionados.remove(dia)
        }
    }

    private fun configurarBotonGenerar() {
        binding.btnGenerar.setOnClickListener {
            generarRutina()
        }
    }

    private fun generarRutina() {
        val titulo = binding.etTitulo.text.toString()
        if (titulo.isBlank()) {
            Toast.makeText(context, "Ingresa un título", Toast.LENGTH_SHORT).show()
            return
        }

        if (diasSeleccionados.isEmpty()) {
            Toast.makeText(context, "Selecciona al menos un día", Toast.LENGTH_SHORT).show()
            return
        }

        val horaInicio = binding.timePickerInicio.hour
        val minutoInicio = binding.timePickerInicio.minute
        val horaFin = binding.timePickerFin.hour
        val minutoFin = binding.timePickerFin.minute
        val semanas = binding.etSemanas.text.toString().toIntOrNull() ?: 4
        val descripcion = binding.etDescripcion.text.toString()
        val ubicacion = binding.etUbicacion.text.toString()

        lifecycleScope.launch {
            val eventos = generarEventosRecurrentes(
                titulo, descripcion, ubicacion, horaInicio, minutoInicio, horaFin, minutoFin, diasSeleccionados, semanas
            )

            if (eventos.isNotEmpty()) {
                viewModel.insertarVarios(eventos)

                // Enviar la fecha del primer evento de vuelta al calendario
                val primerEvento = eventos.first()
                parentFragmentManager.setFragmentResult(
                    "rutina_creada",
                    bundleOf("primer_evento_timestamp" to primerEvento.fecha.time)
                )

                Toast.makeText(context, "✅ Se crearon ${eventos.size} eventos", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack() // Volver automáticamente
            } else {
                Toast.makeText(context, "No se generaron eventos en el futuro. Revisa los días.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generarEventosRecurrentes(
        titulo: String, descripcion: String?, ubicacion: String?,
        horaInicio: Int, minutoInicio: Int, horaFin: Int, minutoFin: Int,
        diasSemana: Set<Int>, cantidadSemanas: Int
    ): List<Evento> {
        val eventos = mutableListOf<Evento>()
        val calendario = Calendar.getInstance() // Empezamos desde hoy

        // Iteramos durante (semanas * 7) días
        for (i in 0 until cantidadSemanas * 7) {
            val diaDeLaSemanaActual = calendario.get(Calendar.DAY_OF_WEEK)

            if (diasSemana.contains(diaDeLaSemanaActual)) {
                // Normalizar la fecha a medianoche (para la búsqueda)
                val fechaNormalizada = (calendario.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time

                val dateInicio = (calendario.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, horaInicio); set(Calendar.MINUTE, minutoInicio) }.time
                val dateFin = (calendario.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, horaFin); set(Calendar.MINUTE, minutoFin) }.time

                eventos.add(
                    Evento(
                        titulo = titulo,
                        fecha = fechaNormalizada,
                        horaInicio = dateInicio,
                        horaFin = dateFin,
                        descripcion = descripcion?.ifBlank { null },
                        ubicacion = ubicacion?.ifBlank { null },
                        color = "#4CAF50" // Verde para rutinas
                    )
                )
            }

            // Pasamos al siguiente día
            calendario.add(Calendar.DAY_OF_YEAR, 1)
        }
        return eventos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
