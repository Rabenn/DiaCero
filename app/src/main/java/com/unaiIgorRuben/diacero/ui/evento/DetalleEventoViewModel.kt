package com.unaiIgorRuben.diacero.ui.evento

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
import java.util.concurrent.TimeUnit

class DetalleEventoViewModel(
    application: Application,
    private val repository: EventoRepository
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    fun obtenerEvento(id: Int): LiveData<Evento> {
        return repository.obtenerEventoPorId(id)
    }

    fun eliminarEvento(id: Int) = viewModelScope.launch {
        repository.eliminarPorId(id)
        workManager.cancelAllWorkByTag("evento_$id")
    }

    fun actualizarEvento(evento: Evento) = viewModelScope.launch {
        repository.upsert(evento)
        programarNotificacion(evento)
    }

    private fun programarNotificacion(evento: Evento) {
        // Cancelar cualquier notificación previa programada para este evento
        workManager.cancelAllWorkByTag("evento_${evento.id}")

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
