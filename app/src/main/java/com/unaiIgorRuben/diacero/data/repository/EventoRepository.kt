package com.unaiIgorRuben.diacero.data.repository

import androidx.lifecycle.LiveData
import com.unaiIgorRuben.diacero.data.local.dao.EventoDao
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento
import java.util.Date

/**
 * Repositorio para gestionar los datos de los eventos.
 * Es la única fuente de verdad para los ViewModels.
 * Abstrae el origen de los datos (en este caso, el DAO de Room).
 */
class EventoRepository(private val eventoDao: EventoDao) {

    // Obtiene todos los eventos como LiveData. La UI se actualizará automáticamente.
    val todosLosEventos: LiveData<List<Evento>> = eventoDao.obtenerTodosLosEventos()

    // Obtiene eventos para un rango de fechas (normalmente un día).
    fun obtenerEventosPorRango(startOfDay: Date, endOfDay: Date): LiveData<List<Evento>> {
        return eventoDao.obtenerEventosPorRango(startOfDay, endOfDay)
    }

    fun obtenerEventoPorId(id: Int): LiveData<Evento> {
        return eventoDao.obtenerEventoPorId(id)
    }

    fun obtenerTodosLosEventos(): LiveData<List<Evento>> {
        return eventoDao.obtenerTodosLosEventos()
    }

    fun buscarEventos(query: String, tipo: TipoEvento? = null): LiveData<List<Evento>> {
        return eventoDao.buscarEventos(query, tipo)
    }

    /**
     * Inserta o actualiza un evento en un hilo secundario.
     */
    suspend fun upsert(evento: Evento) {
        eventoDao.upsert(evento)
    }

    /**
     * Inserta una lista de eventos.
     */
    suspend fun insertarVarios(eventos: List<Evento>) {
        eventoDao.upsertAll(eventos)
    }

    /**
     * Elimina un evento en un hilo secundario.
     */
    suspend fun delete(evento: Evento) {
        eventoDao.delete(evento)
    }

    suspend fun eliminarPorId(id: Int) {
        eventoDao.eliminarPorId(id)
    }

    /**
     * Elimina todos los eventos de la base de datos.
     */
    suspend fun eliminarTodosLosEventos() {
        eventoDao.eliminarTodosLosEventos()
    }
}
