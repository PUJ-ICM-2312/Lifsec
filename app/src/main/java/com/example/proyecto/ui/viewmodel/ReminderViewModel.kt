package com.example.proyecto.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Anciano
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.Recordatorio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class ReminderViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val authViewModel = AuthViewModel()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _recordatorios = mutableStateListOf<Recordatorio>()
    val recordatorios: List<Recordatorio> get() = _recordatorios

    private val _cambio = mutableStateOf(false)
    val cambio: State<Boolean> = _cambio

    // Estado interno: ID del anciano actual
    private val _currentAnciano = MutableStateFlow<String?>(null)
    val currentAnciano: StateFlow<String?> = _currentAnciano.asStateFlow()

    fun setCurrentAnciano(id: String?) {
        _currentAnciano.value = id
    }

    fun cambio() {
        Log.i("RecordatorioViewModel", "se cambio")
        _cambio.value = !_cambio.value
    }

    fun relaunch() {
        Log.i("RecordatorioViewModel", "relaunch anciano: ${_currentAnciano.value}")
        if (authViewModel.currentEntity.value is Anciano) {
            auth.currentUser?.uid?.let { loadRecordatoriosForUser(it) }
        } else if (authViewModel.currentEntity.value is Cuidador) {
            _currentAnciano.value?.let { loadRecordatoriosForUser(it) }
        }
    }

    init {
        // Cargar recordatorios al iniciar si es posible
        if (authViewModel.currentEntity.value is Anciano) {
            auth.currentUser?.uid?.let { loadRecordatoriosForUser(it) }
        } else if (authViewModel.currentEntity.value is Cuidador) {
            _currentAnciano.value?.let { loadRecordatoriosForUser(it) }
        }
    }

    fun addRecordatorio(
        titulo: String,
        fecha: String,
        infoAdicional: String?
    ) {
        val userId = if (authViewModel.currentEntity.value is Anciano) {
            authViewModel.getCurrentUserID()
        } else {
            _currentAnciano.value
        }
        userId?.let { ancianoId ->
            val newRecordatorio = Recordatorio(
                ancianoID = ancianoId,
                titulo = titulo,
                fecha = fecha,
                infoAdicional = infoAdicional
            )
            viewModelScope.launch {
                uploadRecordatorioToFirebase(newRecordatorio)
                _recordatorios.add(newRecordatorio)
            }
        }
    }

    private suspend fun uploadRecordatorioToFirebase(recordatorio: Recordatorio) {
        try {
            val map = hashMapOf(
                "ancianoID" to recordatorio.ancianoID,
                "titulo" to recordatorio.titulo,
                "fecha" to recordatorio.fecha,
                "infoAdicional" to recordatorio.infoAdicional
            )
            firestore.collection("recordatorios")
                .document() // permite que Firestore genere ID automático
                .set(map)
                .await()
        } catch (e: Exception) {
            Log.e("RecordatorioViewModel", "Error subiendo recordatorio: ${e.message}")
        }
    }

    fun loadRecordatoriosForUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val querySnapshot = firestore
                    .collection("recordatorios")
                    .whereEqualTo("ancianoID", userId)
                    .get()
                    .await()
                val remoteList = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Recordatorio(
                            ancianoID = doc.getString("ancianoID") ?: "",
                            titulo = doc.getString("titulo") ?: "",
                            fecha = doc.getString("fecha") ?: "",
                            infoAdicional = doc.getString("infoAdicional")
                        )
                    } catch (e: Exception) {
                        Log.e("RecordatorioViewModel", "Error parseando recordatorio (ID=${doc.id}): ${e.message}")
                        null
                    }
                }.reversed()
                withContext(Dispatchers.Main) {
                    _recordatorios.clear()
                    _recordatorios.addAll(remoteList)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("RecordatorioViewModel", "Error cargando recordatorios para userId=$userId: ${e.message}")
                _isLoading.value = false
            }
        }
    }


    fun cargarAncianoActualDesdeJson(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "ancianoActual.json")
                if (!file.exists()) {
                    Log.w("RecordatorioViewModel", "Archivo ancianoActual.json no encontrado.")
                    return@launch
                }
                val contenido = file.readText()
                val json = JSONObject(contenido)
                val id = json.optString("currentAncianoId", null)
                withContext(Dispatchers.Main) {
                    _currentAnciano.value = id
                    Log.i("RecordatorioViewModel", "Anciano cargado desde JSON: $id")
                }
            } catch (e: Exception) {
                Log.e("RecordatorioViewModel", "Error leyendo JSON: ${e.message}", e)
            }
        }
    }
}
