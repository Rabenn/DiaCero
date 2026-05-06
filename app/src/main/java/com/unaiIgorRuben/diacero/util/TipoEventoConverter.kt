package com.unaiIgorRuben.diacero.util

import androidx.room.TypeConverter
import com.unaiIgorRuben.diacero.data.local.entities.TipoEvento

class TipoEventoConverter {
    @TypeConverter
    fun fromTipoEvento(tipo: TipoEvento): String {
        return tipo.name
    }

    @TypeConverter
    fun toTipoEvento(nombre: String?): TipoEvento {
        return if (nombre == null) TipoEvento.OTROS else TipoEvento.valueOf(nombre)
    }
}
