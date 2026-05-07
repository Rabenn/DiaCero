package com.unaiIgorRuben.diacero.ui.habitos

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.unaiIgorRuben.diacero.data.local.entities.Habit
import com.unaiIgorRuben.diacero.data.repository.HabitRepository
import kotlinx.coroutines.launch

class HabitosViewModel(
    app: android.app.Application,
    private val repository: HabitRepository
) : AndroidViewModel(app) {

    val habits: LiveData<List<Habit>> = repository.getAllHabits()

    private val _lastCompletedHabitId = MutableLiveData<Int?>()
    val lastCompletedHabitId: LiveData<Int?> = _lastCompletedHabitId

    fun markHabitDoneToday(habit: Habit) {
        if (habit.isCompletedToday()) return

        val today = Habit.normalizeToDay(System.currentTimeMillis())
        val updatedDays = (habit.diasCompletados + today).distinct().sorted()
        // Corregido: Pasamos la frecuencia del hábito para calcular la racha
        val currentStreak = Habit.calculateCurrentStreak(updatedDays, habit.frecuencia)
        val updatedHabit = habit.copy(
            diasCompletados = updatedDays,
            bestStreak = maxOf(habit.bestStreak, currentStreak)
        )
        viewModelScope.launch {
            repository.update(updatedHabit)
            _lastCompletedHabitId.value = updatedHabit.id
        }
    }

    fun addHabit(nombre: String, meta: Int, frecuencia: Int) {
        viewModelScope.launch {
            repository.insert(Habit(
                nombre = nombre,
                metaDias = meta,
                frecuencia = frecuencia
            ))
        }
    }

    fun resetStreak(habit: Habit) {
        viewModelScope.launch {
            val resetHabit = habit.copy(
                diasCompletados = emptyList(),
                fechaInicio = java.util.Date()
            )
            repository.update(resetHabit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.delete(habit)
        }
    }

    fun consumeLastCompletedSignal() {
        _lastCompletedHabitId.value = null
    }
}
