package com.unaiIgorRuben.diacero.ui.calendario

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import com.unaiIgorRuben.diacero.util.EventoReceiver
import kotlinx.coroutines.launch
import java.util.Date

class CalendarioViewModel(
    application: Application,
    private val repository: EventoRepository
) : AndroidViewModel(application) {

    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun obtenerEventosPorRango(startOfDay: Date, endOfDay: Date): LiveData<List<Evento>> {
        return repository.obtenerEventosPorRango(startOfDay, endOfDay)
    }

    fun obtenerTodosLosEventos(): LiveData<List<Evento>> {
        return repository.obtenerTodosLosEventos()
    }

    fun buscarEventos(query: String, tipo: TipoEvento? = null): LiveData<List<Evento>> {
        return repository.buscarEventos(query, tipo)
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
}
