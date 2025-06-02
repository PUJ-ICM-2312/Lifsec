package com.example.proyecto

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.LocatCareViewModel


class LocatCareViewModelFactory(
    private val context: Context,
    private val authViewModel: AuthViewModel,
    private val RepositorioUsuarios: RepositorioUsuarios
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatCareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatCareViewModel(context, authViewModel, RepositorioUsuarios) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}