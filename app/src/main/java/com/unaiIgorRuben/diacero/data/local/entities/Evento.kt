package com.unaiIgorRuben.diacero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Representa un evento en la base de datos.
 */
@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val fecha: Date,
    val horaInicio: Date,
    val horaFin: Date,
    val descripcion: String? = null,
    val ubicacion: String? = null,
    val esPrioritario: Boolean = false,
    val tieneAlarma: Boolean = false, // Nueva propiedad para la alarma
    val color: String = "#2196F3",
    val fotoRuta: String? = null,
    val audioRuta: String? = null
)
