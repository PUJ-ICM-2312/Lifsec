package com.example.proyecto.ui.viewmodel

import android.app.Application
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.proyecto.data.Anciano

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
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

    fun cambio() {
        Log.i("cambio", "se cambio")
        _cambio.value = !_cambio.value
    }



    init {
        Log.i("ActivityViewModel", "init ${auth.currentUser?.uid}")
        if(authViewModel.currentEntity.value is Anciano){
            Log.i("ActivityViewModel", "es un anciano")
            auth.currentUser?.let { loadActivitiesForUser(it.uid) }
            loadLocalAndSync()
        }else{
            Log.i("ActivityViewModel", "No es un anciano")
        }

    }

    private fun loadLocalAndSync() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("ActivityViewModel", "entro a loadLocalAndSync")
            syncActivitiesWithFirebase()
        }
    }

    private suspend fun syncActivitiesWithFirebase() {
        Log.i("ActivityViewModel", "entro a syncActivitiesWithFirebase")
        authViewModel.getCurrentUserID()?.let { userId ->
            _activities.forEach { actividad ->
                uploadActivityToFirebase(actividad)
            }
        }
    }

    private suspend fun uploadActivityToFirebase(actividad: Actividad) {
        try {
            val activityMap = hashMapOf(
                "ancianoID" to actividad.ancianoID,
                "actividad" to actividad.actividad,
                "ubicacion" to actividad.ubicacion,
                "infoAdicional" to actividad.infoAdicional,
                "imagenUrl" to actividad.imagenUrl
            )
            firestore.collection("actividades")
                .add(activityMap)
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
        Log.i("ActivityViewModel", "Guardando actividad en firestore")
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
    }

    fun guardarActividadCompleta(
        imagen: Bitmap?,
        actividad: String,
        ubicacion: String,
        infoAdicional: String?,
        onComplete: () -> Unit
    ) {
        Log.i("ActivityViewModel", "Guardando actividad completa")
        viewModelScope.launch {
            _isLoading.value = true
            val urlImagen = imagen?.let { devolverUriPicture(it) }
            addActivity(actividad, ubicacion, urlImagen, infoAdicional)
            _isLoading.value = false
            onComplete()
        }
    }

    /**
     * Carga todas las actividades en Firestore que pertenecen al usuario con el UID dado,
     * y las guarda en la lista interna `_activities`.
     */
    fun loadActivitiesForUser(userId: String) {
        viewModelScope.launch {
            try {
                // 1. Hacer la consulta a Firestore, filtrando por el campo "ancianoID"
                val querySnapshot = firestore
                    .collection("actividades")
                    .whereEqualTo("ancianoID", userId)
                    .get()
                    .await()

                // 2. Mapear cada documento a un objeto Actividad
                val remoteList = querySnapshot.documents.mapNotNull { doc ->
                    try {
                        Actividad(
                            ancianoID    = doc.getString("ancianoID") ?: "",
                            actividad    = doc.getString("actividad") ?: "",
                            ubicacion    = doc.getString("ubicacion") ?: "",
                            infoAdicional = doc.getString("infoAdicional"),
                            imagenUrl     = doc.getString("imagenUrl")
                        )
                    } catch (e: Exception) {
                        Log.e("ActivityViewModel", "Error parseando actividad (ID=${doc.id}): ${e.message}", e)
                        null
                    }
                }

                // Invertir el orden: primero cargada → última en la lista, última cargada → primera en la lista, para que aparezcan en orden
                val reversedList = remoteList.reversed()
                // 3. Actualizar la lista interna `_activities` en el hilo principal
                withContext(Dispatchers.Main) {
                    _activities.clear()
                    _activities.addAll(reversedList)
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error cargando actividades para userId=$userId: ${e.message}", e)
            }
        }
    }


}
