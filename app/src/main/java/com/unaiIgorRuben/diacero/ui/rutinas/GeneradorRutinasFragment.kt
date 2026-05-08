package com.unaiIgorRuben.diacero.ui.rutinas

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.unaiIgorRuben.diacero.MainActivity
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.FragmentGeneradorRutinasBinding
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioViewModel
import com.unaiIgorRuben.diacero.ui.calendario.CalendarioViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GeneradorRutinasFragment : Fragment() {

    private var _binding: FragmentGeneradorRutinasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarioViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = EventoRepository(database.eventoDao())
        CalendarioViewModelFactory(requireActivity().application, repository)
    }

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(requireContext()) }
    private val diasSeleccionados = mutableSetOf<Int>()
    
    private var horaInicio: Int = 9
    private var minutoInicio: Int = 0
    private var horaFin: Int = 10
    private var minutoFin: Int = 0

    // Multimedia
    private var fotoRutaSeleccionada: String? = null
    private var audioRutaSeleccionada: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var estaGrabando = false
    private var archivoGrabacion: File? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fotoRutaSeleccionada = it.toString()
            actualizarTextoMultimedia()
        }
    }

    private val selectAudioLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            audioRutaSeleccionada = it.toString()
            actualizarTextoMultimedia()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) iniciarOPararGrabacion()
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(requireContext(), getString(R.string.permiso_de_ubicaci_n_denegado), Toast.LENGTH_SHORT).show()
        }
    }

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

        binding.toolbar.menu.clear()
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        configurarCheckboxDias()
        configurarBotonGenerar()
        setupMultimediaListeners()
        setupLocationButton()
        setupTimePickers()
    }

    private fun setupTimePickers() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val calInicio = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaInicio)
            set(Calendar.MINUTE, minutoInicio)
        }
        binding.etHoraInicio.setText(sdf.format(calInicio.time))

        val calFin = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaFin)
            set(Calendar.MINUTE, minutoFin)
        }
        binding.etHoraFin.setText(sdf.format(calFin.time))

        binding.etHoraInicio.setOnClickListener { mostrarTimePicker(true) }
        binding.etHoraFin.setOnClickListener { mostrarTimePicker(false) }
    }

    private fun mostrarTimePicker(esInicio: Boolean) {
        val hour = if (esInicio) horaInicio else horaFin
        val minute = if (esInicio) minutoInicio else minutoFin

        TimePickerDialog(requireContext(), { _, h, m ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
            }
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            if (esInicio) {
                horaInicio = h
                minutoInicio = m
                binding.etHoraInicio.setText(sdf.format(cal.time))
            } else {
                horaFin = h
                minutoFin = m
                binding.etHoraFin.setText(sdf.format(cal.time))
            }
        }, hour, minute, true).show()
    }

    private fun setupLocationButton() {
        binding.inputLayoutUbicacion.setEndIconOnClickListener {
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            obtenerUbicacionActual()
        }
    }

    private fun obtenerUbicacionActual() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        binding.etUbicacion.setText(address)
                        Toast.makeText(requireContext(), getString(R.string.ubicaci_n_obtenida), Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(requireContext(), getString(R.string.no_se_pudo_obtener_la_ubicaci_n_activa_el_gps), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupMultimediaListeners() {
        binding.btnAdjuntarFoto.setOnClickListener { selectImageLauncher.launch(arrayOf("image/*")) }
        binding.btnAdjuntarAudio.setOnClickListener { selectAudioLauncher.launch(arrayOf("audio/*")) }
        binding.btnGrabarAudio.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                iniciarOPararGrabacion()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun iniciarOPararGrabacion() {
        if (!estaGrabando) {
            archivoGrabacion = File(requireContext().externalCacheDir, "grabacion_${System.currentTimeMillis()}.mp3")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(archivoGrabacion?.absolutePath)
                prepare()
                start()
            }
            estaGrabando = true
            binding.btnGrabarAudio.text = getString(R.string.detener_grabaci_n)
            binding.tvEstadoGrabacion.visibility = View.VISIBLE
        } else {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            estaGrabando = false
            binding.btnGrabarAudio.text = getString(R.string.grabar_nota_de_voz)
            binding.tvEstadoGrabacion.visibility = View.GONE
            audioRutaSeleccionada = Uri.fromFile(archivoGrabacion).toString()
            actualizarTextoMultimedia()
        }
    }

    private fun actualizarTextoMultimedia() {
        val foto = if (fotoRutaSeleccionada != null) getString(R.string.foto_listo) else ""
        val audio = if (audioRutaSeleccionada != null) getString(R.string.audio_listo) else ""
        if (foto.isNotEmpty() || audio.isNotEmpty()) {
            binding.tvRutaMultimedia.visibility = View.VISIBLE
            binding.tvRutaMultimedia.text = getString(R.string.multimedia_estado, foto, audio)
        }
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
        if (seleccionado) diasSeleccionados.add(dia) else diasSeleccionados.remove(dia)
    }

    private fun configurarBotonGenerar() {
        binding.btnGenerar.setOnClickListener { generarRutina() }
    }

    private fun generarRutina() {
        val titulo = binding.etTitulo.text.toString()
        if (titulo.isBlank() || diasSeleccionados.isEmpty()) {
            Toast.makeText(context,
                getString(R.string.completa_el_t_tulo_y_d_as), Toast.LENGTH_SHORT).show()
            return
        }

        val semanas = binding.etSemanas.text.toString().toIntOrNull() ?: 1
        val descripcion = binding.etDescripcion.text.toString()
        val ubicacion = binding.etUbicacion.text.toString()
        val tieneAlarma = binding.cbAlarma.isChecked

        lifecycleScope.launch {
            val eventos = generarEventosRecurrentes(
                titulo, descripcion, ubicacion, horaInicio, minutoInicio, horaFin, minutoFin, diasSeleccionados, semanas, tieneAlarma
            )

            if (eventos.isNotEmpty()) {
                viewModel.insertarVarios(eventos)
                
                if (tieneAlarma) {
                    programarAlarmaSistemaParaRutina(titulo, horaInicio, minutoInicio, diasSeleccionados)
                }

                parentFragmentManager.setFragmentResult("rutina_creada", bundleOf("primer_evento_timestamp" to eventos.first().fecha.time))
                Toast.makeText(context,
                    getString(R.string.se_crearon_eventos, eventos.size), Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun programarAlarmaSistemaParaRutina(titulo: String, hora: Int, minuto: Int, dias: Set<Int>) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, getString(R.string.diacero_rutina, titulo))
            putExtra(AlarmClock.EXTRA_HOUR, hora)
            putExtra(AlarmClock.EXTRA_MINUTES, minuto)
            
            val daysList = ArrayList<Int>()
            dias.forEach { day ->
                daysList.add(day)
            }
            putExtra(AlarmClock.EXTRA_DAYS, daysList)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(),
                getString(R.string.no_se_pudo_abrir_la_app_de_alarmas), Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarEventosRecurrentes(
        titulo: String, descripcion: String?, ubicacion: String?,
        horaInicio: Int, minutoInicio: Int, horaFin: Int, minutoFin: Int,
        diasSemana: Set<Int>, cantidadSemanas: Int, tieneAlarma: Boolean
    ): List<Evento> {
        val eventos = mutableListOf<Evento>()
        val calendario = Calendar.getInstance()

        for (i in 0 until cantidadSemanas * 7) {
            if (diasSemana.contains(calendario.get(Calendar.DAY_OF_WEEK))) {
                val fechaNormalizada = (calendario.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.time

                val dateInicio = (calendario.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, horaInicio); set(Calendar.MINUTE, minutoInicio) }.time
                val dateFin = (calendario.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, horaFin); set(Calendar.MINUTE, minutoFin) }.time

                eventos.add(Evento(
                    titulo = titulo, fecha = fechaNormalizada, horaInicio = dateInicio, horaFin = dateFin,
                    descripcion = descripcion?.ifBlank { null }, ubicacion = ubicacion?.ifBlank { null },
                    color = "#4CAF50", tieneAlarma = tieneAlarma,
                    fotoRuta = fotoRutaSeleccionada, audioRuta = audioRutaSeleccionada
                ))
            }
            calendario.add(Calendar.DAY_OF_YEAR, 1)
        }
        return eventos
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
