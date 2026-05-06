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
    val icono: String,
    val fechaInicio: Date = Date(),
    val diasCompletados: List<Long> = emptyList(),
    val bestStreak: Int = 0
) {
    val currentStreak: Int
        get() = calculateCurrentStreak(diasCompletados)

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

        fun calculateCurrentStreak(completedDays: List<Long>, now: Long = System.currentTimeMillis()): Int {
            if (completedDays.isEmpty()) return 0
            val sortedDays = completedDays.map { normalizeToDay(it) }.distinct().sortedDescending()
            val today = normalizeToDay(now)
            val oneDayMs = 24L * 60L * 60L * 1000L

            val startsTodayOrYesterday = sortedDays.first() == today || sortedDays.first() == today - oneDayMs
            if (!startsTodayOrYesterday) return 0

            var streak = 1
            var expected = sortedDays.first() - oneDayMs
            for (i in 1 until sortedDays.size) {
                if (sortedDays[i] == expected) {
                    streak++
                    expected -= oneDayMs
                } else if (sortedDays[i] < expected) {
                    break
                }
            }
            return streak
        }
    }
}
