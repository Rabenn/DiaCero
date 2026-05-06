package com.unaiIgorRuben.diacero.ui.evento

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.util.EventoReceiver
import kotlinx.coroutines.launch

class DetalleEventoViewModel(
    application: Application,
    private val repository: EventoRepository
) : AndroidViewModel(application) {

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun obtenerEvento(id: Int): LiveData<Evento> {
        return repository.obtenerEventoPorId(id)
    }

    fun eliminarEvento(id: Int) = viewModelScope.launch {
        repository.eliminarPorId(id)
        cancelarNotificacion(id)
    }

    fun actualizarEvento(evento: Evento) = viewModelScope.launch {
        repository.upsert(evento)
        programarNotificacion(evento)
    }

    private fun programarNotificacion(evento: Evento) {
        val intent = Intent(getApplication(), EventoReceiver::class.java).apply {
            putExtra("titulo", evento.titulo)
            putExtra("id", evento.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            evento.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTime = evento.horaInicio.time

        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Si no tiene permiso de alarmas exactas, usamos la normal (menos precisa pero funciona)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    private fun cancelarNotificacion(eventoId: Int) {
        val intent = Intent(getApplication(), EventoReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            eventoId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
