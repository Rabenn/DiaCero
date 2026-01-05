package com.unaiIgorRuben.diacero.ui.calendario

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import kotlinx.coroutines.launch
import java.util.Date

class CalendarioViewModel(private val repository: EventoRepository) : ViewModel() {

    fun obtenerEventosPorRango(startOfDay: Date, endOfDay: Date): LiveData<List<Evento>> {
        return repository.obtenerEventosPorRango(startOfDay, endOfDay)
    }

    fun insertarEvento(evento: Evento) = viewModelScope.launch {
        repository.upsert(evento)
    }

    fun insertarVarios(eventos: List<Evento>) = viewModelScope.launch {
        repository.insertarVarios(eventos)
    }
}
