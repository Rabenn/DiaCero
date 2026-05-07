package com.unaiIgorRuben.diacero.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unaiIgorRuben.diacero.R

class EventoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Evento"
        val eventoId = intent.getIntExtra("id", 0)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.mostrarNotificacionEvento(
            context.getString(R.string.tu_evento_ha_comenzado),
            titulo,
            eventoId
        )
    }
}
