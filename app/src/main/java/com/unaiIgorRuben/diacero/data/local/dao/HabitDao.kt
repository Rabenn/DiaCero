package com.unaiIgorRuben.diacero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.unaiIgorRuben.diacero.data.local.entities.Habit

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY fechaInicio ASC")
    fun getAllHabits(): LiveData<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int
}
