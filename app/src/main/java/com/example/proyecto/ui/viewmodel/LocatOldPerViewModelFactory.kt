package com.example.proyecto.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.data.RepositorioUsuarios

class LocatOldPerViewModelFactory(
    private val context: Context,
    private val authViewModel: AuthViewModel,
    private val repositorioUsuarios: RepositorioUsuarios
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatOldPerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatOldPerViewModel(context, authViewModel, repositorioUsuarios) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
