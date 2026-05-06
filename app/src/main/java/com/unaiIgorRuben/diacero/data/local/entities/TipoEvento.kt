package com.unaiIgorRuben.diacero.data.local.entities

enum class TipoEvento(val nombre: String, val color: String) {
    TRABAJO("Trabajo", "#4CAF50"),           // Verde
    TAREAS("Tareas", "#2196F3"),            // Azul
    SOCIAL("Social", "#FF9800"),            // Naranja
    FECHA_IMPORTANTE("Fecha Importante", "#F44336"), // Rojo
    OTROS("Otros", "#9E9E9E");              // Gris

    companion object {
        fun fromNombre(nombre: String): TipoEvento {
            return values().find { it.nombre == nombre } ?: OTROS
        }
    }
}
