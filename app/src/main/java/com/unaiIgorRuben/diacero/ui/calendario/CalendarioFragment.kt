package com.unaiIgorRuben.diacero.ui.calendario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.unaiIgorRuben.diacero.MainActivity
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.FragmentCalendarioBinding
import com.unaiIgorRuben.diacero.ui.calendario.adapter.CalendarioAdapter
import com.unaiIgorRuben.diacero.ui.evento.DetalleEventoActivity
import com.unaiIgorRuben.diacero.ui.rutinas.GeneradorRutinasFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CalendarioAdapter
    private var fechaSeleccionada: Date = Date()
    private var eventosDelDiaActual: List<Evento> = emptyList()
    private var tipoSeleccionado: TipoEvento? = null

    private val viewModel: CalendarioViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = EventoRepository(database.eventoDao())
        CalendarioViewModelFactory(requireActivity().application, repository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Notificar al usuario que las alarmas podrían no sonar
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

        setupCalendarVisuals() // Configuración estilo Duolingo
        setupRecyclerView()
        setupSearchView()
        setupSpinnerFiltro()
        checkNotificationPermission()

        parentFragmentManager.setFragmentResultListener("rutina_creada", this) { _, bundle ->
            val primerEventoTimestamp = bundle.getLong("primer_evento_timestamp")
            if (primerEventoTimestamp > 0) {
                fechaSeleccionada = Date(primerEventoTimestamp)
                binding.calendarView.selectedDate = calendarDayFromDate(fechaSeleccionada)
                actualizarFechaSeleccionada(fechaSeleccionada)
            }
        }

        binding.fabAgregarEvento.setOnClickListener {
            mostrarDialogoTipoCreacion()
        }
        binding.fabIrCasa.setOnClickListener {
            (activity as? MainActivity)?.volverAlMenuPrincipal()
        }

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            fechaSeleccionada = convertirCalendarDayADate(date)
            actualizarFechaSeleccionada(fechaSeleccionada)
            observeEventos(fechaSeleccionada)
        }

        fechaSeleccionada = Date()
        binding.calendarView.selectedDate = calendarDayFromDate(fechaSeleccionada)
        actualizarFechaSeleccionada(fechaSeleccionada)
        observarEventosParaMarcadores()
        observeEventos(fechaSeleccionada)
    }

    /**
     * Configura el aspecto visual del calendario siguiendo la línea de Duolingo
     */
    private fun setupCalendarVisuals() {
        binding.calendarView.apply {
            // 1. Tipografía: Usar el estilo definido con brand_font y números grandes
            setDateTextAppearance(R.style.DuoCalendarNumbers)
            setWeekDayTextAppearance(R.style.DuoCalendarNumbers)
            setHeaderTextAppearance(R.style.DuoCalendarNumbers)

            // 2. Colores de marca
            selectionColor = ContextCompat.getColor(requireContext(), R.color.brand_purple)

            // 3. Espaciado y visibilidad (más aire para que parezca un Dashboard)
            setTileHeightDp(52)
            showOtherDates = MaterialCalendarView.SHOW_ALL

            // 4. Flechas personalizadas (opcional, si tienes los iconos)
            setLeftArrow(R.drawable.ic_arrow_left)
            setRightArrow(R.drawable.ic_arrow_right)
        }
    }

    private fun setupSearchView() {
        binding.searchViewEventos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchViewEventos.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty()

                // Si hay texto, buscamos en TODOS los eventos (no solo los del día)
                if (query.length >= 2) {
                    viewModel.buscarEventos(query, tipoSeleccionado).observe(viewLifecycleOwner) { eventos ->
                        adapter.actualizarEventos(eventos)
                        binding.tvEventosDelDia.text = "Resultados para: $query"
                    }
                } else if (query.isEmpty()) {
                    // Si borramos el buscador, volvemos a mostrar los eventos del día seleccionado
                    observeEventos(fechaSeleccionada)
                    binding.tvEventosDelDia.text = getString(R.string.eventos_del_dia)
                }
                return true
            }
        })
    }


    private fun setupSpinnerFiltro() {
        val opciones = listOf(
            getString(R.string.todos),
            getString(R.string.trabajo),
            getString(R.string.tareas),
            getString(R.string.social),
            getString(R.string.fecha_importante),
            getString(R.string.otros)
        )

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opciones)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoFiltro.adapter = adapterSpinner

        binding.spinnerTipoFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tipoSeleccionado = when (position) {
                    1 -> TipoEvento.TRABAJO
                    2 -> TipoEvento.TAREAS
                    3 -> TipoEvento.SOCIAL
                    4 -> TipoEvento.FECHA_IMPORTANTE
                    5 -> TipoEvento.OTROS
                    else -> null
                }

                val query = binding.searchViewEventos.query.toString()
                if (query.isNotEmpty() || tipoSeleccionado != null) {
                    realizarBusqueda(query)
                } else {
                    limpiarBusqueda()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun realizarBusqueda(query: String) {
        viewModel.buscarEventos(query, tipoSeleccionado).observe(viewLifecycleOwner) { eventos ->
            if (binding.searchViewEventos.query.isNotEmpty() || tipoSeleccionado != null) {
                adapter.actualizarEventos(eventos)
                binding.tvEventosDelDia.text = getString(R.string.resultados_busqueda, eventos.size)
            }
        }
    }

    private fun limpiarBusqueda() {
        if (binding.searchViewEventos.query.isEmpty() && tipoSeleccionado == null) {
            binding.tvEventosDelDia.text = getString(R.string.eventos_del_dia)
            observeEventos(fechaSeleccionada)
        }
    }

    private fun mostrarDialogoTipoCreacion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.que_deseas_crear))
            .setMessage(getString(R.string.elige_tipo_creacion))
            .setNegativeButton(getString(R.string.rutina)) { _, _ ->
                parentFragmentManager.commit {
                    replace(R.id.fragmentContainer, GeneradorRutinasFragment())
                    addToBackStack(null)
                }
            }
            .setPositiveButton(getString(R.string.evento)) { _, _ ->
                val intent = Intent(requireContext(), DetalleEventoActivity::class.java).apply {
                    putExtra("SELECTED_DATE_MILLIS", fechaSeleccionada.time)
                }
                startActivity(intent)
            }
            .show()
    }

    private fun actualizarFechaSeleccionada(date: Date) {
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, "EEEE d MMMM yyyy")
        val formato = SimpleDateFormat(pattern, locale)
        binding.tvFechaSeleccionada.text = formato.format(date).replaceFirstChar { it.uppercase() }
    }

    override fun onResume() {
        super.onResume()
        cargarPreferencias()
        val query = binding.searchViewEventos.query.toString()
        if (query.isEmpty() && tipoSeleccionado == null) {
            observeEventos(fechaSeleccionada)
        } else {
            realizarBusqueda(query)
        }
    }

    private fun cargarPreferencias() {
        val prefs = requireContext().getSharedPreferences("ajustes_prefs", Context.MODE_PRIVATE)
        val mostrarEnNegro = prefs.getBoolean("eventos_pasados_negro", false)
        adapter.setMostrarPasadosEnNegro(mostrarEnNegro)
    }

    private fun observarEventosParaMarcadores() {
        viewModel.obtenerTodosLosEventos().observe(viewLifecycleOwner) { eventos ->
            aplicarDecoradoresEventos(eventos)
            actualizarResumenEventosDelDia(eventosDelDiaActual)
        }
    }

    private fun aplicarDecoradoresEventos(eventos: List<Evento>) {
        binding.calendarView.removeDecorators()

        // Decorador de texto para modo oscuro/claro
        val isDarkMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            binding.calendarView.addDecorator(CalendarTextColorDecorator(Color.WHITE))
        } else {
            binding.calendarView.addDecorator(CalendarTextColorDecorator(Color.parseColor("#4B4B4B")))
        }

        val eventosPorDia = eventos.groupBy { calendarDayFromDate(it.fecha) }

        eventosPorDia.forEach { (dia, listaEventos) ->
            val colores = listaEventos.take(3).map {
                it.tipo.color.toColorInt()
            }
            val extraCount = if (listaEventos.size > 3) listaEventos.size - 3 else 0
            val esPrioritario = listaEventos.any { it.esPrioritario }

            binding.calendarView.addDecorator(
                EventDayDecorator(
                    day = dia,
                    colors = colores,
                    extraCount = extraCount,
                    isPriority = esPrioritario
                )
            )
        }
    }

    private fun convertirCalendarDayADate(day: CalendarDay): Date {
        return Calendar.getInstance().apply {
            set(day.year, day.month - 1, day.day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun calendarDayFromDate(date: Date): CalendarDay {
        val calendar = Calendar.getInstance().apply { time = date }
        return CalendarDay.from(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
            eventosDelDiaActual = eventos
            if (binding.searchViewEventos.query.isEmpty() && tipoSeleccionado == null) {
                adapter.actualizarEventos(eventos)
                actualizarResumenEventosDelDia(eventos)
            }
        }
    }

    private fun actualizarResumenEventosDelDia(eventos: List<Evento>) {
        if (eventos.isEmpty()) {
            binding.tvEventosDelDia.text = getString(R.string.no_hay_eventos_dia)
            return
        }
        val titulos = eventos.take(2).joinToString(" | ") { it.titulo }
        val sufijo = if (eventos.size > 2) getString(R.string.mas_eventos, eventos.size - 2) else ""
        binding.tvEventosDelDia.text = getString(R.string.resumen_eventos, eventos.size, titulos, sufijo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}