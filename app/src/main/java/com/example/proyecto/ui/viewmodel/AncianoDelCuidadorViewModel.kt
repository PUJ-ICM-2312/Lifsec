package com.example.proyecto.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Anciano
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AncianoDelCuidadorViewModel: ViewModel() {

    //Instancia de Firestore
    private val firestore: FirebaseFirestore = Firebase.firestore

    //Lista interna de IDs de ancianos
    private var ancianoIds: List<String> = emptyList()

    //Estado interno: lista de objetos Anciano cargados
    private val _ancianos = MutableStateFlow<List<Anciano>>(emptyList())
    val ancianos: StateFlow<List<Anciano>> = _ancianos.asStateFlow()

    //Flag de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _emailIsLoading = MutableStateFlow(false)
    val emailIsLoading: StateFlow<Boolean> = _emailIsLoading

    var currentAnciano: Anciano? = null

    suspend fun isEmailDeAnciano(email: String): Boolean {
        _emailIsLoading.value = true
        return try {
            val snapshot = Firebase.firestore
                .collection("ancianos")
                .whereEqualTo("email", email)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            Log.e("AncianoViewModel", "Error verificando email: ${e.message}")
            false
        } finally {
            _emailIsLoading.value = false
        }
    }

    suspend fun obtenerIdAncianoPorEmail(email: String): String? {
        return try {

            if(!isEmailDeAnciano(email)) {
                Log.e("AncianoViewModel", "NO EXISTE ESE CORREO EN UN ANCIANO")
                return null
            }

            val snapshot = firestore.collection("ancianos")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                document.id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AncianoViewModel", "Error al buscar anciano por email: ${e.message}")
            null
        }
    }

    /**
     * 5) Asigna una nueva lista de IDs de ancianos y dispara la carga desde Firestore.
     */
    fun setAncianoIds(ids: List<String>) {
        if (ancianoIds == ids) return            // Si la lista no cambió, no recargamos
        ancianoIds = ids
        loadAncianosByIds(ids)
    }

    /**
     * 6) Consulta Firestore por cada ID en la lista y construye la lista de Anciano.
     */
    private fun loadAncianosByIds(ids: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Para cada ID, lanzamos una coroutine async que obtiene el documento
                val deferredList = ids.map { id ->
                    async {
                        val document = firestore.collection("ancianos")
                            .document(id)
                            .get()
                            .await()
                        if (document.exists()) {
                            document.toObject(Anciano::class.java)
                        } else {
                            null
                        }
                    }
                }
                // AwaitAll devuelve List<Anciano?>; filtramos los nulos
                val fetchedAncianos = deferredList.awaitAll().filterNotNull()
                _ancianos.value = fetchedAncianos
            } catch (e: Exception) {
                // Si ocurre algún error, limpiamos la lista
                _ancianos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}