package com.unaiIgorRuben.diacero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento
import java.util.Date

/**
 * DAO (Data Access Object) para la entidad Evento.
 * Define cómo se accede a los datos de la tabla 'eventos'.
 */
@Dao
interface EventoDao {

    /**
     * Inserta un nuevo evento. Si ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(evento: Evento)

    /**
     * Inserta una lista de eventos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(eventos: List<Evento>)

    /**
     * Elimina un evento de la base de datos.
     */
    @Delete
    suspend fun delete(evento: Evento)

    /**
     * Obtiene todos los eventos ordenados por fecha.
     * LiveData notificará a los observadores automáticamente cuando los datos cambien.
     */
    @Query("SELECT * FROM eventos ORDER BY fecha, horaInicio ASC")
    fun obtenerTodosLosEventos(): LiveData<List<Evento>>

    /**
     * Obtiene todos los eventos dentro de un rango de fechas (ej: un día completo).
     */
    @Query("SELECT * FROM eventos WHERE fecha >= :startOfDay AND fecha < :endOfDay ORDER BY horaInicio ASC")
    fun obtenerEventosPorRango(startOfDay: Date, endOfDay: Date): LiveData<List<Evento>>

    /**
     * Obtiene un evento por su ID.
     */
    @Query("SELECT * FROM eventos WHERE id = :id")
    fun obtenerEventoPorId(id: Int): LiveData<Evento>

    /**
     * Elimina un evento por su ID.
     */
    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    /**
     * Busca eventos por título y opcionalmente por tipo.
     */
    @Query("SELECT * FROM eventos WHERE titulo LIKE '%' || :query || '%' AND (:tipo IS NULL OR tipo = :tipo) ORDER BY fecha, horaInicio ASC")
    fun buscarEventos(query: String, tipo: TipoEvento?): LiveData<List<Evento>>

    /**
     * Elimina todos los eventos de la base de datos.
     */
    @Query("DELETE FROM eventos")
    suspend fun eliminarTodosLosEventos()
}
