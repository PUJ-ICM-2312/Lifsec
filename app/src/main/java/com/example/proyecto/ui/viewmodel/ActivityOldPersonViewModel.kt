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
import java.io.File
import java.io.IOException
import java.util.UUID
import androidx.compose.runtime.State

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

    fun generarCambio() {
        _cambio.value = !_cambio.value
    }


    init {
        loadLocalAndSync()
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

    fun procesarImagenYGuardarUrl(imagen: Bitmap?) {
        viewModelScope.launch {
            _isLoading.value = true
            val url = devolverUriPicture(imagen)
            _ultimaImagenUrl.value = url
            _isLoading.value = false
        }
    }

    fun addActivity(
        actividad: String,
        ubicacion: String,
        imagenUrl: String?,
        infoAdicional: String?
    ) {
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
        viewModelScope.launch {
            _isLoading.value = true
            val urlImagen = imagen?.let { devolverUriPicture(it) }
            addActivity(actividad, ubicacion, urlImagen, infoAdicional)
            _isLoading.value = false
            onComplete()
        }
    }



}
