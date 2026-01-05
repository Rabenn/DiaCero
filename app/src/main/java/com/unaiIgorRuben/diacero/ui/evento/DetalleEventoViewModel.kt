package com.unaiIgorRuben.diacero.ui.evento

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unaiIgorRuben.diacero.data.local.entities.Evento
import com.unaiIgorRuben.diacero.data.repository.EventoRepository
import kotlinx.coroutines.launch

class DetalleEventoViewModel(private val repository: EventoRepository) : ViewModel() {

    fun obtenerEvento(id: Int): LiveData<Evento> {
        return repository.obtenerEventoPorId(id)
    }

    fun eliminarEvento(id: Int) = viewModelScope.launch {
        repository.eliminarPorId(id)
    }
}
