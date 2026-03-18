package com.unaiIgorRuben.diacero.ui.evento

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.unaiIgorRuben.diacero.data.local.database.AppDatabase
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.databinding.ActivityDetalleEventoBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEventoBinding
    private var eventoId: Int = -1
    private var eventoActual: Evento? = null
    private var estaEnModoEdicion = false

    private var horaInicioEditada: Date? = null
    private var horaFinEditada: Date? = null
    private var fotoRutaSeleccionada: String? = null
    private var audioRutaSeleccionada: String? = null

    // Grabación de audio
    private var mediaRecorder: MediaRecorder? = null
    private var estaGrabando = false
    private var archivoGrabacion: File? = null

    // Reproducción de audio
    private var mediaPlayer: MediaPlayer? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            iniciarOPararGrabacion()
        } else {
            Toast.makeText(this, "Permiso de audio denegado", Toast.LENGTH_SHORT).show()
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
        if (eventoId == -1) {
            finish()
            return
        }

        setupToolbar()
        observeEvento()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { 
            if (estaEnModoEdicion) {
                alternarModoEdicion(false)
            } else {
                onBackPressed()
            }
        }
    }

    private fun observeEvento() {
        viewModel.obtenerEvento(eventoId).observe(this) { evento ->
            evento?.let { 
                eventoActual = it
                if (!estaEnModoEdicion) {
                    mostrarDetalles(it)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnEliminar.setOnClickListener {
            viewModel.eliminarEvento(eventoId)
            finish()
        }

        binding.btnEditar.setOnClickListener {
            alternarModoEdicion(true)
        }

        binding.btnGuardar.setOnClickListener {
            guardarCambios()
        }

        binding.etHoraInicio.setOnClickListener { mostrarTimePicker(true) }
        binding.etHoraFin.setOnClickListener { mostrarTimePicker(false) }

        binding.btnAdjuntarFoto.setOnClickListener {
            selectImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnAdjuntarAudio.setOnClickListener {
            selectAudioLauncher.launch(arrayOf("audio/*", "application/ogg"))
        }

        binding.btnGrabarAudio.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                iniciarOPararGrabacion()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnReproducirAudio.setOnClickListener {
            reproducirAudio()
        }

        binding.btnVerFoto.setOnClickListener {
            verFotoPantallaCompleta()
        }
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
        val rutaAudio = eventoActual?.audioRuta ?: audioRutaSeleccionada
        if (rutaAudio == null) {
            Toast.makeText(this, "No hay audio para reproducir", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                binding.btnReproducirAudio.text = "🎤 Reproducir nota de voz"
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@DetalleEventoActivity, Uri.parse(rutaAudio))
                prepare()
                start()
                setOnCompletionListener {
                    binding.btnReproducirAudio.text = "🎤 Reproducir nota de voz"
                    release()
                    mediaPlayer = null
                }
            }
            binding.btnReproducirAudio.text = "⏹ Detener reproducción"
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarOPararGrabacion() {
        if (!estaGrabando) {
            iniciarGrabacion()
        } else {
            detenerGrabacion()
        }
    }

    private fun iniciarGrabacion() {
        archivoGrabacion = File(externalCacheDir, "grabacion_${System.currentTimeMillis()}.mp3")
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(archivoGrabacion?.absolutePath)
            prepare()
            start()
        }
        
        estaGrabando = true
        binding.btnGrabarAudio.text = "⏹ Detener Grabación"
        binding.tvEstadoGrabacion.visibility = View.VISIBLE
    }

    private fun detenerGrabacion() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        estaGrabando = false
        
        binding.btnGrabarAudio.text = "🎤 Grabar nota de voz"
        binding.tvEstadoGrabacion.visibility = View.GONE
        
        archivoGrabacion?.let {
            audioRutaSeleccionada = Uri.fromFile(it).toString()
            binding.tvRutaAudio.text = "Grabación lista"
            binding.tvRutaAudio.visibility = View.VISIBLE
            Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarTimePicker(esInicio: Boolean) {
        val currentDateTime = if (esInicio) horaInicioEditada else horaFinEditada
        val calendar = Calendar.getInstance()
        if (currentDateTime != null) calendar.time = currentDateTime

        TimePickerDialog(this, { _, hourOfDay, minute ->
            val newCalendar = Calendar.getInstance()
            if (currentDateTime != null) newCalendar.time = currentDateTime
            newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newCalendar.set(Calendar.MINUTE, minute)
            
            val newDate = newCalendar.time
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            if (esInicio) {
                horaInicioEditada = newDate
                binding.etHoraInicio.setText(sdf.format(newDate))
            } else {
                horaFinEditada = newDate
                binding.etHoraFin.setText(sdf.format(newDate))
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
            
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            horaInicioEditada = it.horaInicio
            horaFinEditada = it.horaFin
            binding.etHoraInicio.setText(sdf.format(it.horaInicio))
            binding.etHoraFin.setText(sdf.format(it.horaFin))

            fotoRutaSeleccionada = it.fotoRuta
            audioRutaSeleccionada = it.audioRuta
            
            binding.tvRutaFoto.visibility = if (it.fotoRuta != null) View.VISIBLE else View.GONE
            binding.tvRutaFoto.text = if (it.fotoRuta != null) "Foto adjunta" else ""
            binding.tvRutaAudio.visibility = if (it.audioRuta != null) View.VISIBLE else View.GONE
            binding.tvRutaAudio.text = if (it.audioRuta != null) "Audio adjunto" else ""
        }
    }

    private fun guardarCambios() {
        val nuevoTitulo = binding.etTitulo.text.toString()
        if (nuevoTitulo.isBlank()) {
            Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        eventoActual?.let { evento ->
            val eventoEditado = evento.copy(
                titulo = nuevoTitulo,
                descripcion = binding.etDescripcion.text.toString().ifBlank { null },
                ubicacion = binding.etUbicacion.text.toString().ifBlank { null },
                esPrioritario = binding.cbPrioritario.isChecked,
                horaInicio = horaInicioEditada ?: evento.horaInicio,
                horaFin = horaFinEditada ?: evento.horaFin,
                fotoRuta = fotoRutaSeleccionada,
                audioRuta = audioRutaSeleccionada
            )
            viewModel.actualizarEvento(eventoEditado)
            Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show()
            alternarModoEdicion(false)
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
        
        // Mostrar multimedia
        if (evento.fotoRuta != null) {
            binding.ivFotoPreview.visibility = View.VISIBLE
            Glide.with(this).load(Uri.parse(evento.fotoRuta)).into(binding.ivFotoPreview)
            binding.btnVerFoto.visibility = View.VISIBLE
        } else {
            binding.ivFotoPreview.visibility = View.GONE
            binding.btnVerFoto.visibility = View.GONE
        }

        binding.btnReproducirAudio.visibility = if (evento.audioRuta != null) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        if (estaGrabando) {
            detenerGrabacion()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
