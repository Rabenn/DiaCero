package com.unaiIgorRuben.diacero.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.unaiIgorRuben.diacero.data.local.dao.EventoDao
import com.unaiIgorRuben.diacero.data.local.dao.HabitDao
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.local.entities.Habit
import com.unaiIgorRuben.diacero.util.DateConverter
import com.unaiIgorRuben.diacero.util.LongListConverter
import com.unaiIgorRuben.diacero.util.TipoEventoConverter

@Database(
    entities = [Evento::class, Habit::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(DateConverter::class, TipoEventoConverter::class, LongListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diacero_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
