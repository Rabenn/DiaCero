package com.unaiIgorRuben.diacero.data.local.entities

import androidx.annotation.StringRes
import com.unaiIgorRuben.diacero.R

enum class TipoEvento(@StringRes val resourceId: Int, val color: String) {
    TRABAJO(R.string.trabajo, "#4CAF50"),
    TAREAS(R.string.tareas, "#2196F3"),
    SOCIAL(R.string.social, "#FF9800"),
    FECHA_IMPORTANTE(R.string.fecha_importante, "#F44336"),
    OTROS(R.string.otros, "#9E9E9E");

    // Mantenemos esta propiedad por compatibilidad si es necesario en base de datos
    val nombre: String get() = name 
}
