package com.unaiIgorRuben.diacero.ui.evento

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unaiIgorRuben.diacero.data.repository.EventoRepository

class DetalleEventoViewModelFactory(
    private val application: Application,
    private val repository: EventoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleEventoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetalleEventoViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
