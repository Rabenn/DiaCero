package com.unaiIgorRuben.diacero.ui.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unaiIgorRuben.diacero.data.repository.EventoRepository

/**
 * Factory para crear instancias de CalendarioViewModel con un EventoRepository como dependencia.
 */
class CalendarioViewModelFactory(private val repository: EventoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
