package com.unaiIgorRuben.diacero.ui.calendario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.util.EventoWorker
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class CalendarioViewModel(
    application: Application,
    private val repository: EventoRepository
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    fun obtenerEventosPorRango(startOfDay: Date, endOfDay: Date): LiveData<List<Evento>> {
        return repository.obtenerEventosPorRango(startOfDay, endOfDay)
    }

    fun obtenerTodosLosEventos(): LiveData<List<Evento>> {
        return repository.obtenerTodosLosEventos()
    }

    fun buscarEventosPorNombre(query: String): LiveData<List<Evento>> {
        return repository.buscarEventosPorNombre(query)
    }

    fun insertarEvento(evento: Evento) = viewModelScope.launch {
        repository.upsert(evento)
        programarNotificacion(evento)
    }

    fun insertarVarios(eventos: List<Evento>) = viewModelScope.launch {
        repository.insertarVarios(eventos)
        // Programamos notificaciones para todos los eventos nuevos
        eventos.forEach { programarNotificacion(it) }
    }

    private fun programarNotificacion(evento: Evento) {
        val delay = evento.horaInicio.time - System.currentTimeMillis()
        if (delay > 0) {
            val data = Data.Builder()
                .putString("titulo", evento.titulo)
                .putInt("id", evento.id)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<EventoWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("evento_${evento.id}")
                .build()

            workManager.enqueue(workRequest)
        }
    }
}
