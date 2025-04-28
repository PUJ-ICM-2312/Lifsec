package com.example.proyecto

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.ui.LocatCareViewModel
//Constructor que toma el parametro de contexto
class LocatCareViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocatCareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocatCareViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}