package com.unaiIgorRuben.diacero.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val icono: String = "⭐",
    val fechaInicio: Date = Date(),
    val diasCompletados: List<Long> = emptyList(),
    val bestStreak: Int = 0,
    val metaDias: Int = 30,
    val frecuencia: Int = 1
) {
    val currentStreak: Int
        get() = calculateCurrentStreak(diasCompletados, frecuencia)

    fun isCompletedToday(now: Long = System.currentTimeMillis()): Boolean {
        val today = normalizeToDay(now)
        return diasCompletados.any { normalizeToDay(it) == today }
    }

    companion object {
        fun normalizeToDay(timestamp: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

        fun calculateCurrentStreak(completedDays: List<Long>, frecuencia: Int, now: Long = System.currentTimeMillis()): Int {
            if (completedDays.isEmpty()) return 0
            val sortedDays = completedDays.map { normalizeToDay(it) }.distinct().sortedDescending()
            val today = normalizeToDay(now)
            val dayMs = 24L * 60L * 60L * 1000L
            val maxGapMs = frecuencia * dayMs

            // Si el tiempo desde la última vez supera la frecuencia, la racha ha terminado
            if (today - sortedDays.first() > maxGapMs) return 0

            var streak = 1
            for (i in 0 until sortedDays.size - 1) {
                if (sortedDays[i] - sortedDays[i+1] <= maxGapMs) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }
    }
}
