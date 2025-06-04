package com.example.proyecto.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Actividad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.io.File

import com.example.proyecto.data.Anciano
import com.example.proyecto.data.Cuidador
import kotlinx.coroutines.flow.asStateFlow


class ActivityViewModel() : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    private val authViewModel = AuthViewModel()


    private val _ultimaImagenUrl = MutableStateFlow<String?>(null)
    val ultimaImagenUrl: StateFlow<String?> = _ultimaImagenUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _activities = mutableStateListOf<Actividad>()
    val activities: List<Actividad> get() = _activities

    private val _cambio = mutableStateOf(false)
    val cambio: State<Boolean> = _cambio

    private val _currentAnciano = MutableStateFlow<String?>(null)
    val currentAnciano: StateFlow<String?> = _currentAnciano.asStateFlow()



    fun cambio() {
        Log.i("cambio", "se cambio")
        _cambio.value = !_cambio.value
    }

    fun relaunch() {
        Log.i("ActivityViewModel", "relaunch $currentAnciano")
        if (authViewModel.currentEntity.value is Anciano) {
            Log.i("ActivityViewModel", "es un anciano")
            auth.currentUser?.let { loadActivitiesForUser(it.uid) }
        } else if (authViewModel.currentEntity.value is Cuidador){
                Log.i("ActivityViewModel", "No es un anciano")
            currentAnciano.value?.let { loadActivitiesForUser(it) }
        }
        else{
            Log.i("ActivityViewModel", "No es un anciano ni un cuidador")
        }
    }

    init {
        Log.i("ActivityViewModel", "init ${auth.currentUser?.uid}")
        if (authViewModel.currentEntity.value is Anciano) {
            auth.currentUser?.let { loadActivitiesForUser(it.uid) }
        } else if (authViewModel.currentEntity.value is Cuidador) {
            if (!(currentAnciano.value == null || currentAnciano.value == "")) {
                currentAnciano.value?.let { loadActivitiesForUser(it) }
            }
        }
    }

    private suspend fun uploadActivityToFirebase(actividad: Actividad) {
        try {
            val activityMap = hashMapOf(
                "id" to actividad.id,
                "ancianoID" to actividad.ancianoID,
                "actividad" to actividad.actividad,
                "ubicacion" to actividad.ubicacion,
                "infoAdicional" to actividad.infoAdicional,
                "imagenUrl" to actividad.imagenUrl,
                "imagenFilename" to actividad.imagenFilename
            )
            firestore.collection("actividades")
                .document(actividad.id)
                .set(activityMap)
                .await()
        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error sincronizando actividad: ${e.message}")
        }
    }

    private suspend fun uploadImageToStorage(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            try {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val filename = "${UUID.randomUUID()}.jpg"
                val imageRef = storage.reference.child("actividades/$filename")
                imageRef.putBytes(baos.toByteArray()).await()
                imageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error subiendo imagen: ${e.message}", e)
                null
            }
        }
    }

    private suspend fun devolverUriPicture(imagen: Bitmap?): String? {
        return imagen?.let { uploadImageToStorage(it) }
    }

    fun addActivity(
        actividad: String,
        ubicacion: String,
        imagenUrl: String?,
        infoAdicional: String?
    ) {
        if (authViewModel.currentEntity.value is Anciano) {
            authViewModel.getCurrentUserID()?.let { userID ->
                val newActivity = Actividad(
                    ancianoID = userID,
                    actividad = actividad,
                    ubicacion = ubicacion,
                    imagenUrl = imagenUrl,
                    infoAdicional = infoAdicional
                )
                viewModelScope.launch {
                    uploadActivityToFirebase(newActivity)
                    _activities.add(newActivity)
                }
            }
        } else {
            currentAnciano.let { userID ->
                val newActivity = userID?.let {
                    it.value?.let { it1 ->
                        Actividad(
                            ancianoID = it1,
                            actividad = actividad,
                            ubicacion = ubicacion,
                            imagenUrl = imagenUrl,
                            infoAdicional = infoAdicional
                        )
                    }
                }
                viewModelScope.launch {
                    newActivity?.let {
                        uploadActivityToFirebase(it)
                        _activities.add(it)
                    }
                }
            }
        }
    }

    fun cargarAncianoActualDesdeJson(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "ancianoActual.json")
                if (!file.exists()) {
                    Log.w("AncianoViewModel", "Archivo ancianoActual.json no encontrado.")
                    return@launch
                }
                val contenido = file.readText()
                val json = JSONObject(contenido)
                val id = json.optString("currentAncianoId", null)
                withContext(Dispatchers.Main) {
                    _currentAnciano.value = id
                    Log.i("AncianoViewModel", "Anciano cargado desde JSON: $id")
                }
            } catch (e: Exception) {
                Log.e("AncianoViewModel", "Error leyendo JSON: ${e.message}", e)
            }
        }
    }

    fun guardarActividadCompleta(
        imagen: Bitmap?,
        actividad: String,
        ubicacion: String,
        infoAdicional: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val urlImagen = imagen?.let { devolverUriPicture(it) }
            addActivity(actividad, ubicacion, urlImagen, infoAdicional)
            _isLoading.value = false
            onComplete()
        }
    }

    fun loadActivitiesForUser(userId: String) {
        viewModelScope.launch {
            try {
                val querySnapshot = firestore
                    .collection("actividades")
                    .whereEqualTo("ancianoID", userId)
                    .get()
                    .await()

                val remoteList = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Actividad(
                            id = doc.getString("id") ?: UUID.randomUUID().toString(),
                            ancianoID = doc.getString("ancianoID") ?: "",
                            actividad = doc.getString("actividad") ?: "",
                            ubicacion = doc.getString("ubicacion") ?: "",
                            infoAdicional = doc.getString("infoAdicional"),
                            imagenUrl = doc.getString("imagenUrl"),
                            imagenFilename = doc.getString("imagenFilename")
                        )
                    } catch (e: Exception) {
                        Log.e("ActivityViewModel", "Error parseando actividad (ID=${doc.id}): ${e.message}", e)
                        null
                    }
                }.reversed()

                withContext(Dispatchers.Main) {
                    _activities.clear()
                    _activities.addAll(remoteList)
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error cargando actividades para userId=$userId: ${e.message}", e)
            }
        }
    }
}
