package com.unaiIgorRuben.diacero.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unaiIgorRuben.diacero.data.local.entities.Habit

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY fechaInicio ASC")
    fun getAllHabits(): LiveData<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int
}
