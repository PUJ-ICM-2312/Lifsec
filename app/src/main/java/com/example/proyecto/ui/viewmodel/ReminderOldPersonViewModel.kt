package com.example.proyecto.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.proyecto.data.Recordatorio
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class ReminderViewModel : ViewModel() {

    private val _reminders = mutableStateListOf<Recordatorio>()
    val reminders: SnapshotStateList<Recordatorio> = _reminders

    fun addReminder(titulo: String, fecha: String, infoAdicional: String? = null) {
        _reminders.add(Recordatorio(titulo, fecha, infoAdicional))
    }
}
