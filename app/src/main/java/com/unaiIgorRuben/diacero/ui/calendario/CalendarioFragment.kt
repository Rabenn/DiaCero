package com.unaiIgorRuben.diacero.ui.calendario

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.FragmentCalendarioBinding
import com.unaiIgorRuben.diacero.ui.calendario.adapter.CalendarioAdapter
import com.unaiIgorRuben.diacero.ui.evento.DetalleEventoActivity
import com.unaiIgorRuben.diacero.ui.rutinas.GeneradorRutinasFragment
import java.util.Calendar
import java.util.Date

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CalendarioAdapter

    private val viewModel: CalendarioViewModel by viewModels { 
        val database = AppDatabase.getDatabase(requireContext()) 
        val repository = EventoRepository(database.eventoDao())
        CalendarioViewModelFactory(requireActivity().application, repository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Informar al usuario que no recibirá notificaciones
        }
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

        setupRecyclerView()
        checkNotificationPermission()

        parentFragmentManager.setFragmentResultListener("rutina_creada", this) { _, bundle ->
            val primerEventoTimestamp = bundle.getLong("primer_evento_timestamp")
            if (primerEventoTimestamp > 0) {
                binding.calendarView.date = primerEventoTimestamp
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

        observeEventos(Date())
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CalendarioAdapter(
            eventos = emptyList(),
            onItemClick = { evento ->
                val intent = Intent(requireContext(), DetalleEventoActivity::class.java).apply {
                    putExtra("EVENTO_ID", evento.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { }
        )
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter
    }

    private fun observeEventos(date: Date) {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.time

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.time

        viewModel.obtenerEventosPorRango(startOfDay, endOfDay).observe(viewLifecycleOwner) { eventos ->
            if (eventos.isEmpty()) {
                binding.tvEventosDelDia.visibility = View.VISIBLE
                binding.tvEventosDelDia.text = "No hay eventos para este día."
                adapter.actualizarEventos(emptyList())
            } else {
                binding.tvEventosDelDia.visibility = View.GONE
                adapter.actualizarEventos(eventos)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
