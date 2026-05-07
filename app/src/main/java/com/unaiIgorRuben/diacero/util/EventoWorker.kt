package com.unaiIgorRuben.diacero.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.unaiIgorRuben.diacero.R

class EventoWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Evento"
        val eventoId = inputData.getInt("id", 0)

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.mostrarNotificacionEvento(
            applicationContext.getString(R.string.tu_evento_ha_comenzado),
            titulo,
            eventoId
        )

        return Result.success()
    }
}
