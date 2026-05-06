package com.unaiIgorRuben.diacero.ui.habitos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unaiIgorRuben.diacero.data.repository.HabitRepository

class HabitosViewModelFactory(
    private val app: Application,
    private val repository: HabitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitosViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
