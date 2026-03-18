package com.unaiIgorRuben.diacero.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class EventoWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Evento"
        val eventoId = inputData.getInt("id", 0)

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.mostrarNotificacionEvento(
            "¡Tu evento ha comenzado!",
            titulo,
            eventoId
        )

        return Result.success()
    }
}
