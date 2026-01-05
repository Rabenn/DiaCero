package com.unaiIgorRuben.diacero.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * UTILIDADES PARA TRABAJAR CON FECHAS
 *
 * Centraliza todo el formateo de fechas para mantener consistencia
 * en toda la aplicación
 */
object DateUtils {

    // Formatos predefinidos
    private val formatoCompleto = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    private val formatoFecha = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
    private val formatoFechaCorta = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    private val formatoHora = SimpleDateFormat("HH:mm", Locale("es", "ES"))
    private val formatoHora12 = SimpleDateFormat("hh:mm a", Locale("es", "ES"))
    private val formatoDiaSemana = SimpleDateFormat("EEEE", Locale("es", "ES"))
    private val formatoMesAnio = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    /**
     * Convierte timestamp a fecha legible
     * Ejemplo: "Lunes, 6 de enero de 2025"
     */
    fun formatearFechaCompleta(timestamp: Long): String {
        return formatoCompleto.format(Date(timestamp))
    }

    /**
     * Formato: "6 de enero"
     */
    fun formatearFecha(timestamp: Long): String {
        return formatoFecha.format(Date(timestamp))
    }

    /**
     * Formato: "06/01/2025"
     */
    fun formatearFechaCorta(timestamp: Long): String {
        return formatoFechaCorta.format(Date(timestamp))
    }

    /**
     * Formato: "14:30"
     */
    fun formatearHora(timestamp: Long): String {
        return formatoHora.format(Date(timestamp))
    }

    /**
     * Formato: "02:30 PM"
     */
    fun formatearHora12(timestamp: Long): String {
        return formatoHora12.format(Date(timestamp))
    }

    /**
     * Formato: "Lunes"
     */
    fun obtenerDiaSemana(timestamp: Long): String {
        return formatoDiaSemana.format(Date(timestamp)).capitalize(Locale("es", "ES"))
    }

    /**
     * Formato: "Enero 2025"
     */
    fun formatearMesAnio(timestamp: Long): String {
        return formatoMesAnio.format(Date(timestamp)).capitalize(Locale("es", "ES"))
    }

    /**
     * Rango de horas: "10:00 - 11:30"
     */
    fun formatearRangoHoras(inicio: Long, fin: Long): String {
        return "${formatearHora(inicio)} - ${formatearHora(fin)}"
    }

    /**
     * Obtiene el timestamp del inicio del día (00:00:00)
     */
    fun obtenerInicioDia(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Obtiene el timestamp del fin del día (23:59:59)
     */
    fun obtenerFinDia(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Verifica si una fecha es hoy
     */
    fun esHoy(timestamp: Long): Boolean {
        val hoy = obtenerInicioDia(System.currentTimeMillis())
        val fecha = obtenerInicioDia(timestamp)
        return hoy == fecha
    }

    /**
     * Verifica si una fecha es mañana
     */
    fun esManana(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val manana = obtenerInicioDia(calendar.timeInMillis)
        val fecha = obtenerInicioDia(timestamp)
        return manana == fecha
    }

    /**
     * Obtiene un texto relativo: "Hoy", "Mañana" o la fecha
     */
    fun obtenerTextoRelativo(timestamp: Long): String {
        return when {
            esHoy(timestamp) -> "Hoy"
            esManana(timestamp) -> "Mañana"
            else -> formatearFecha(timestamp)
        }
    }

    /**
     * Calcula la diferencia en días entre dos fechas
     */
    fun diferenciaDias(inicio: Long, fin: Long): Long {
        val diff = fin - inicio
        return diff / (1000 * 60 * 60 * 24)
    }

    /**
     * Obtiene el primer día del mes
     */
    fun obtenerPrimerDiaMes(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return obtenerInicioDia(calendar.timeInMillis)
    }

    /**
     * Obtiene el último día del mes
     */
    fun obtenerUltimoDiaMes(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return obtenerFinDia(calendar.timeInMillis)
    }
}