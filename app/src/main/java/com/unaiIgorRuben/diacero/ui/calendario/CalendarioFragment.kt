package com.unaiIgorRuben.diacero.ui.calendario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.FragmentCalendarioBinding
import com.unaiIgorRuben.diacero.ui.rutinas.GeneradorRutinasFragment
import java.util.Calendar
import java.util.Date

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarioViewModel by viewModels { 
        val database = AppDatabase.getDatabase(requireContext()) 
        val repository = EventoRepository(database.eventoDao())
        CalendarioViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Escuchar por el resultado del GeneradorRutinasFragment
        parentFragmentManager.setFragmentResultListener("rutina_creada", this) { requestKey, bundle ->
            val primerEventoTimestamp = bundle.getLong("primer_evento_timestamp")
            if (primerEventoTimestamp > 0) {
                binding.calendarView.date = primerEventoTimestamp
                // Al cambiar la fecha, el setOnDateChangeListener se disparará automáticamente
            }
        }

        binding.fabAgregarEvento.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, GeneradorRutinasFragment())
                addToBackStack(null) 
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            observeEventos(calendar.time)
        }

        observeEventos(Date()) // Carga los eventos para hoy
    }

    private fun observeEventos(date: Date) {
        val cal = Calendar.getInstance()
        cal.time = date
        
        // Rango de búsqueda: desde el inicio del día seleccionado
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.time

        // Rango de búsqueda: hasta el inicio del día siguiente
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.time

        viewModel.obtenerEventosPorRango(startOfDay, endOfDay).observe(viewLifecycleOwner) { eventos ->
            if (eventos.isEmpty()) {
                binding.tvEventosDelDia.text = "No hay eventos para este día."
            } else {
                binding.tvEventosDelDia.text = "Eventos: \n" + eventos.joinToString("\n") { it.titulo }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
