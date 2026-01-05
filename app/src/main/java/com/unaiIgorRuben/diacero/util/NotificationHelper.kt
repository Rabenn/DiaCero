package com.unaiIgorRuben.diacero.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * HELPER PARA EL MODO ENFOQUE
 * Gestiona el "No Molestar" del sistema durante eventos prioritarios
 *
 * ⚠️ IMPORTANTE: Este es uno de los puntos más delicados del proyecto
 * Requiere permisos especiales del sistema
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * VERIFICA SI LA APP TIENE PERMISO PARA MODIFICAR "NO MOLESTAR"
     *
     * ¿Por qué necesitamos este permiso?
     * - El "No Molestar" es una configuración crítica del sistema
     * - Android protege esto para evitar que apps maliciosas silencien el teléfono
     * - El usuario debe conceder este permiso MANUALMENTE desde Ajustes
     */
    fun tienePermisoNoMolestar(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            // En versiones antiguas (<Android 6) no existe este permiso
            true
        }
    }

    /**
     * ABRE LA CONFIGURACIÓN PARA QUE EL USUARIO CONCEDA EL PERMISO
     *
     * Flujo recomendado:
     * 1. Detectar que no hay permiso
     * 2. Mostrar un diálogo explicando POR QUÉ lo necesitas
     * 3. Llamar a esta función para abrir Ajustes
     */
    fun solicitarPermisoNoMolestar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * ACTIVA EL MODO "NO MOLESTAR"
     *
     * @param modo: Tipo de filtro que quieres aplicar
     * - INTERRUPTION_FILTER_NONE: Silencio total (ni alarmas)
     * - INTERRUPTION_FILTER_PRIORITY: Solo notificaciones prioritarias
     * - INTERRUPTION_FILTER_ALARMS: Solo alarmas
     *
     * @return true si se activó correctamente, false si no hay permiso
     */
    fun activarModoEnfoque(
        modo: Int = NotificationManager.INTERRUPTION_FILTER_PRIORITY
    ): Boolean {
        if (!tienePermisoNoMolestar()) {
            return false
        }

        try {
            // Guardar el modo anterior para restaurarlo después
            guardarModoAnterior()

            // Activar el nuevo modo
            notificationManager.setInterruptionFilter(modo)

            return true
        } catch (e: SecurityException) {
            // Puede ocurrir si el permiso fue revocado mientras la app estaba abierta
            e.printStackTrace()
            return false
        }
    }

    /**
     * DESACTIVA EL MODO "NO MOLESTAR"
     * Restaura el estado normal del teléfono
     */
    fun desactivarModoEnfoque(): Boolean {
        if (!tienePermisoNoMolestar()) {
            return false
        }

        try {
            // Obtener el modo anterior guardado
            val modoAnterior = obtenerModoAnterior()

            // Si había un modo guardado, restaurarlo
            // Si no, volver al modo normal
            val modoRestaurar = modoAnterior
                ?: NotificationManager.INTERRUPTION_FILTER_ALL

            notificationManager.setInterruptionFilter(modoRestaurar)

            return true
        } catch (e: SecurityException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * OBTIENE EL ESTADO ACTUAL DEL "NO MOLESTAR"
     * Útil para mostrar en la UI
     */
    fun obtenerEstadoActual(): String {
        return when (notificationManager.currentInterruptionFilter) {
            NotificationManager.INTERRUPTION_FILTER_NONE -> "Silencio total"
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "Solo prioritarias"
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> "Solo alarmas"
            NotificationManager.INTERRUPTION_FILTER_ALL -> "Normal"
            else -> "Desconocido"
        }
    }

    /**
     * VERIFICA SI EL MODO ENFOQUE ESTÁ ACTIVO
     */
    fun estaEnModoEnfoque(): Boolean {
        val filtroActual = notificationManager.currentInterruptionFilter
        return filtroActual != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    // ===== FUNCIONES PRIVADAS (Gestión interna) =====

    private fun guardarModoAnterior() {
        val prefs = context.getSharedPreferences("modo_enfoque", Context.MODE_PRIVATE)
        val modoActual = notificationManager.currentInterruptionFilter
        prefs.edit().putInt("modo_anterior", modoActual).apply()
    }

    private fun obtenerModoAnterior(): Int? {
        val prefs = context.getSharedPreferences("modo_enfoque", Context.MODE_PRIVATE)
        return if (prefs.contains("modo_anterior")) {
            prefs.getInt("modo_anterior", NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            null
        }
    }
}