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
    version = 1,
    exportSchema = false // No exportar el esquema para este proyecto
)
@TypeConverters(com.unaiIgorRuben.diacero.util.DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao

    companion object {
        // La anotación @Volatile asegura que la variable INSTANCE sea siempre
        // visible para todos los hilos y sus cambios sean inmediatos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia no es nula, la devolvemos. Si es nula, creamos la BD.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diacero_database" // Nombre del archivo de la BD
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
