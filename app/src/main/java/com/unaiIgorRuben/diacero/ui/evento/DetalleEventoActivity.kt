package com.unaiIgorRuben.diacero.ui.evento

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.MainActivity
import com.unaiIgorRuben.diacero.R
import com.unaiIgorRuben.diacero.databinding.ActivityDetalleEventoBinding
import com.unaiIgorRuben.diacero.ui.BaseActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetalleEventoActivity : BaseActivity() {

    private lateinit var binding: ActivityDetalleEventoBinding
    private var eventoId: Int = -1
    private var eventoActual: Evento? = null
    private var estaEnModoEdicion = false
    private var esModoCreacion = false
    private var fechaSeleccionada: Date = Date()

    private var horaInicioEditada: Date? = null
    private var horaFinEditada: Date? = null
    private var fotoRutaSeleccionada: String? = null
    private var audioRutaSeleccionada: String? = null
    private var tipoSeleccionado: TipoEvento = TipoEvento.OTROS

    private var mediaRecorder: MediaRecorder? = null
    private var estaGrabando = false
    private var archivoGrabacion: File? = null
    private var mediaPlayer: MediaPlayer? = null

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            iniciarOPararGrabacion()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fotoRutaSeleccionada = it.toString()
            binding.tvRutaFoto.text = "Foto seleccionada"
            binding.tvRutaFoto.visibility = View.VISIBLE
        }
    }

    private val selectAudioLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            audioRutaSeleccionada = it.toString()
            binding.tvRutaAudio.text = "Audio seleccionado"
            binding.tvRutaAudio.visibility = View.VISIBLE
        }
    }

    private val viewModel: DetalleEventoViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        val repository = EventoRepository(database.eventoDao())
        DetalleEventoViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventoId = intent.getIntExtra("EVENTO_ID", -1)
        esModoCreacion = eventoId == -1
        fechaSeleccionada = Date(intent.getLongExtra("SELECTED_DATE_MILLIS", System.currentTimeMillis()))

        setupToolbar()
        setupTipoSpinner()
        if (esModoCreacion) prepararModoCreacion() else observeEvento()
        setupListeners()
        setupLocationButton()
    }

    private fun setupTipoSpinner() {
        val tipos = TipoEvento.values().map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        binding.autoCompleteTipo.setAdapter(adapter)
        
        binding.autoCompleteTipo.setOnItemClickListener { _, _, position, _ ->
            tipoSeleccionado = TipoEvento.values()[position]
        }
    }

    private fun setupLocationButton() {
        binding.inputLayoutUbicacion.setEndIconOnClickListener {
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        binding.etUbicacion.setText(address)
                        Toast.makeText(this, "Ubicación obtenida", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(this, "No se pudo obtener la ubicación. Activa el GPS.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.menu.clear()
        binding.toolbar.menu.add("Inicio").apply {
            setIcon(R.drawable.ic_home)
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        binding.toolbar.setOnMenuItemClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("GO_HOME", true)
            }
            startActivity(intent)
            finish()
            true
        }
        binding.toolbar.setNavigationOnClickListener { 
            if (estaEnModoEdicion && !esModoCreacion) alternarModoEdicion(false) else onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun prepararModoCreacion() {
        binding.toolbar.title = "Nuevo Evento"
        binding.layoutVisualizacion.visibility = View.GONE
        binding.layoutEdicion.visibility = View.VISIBLE
        binding.btnEditar.visibility = View.GONE
        binding.btnEliminar.visibility = View.GONE
        binding.btnGuardar.visibility = View.VISIBLE
        estaEnModoEdicion = true

        tipoSeleccionado = TipoEvento.OTROS
        binding.autoCompleteTipo.setText(tipoSeleccionado.nombre, false)

        val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etFecha.setText(sdfFecha.format(fechaSeleccionada))

        val inicio = Calendar.getInstance().apply {
            time = fechaSeleccionada
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }
        val fin = inicio.clone() as Calendar
        fin.add(Calendar.HOUR_OF_DAY, 1)

        horaInicioEditada = inicio.time
        horaFinEditada = fin.time
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.etHoraInicio.setText(sdf.format(horaInicioEditada!!))
        binding.etHoraFin.setText(sdf.format(horaFinEditada!!))
    }

    private fun observeEvento() {
        viewModel.obtenerEvento(eventoId).observe(this) { evento ->
            evento?.let { 
                eventoActual = it
                if (!estaEnModoEdicion) mostrarDetalles(it)
            }
        }
    }

    private fun setupListeners() {
        binding.btnEliminar.setOnClickListener { viewModel.eliminarEvento(eventoId); finish() }
        binding.btnEditar.setOnClickListener { alternarModoEdicion(true) }
        binding.btnGuardar.setOnClickListener { guardarCambios() }
        binding.etFecha.setOnClickListener { mostrarDatePicker() }
        binding.etHoraInicio.setOnClickListener { mostrarTimePicker(true) }
        binding.etHoraFin.setOnClickListener { mostrarTimePicker(false) }
        binding.btnAdjuntarFoto.setOnClickListener { selectImageLauncher.launch(arrayOf("image/*")) }
        binding.btnAdjuntarAudio.setOnClickListener { selectAudioLauncher.launch(arrayOf("audio/*", "application/ogg")) }
        binding.btnGrabarAudio.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                iniciarOPararGrabacion()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        binding.btnReproducirAudio.setOnClickListener { reproducirAudio() }
        binding.btnVerFoto.setOnClickListener { verFotoPantallaCompleta() }
    }

    private fun verFotoPantallaCompleta() {
        val ruta = eventoActual?.fotoRuta ?: fotoRutaSeleccionada
        if (ruta != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(ruta), "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reproducirAudio() {
        val rutaAudio = eventoActual?.audioRuta ?: audioRutaSeleccionada ?: return
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
                binding.btnReproducirAudio.text = "🎤 Reproducir nota de voz"
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@DetalleEventoActivity, Uri.parse(rutaAudio))
                prepare(); start()
                setOnCompletionListener {
                    binding.btnReproducirAudio.text = "🎤 Reproducir nota de voz"
                    release(); mediaPlayer = null
                }
            }
            binding.btnReproducirAudio.text = "⏹ Detener reproducción"
        } catch (e: Exception) {
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarOPararGrabacion() { if (!estaGrabando) iniciarGrabacion() else detenerGrabacion() }

    private fun iniciarGrabacion() {
        archivoGrabacion = File(externalCacheDir, "grabacion_${System.currentTimeMillis()}.mp3")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(archivoGrabacion?.absolutePath)
            prepare(); start()
        }
        estaGrabando = true
        binding.btnGrabarAudio.text = "⏹ Detener Grabación"
        binding.tvEstadoGrabacion.visibility = View.VISIBLE
    }

    private fun detenerGrabacion() {
        try { mediaRecorder?.apply { stop(); release() } } catch (e: Exception) {}
        mediaRecorder = null; estaGrabando = false
        binding.btnGrabarAudio.text = "🎤 Grabar nota de voz"
        binding.tvEstadoGrabacion.visibility = View.GONE
        archivoGrabacion?.let {
            audioRutaSeleccionada = Uri.fromFile(it).toString()
            binding.tvRutaAudio.text = "Grabación lista"
            binding.tvRutaAudio.visibility = View.VISIBLE
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = fechaSeleccionada

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            fechaSeleccionada = newCal.time
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etFecha.setText(sdf.format(fechaSeleccionada))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun mostrarTimePicker(esInicio: Boolean) {
        val calendar = Calendar.getInstance()
        val current = if (esInicio) horaInicioEditada else horaFinEditada
        if (current != null) calendar.time = current

        TimePickerDialog(this, { _, hour, minute ->
            val newCal = Calendar.getInstance().apply {
                if (current != null) time = current
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            if (esInicio) {
                horaInicioEditada = newCal.time
                binding.etHoraInicio.setText(sdf.format(newCal.time))
            } else {
                horaFinEditada = newCal.time
                binding.etHoraFin.setText(sdf.format(newCal.time))
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun alternarModoEdicion(activar: Boolean) {
        estaEnModoEdicion = activar
        binding.layoutVisualizacion.visibility = if (activar) View.GONE else View.VISIBLE
        binding.layoutEdicion.visibility = if (activar) View.VISIBLE else View.GONE
        binding.btnEditar.visibility = if (activar) View.GONE else View.VISIBLE
        binding.btnGuardar.visibility = if (activar) View.VISIBLE else View.GONE
        
        if (activar) {
            prepararCamposEdicion()
            binding.toolbar.title = "Editar Evento"
        } else {
            binding.toolbar.title = "Detalle del Evento"
            if (estaGrabando) detenerGrabacion()
        }
    }

    private fun prepararCamposEdicion() {
        eventoActual?.let {
            binding.etTitulo.setText(it.titulo)
            binding.etDescripcion.setText(it.descripcion ?: "")
            binding.etUbicacion.setText(it.ubicacion ?: "")
            binding.cbPrioritario.isChecked = it.esPrioritario
            binding.cbAlarma.isChecked = it.tieneAlarma
            
            fechaSeleccionada = it.fecha
            val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etFecha.setText(sdfFecha.format(it.fecha))

            val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            horaInicioEditada = it.horaInicio; horaFinEditada = it.horaFin
            binding.etHoraInicio.setText(sdfHora.format(it.horaInicio))
            binding.etHoraFin.setText(sdfHora.format(it.horaFin))
            fotoRutaSeleccionada = it.fotoRuta; audioRutaSeleccionada = it.audioRuta
            binding.tvRutaFoto.visibility = if (it.fotoRuta != null) View.VISIBLE else View.GONE
            binding.tvRutaAudio.visibility = if (it.audioRuta != null) View.VISIBLE else View.GONE
            
            tipoSeleccionado = it.tipo
            binding.autoCompleteTipo.setText(it.tipo.nombre, false)
        }
    }

    private fun guardarCambios() {
        val nuevoTitulo = binding.etTitulo.text.toString()
        if (nuevoTitulo.isBlank()) return
        if (esModoCreacion) { crearEvento(nuevoTitulo); return }

        eventoActual?.let { evento ->
            val eventoEditado = evento.copy(
                titulo = nuevoTitulo,
                fecha = normalizarFecha(fechaSeleccionada),
                descripcion = binding.etDescripcion.text.toString().ifBlank { null },
                ubicacion = binding.etUbicacion.text.toString().ifBlank { null },
                esPrioritario = binding.cbPrioritario.isChecked,
                tieneAlarma = binding.cbAlarma.isChecked,
                horaInicio = combinarFechaYHora(fechaSeleccionada, horaInicioEditada ?: evento.horaInicio),
                horaFin = combinarFechaYHora(fechaSeleccionada, horaFinEditada ?: evento.horaFin),
                fotoRuta = fotoRutaSeleccionada,
                audioRuta = audioRutaSeleccionada,
                tipo = tipoSeleccionado,
                color = tipoSeleccionado.color
            )
            viewModel.actualizarEvento(eventoEditado)
            if (eventoEditado.tieneAlarma) programarAlarmaSistema(eventoEditado)
            alternarModoEdicion(false)
        }
    }

    private fun crearEvento(titulo: String) {
        val inicio = horaInicioEditada ?: return
        val fin = horaFinEditada ?: return
        val eventoNuevo = Evento(
            titulo = titulo,
            fecha = normalizarFecha(fechaSeleccionada),
            horaInicio = combinarFechaYHora(fechaSeleccionada, inicio),
            horaFin = combinarFechaYHora(fechaSeleccionada, fin),
            descripcion = binding.etDescripcion.text.toString().ifBlank { null },
            ubicacion = binding.etUbicacion.text.toString().ifBlank { null },
            esPrioritario = binding.cbPrioritario.isChecked,
            tieneAlarma = binding.cbAlarma.isChecked,
            fotoRuta = fotoRutaSeleccionada,
            audioRuta = audioRutaSeleccionada,
            tipo = tipoSeleccionado,
            color = tipoSeleccionado.color
        )
        viewModel.actualizarEvento(eventoNuevo)
        if (eventoNuevo.tieneAlarma) programarAlarmaSistema(eventoNuevo)
        finish()
    }

    private fun programarAlarmaSistema(evento: Evento) {
        val cal = Calendar.getInstance().apply { time = evento.horaInicio }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, "DiaCero: ${evento.titulo}")
            putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
        }
        try { startActivity(intent) } catch (e: Exception) {}
    }

    private fun normalizarFecha(base: Date): Date {
        return Calendar.getInstance().apply {
            time = base; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun combinarFechaYHora(baseFecha: Date, hora: Date): Date {
        val fechaCal = Calendar.getInstance().apply { time = baseFecha }
        val horaCal = Calendar.getInstance().apply { time = hora }
        fechaCal.set(Calendar.HOUR_OF_DAY, horaCal.get(Calendar.HOUR_OF_DAY))
        fechaCal.set(Calendar.MINUTE, horaCal.get(Calendar.MINUTE))
        return fechaCal.time
    }

    private fun mostrarDetalles(evento: Evento) {
        binding.tvTitulo.text = evento.titulo
        binding.tvDescripcion.text = evento.descripcion ?: ""
        binding.tvDescripcion.visibility = if (evento.descripcion.isNullOrEmpty()) View.GONE else View.VISIBLE
        val sdfFecha = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
        val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.tvFecha.text = sdfFecha.format(evento.fecha)
        binding.tvHora.text = "${sdfHora.format(evento.horaInicio)} - ${sdfHora.format(evento.horaFin)}"
        if (!evento.ubicacion.isNullOrEmpty()) {
            binding.tvUbicacion.text = evento.ubicacion; binding.layoutUbicacion.visibility = View.VISIBLE
        } else {
            binding.layoutUbicacion.visibility = View.GONE
        }
        binding.chipPrioritario.visibility = if (evento.esPrioritario) View.VISIBLE else View.GONE
        binding.chipAlarma.visibility = if (evento.tieneAlarma) View.VISIBLE else View.GONE
        
        // Mostrar tipo y color
        binding.chipTipo.text = evento.tipo.nombre
        binding.chipTipo.setChipBackgroundColorResource(android.R.color.transparent)
        binding.chipTipo.chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor(evento.tipo.color))
        binding.colorIndicator.setBackgroundColor(Color.parseColor(evento.tipo.color))
        binding.toolbar.setBackgroundColor(Color.parseColor(evento.tipo.color))

        if (evento.fotoRuta != null) {
            binding.ivFotoPreview.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(evento.fotoRuta)).into(binding.ivFotoPreview)
            binding.btnVerFoto.visibility = View.VISIBLE
        } else {
            binding.ivFotoPreview.visibility = View.GONE; binding.btnVerFoto.visibility = View.GONE
        }
        binding.btnReproducirAudio.visibility = if (evento.audioRuta != null) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
