package com.unaiIgorRuben.diacero.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.unaiIgorRuben.diacero.data.local.dao.EventoDao
import com.unaiIgorRuben.diacero.data.local.entities.Evento

@Database(
    entities = [Evento::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(com.unaiIgorRuben.diacero.util.DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao

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
                .fallbackToDestructiveMigration() // Permite recrear la base de datos si el esquema cambia
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
