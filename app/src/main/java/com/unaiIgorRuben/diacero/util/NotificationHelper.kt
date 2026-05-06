package com.unaiIgorRuben.diacero.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.unaiIgorRuben.diacero.MainActivity
import com.unaiIgorRuben.diacero.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "eventos_channel"
        const val CHANNEL_NAME = "Notificaciones de Eventos"
        private const val PREFS_NAME = "ajustes_prefs"
        private const val KEY_NOTIFICACIONES = "notificaciones_enabled"
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones que avisan cuando comienza un evento"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacionEvento(titulo: String, mensaje: String, eventoId: Int) {
        // VERIFICACIÓN DE AJUSTES: Si el usuario las ha desactivado, no hacemos nada
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_NOTIFICACIONES, true)
        if (!enabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            eventoId, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sun) 
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventoId, notification)
    }
}
