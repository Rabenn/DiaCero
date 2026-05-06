package com.unaiIgorRuben.diacero.data.repository

import androidx.lifecycle.LiveData
import com.unaiIgorRuben.diacero.data.local.dao.HabitDao
import com.unaiIgorRuben.diacero.data.local.entities.Habit

class HabitRepository(
    private val habitDao: HabitDao
) {
    fun getAllHabits(): LiveData<List<Habit>> = habitDao.getAllHabits()

    suspend fun insert(habit: Habit): Long = habitDao.insert(habit)

    suspend fun update(habit: Habit) = habitDao.update(habit)

    suspend fun countHabits(): Int = habitDao.countHabits()
}
