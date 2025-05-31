package com.example.proyecto.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.proyecto.data.Recordatorio
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val _reminders = mutableStateListOf<Recordatorio>()
    val reminders: SnapshotStateList<Recordatorio> = _reminders

    private val JSON_FILENAME = "reminders_data.json"

    init {
        loadLocalReminders()
    }

    /** Añade un nuevo recordatorio y lo persiste inmediatamente */
    fun addReminder(ancianoID: String, titulo: String, fecha: String, infoAdicional: String? = null) {
        _reminders.add(Recordatorio(ancianoID, titulo, fecha, infoAdicional))
        saveLocalReminders()
    }

    /** Elimina un recordatorio por índice y actualiza el almacenamiento */
    fun removeReminder(index: Int) {
        if (index in _reminders.indices) {
            _reminders.removeAt(index)
            saveLocalReminders()
        }
    }

    /** Sobrescribe o recarga toda la lista de recordatorios en memoria */
    fun setReminders(list: List<Recordatorio>) {
        _reminders.clear()
        _reminders.addAll(list)
        saveLocalReminders()
    }

    /** Carga los recordatorios desde el JSON en storage interno */
    fun loadLocalReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val file = File(context.filesDir, JSON_FILENAME)
            val loadedList: List<Recordatorio> = try {
                if (file.exists()) {
                    context.openFileInput(JSON_FILENAME).use { fis ->
                        val json = fis.bufferedReader().use { it.readText() }
                        val type = object : TypeToken<List<Recordatorio>>() {}.type
                        Gson().fromJson(json, type) ?: emptyList()
                    }
                } else emptyList()
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error leyendo JSON: ${e.message}", e)
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _reminders.clear()
                _reminders.addAll(loadedList)
                Log.d("ReminderViewModel", "Recordatorios cargados: ${_reminders.size}")
            }
        }
    }

    /** Guarda los recordatorios actuales en un JSON en storage interno */
    fun saveLocalReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val gson = Gson()
            val jsonString = gson.toJson(_reminders.toList())

            try {
                context.openFileOutput(JSON_FILENAME, Context.MODE_PRIVATE).use { fos ->
                    fos.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                Log.d("ReminderViewModel", "Recordatorios guardados exitosamente.")
            } catch (e: IOException) {
                Log.e("ReminderViewModel", "Error guardando JSON: ${e.message}", e)
            }
        }
    }
}
