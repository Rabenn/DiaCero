package com.unaiIgorRuben.diacero.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EventoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Evento"
        val eventoId = intent.getIntExtra("id", 0)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.mostrarNotificacionEvento(
            "¡Tu evento ha comenzado!",
            titulo,
            eventoId
        )
    }
}
