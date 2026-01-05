package com.unaiIgorRuben.diacero.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room necesita saber cómo convertir tipos complejos a tipos primitivos que pueda almacenar.
 * Esta clase le dice cómo convertir un objeto Date a un Long (milisegundos) y viceversa.
 */
class DateConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
