package com.unaiIgorRuben.diacero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Representa un evento en la base de datos.
 *
 * @param id Identificador único autoincremental.
 * @param titulo El nombre del evento.
 * @param fecha La fecha en la que ocurre el evento.
 * @param horaInicio La hora de inicio del evento.
 * @param horaFin La hora de finalización del evento.
 * @param descripcion Detalles adicionales sobre el evento.
 * @param ubicacion Lugar donde se realiza el evento (opcional).
 * @param esPrioritario True si debe activar el "Modo Enfoque".
 * @param color Etiqueta de color para organización visual.
 * @param fotoRuta Path al archivo de imagen guardado en el dispositivo (opcional).
 * @param audioRuta Path al archivo de audio guardado en el dispositivo (opcional).
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
    val color: String = "#2196F3", // Un color azul por defecto
    val fotoRuta: String? = null,
    val audioRuta: String? = null
)
