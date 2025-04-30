package com.example.proyecto.ui.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.proyecto.data.Actividad
import androidx.core.graphics.createBitmap

class ActivityViewModel : ViewModel() {

    private val _activities = mutableStateListOf<Actividad>()
    val activities: List<Actividad> get() = _activities

    fun addActivity(actividad: String, ubicacion: String, imagen: Bitmap) {
        val dummyImage = createBitmap(100, 100)
        _activities.add(Actividad(actividad, ubicacion, imagen, null))
    }

    fun removeActivity(index: Int) {
        if (index in _activities.indices) {
            _activities.removeAt(index)
        }
    }

    fun updateActivity(index: Int, updatedActivity: Actividad) {
        if (index in _activities.indices) {
            _activities[index] = updatedActivity
        }
    }
}
