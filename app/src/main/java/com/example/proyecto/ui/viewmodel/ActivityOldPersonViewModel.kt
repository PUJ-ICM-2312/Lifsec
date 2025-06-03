package com.example.proyecto.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Actividad
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import androidx.compose.runtime.State


// Heredar de AndroidViewModel para obtener el contexto de la aplicación
class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage
    private val authViewModel = AuthViewModel()

    private val _cambio = mutableStateOf(false)
    val cambio: State<Boolean> = _cambio
    fun generarCambio(){
        _cambio.value = !_cambio.value

    }
    private val _activities = mutableStateListOf<Actividad>()
    val activities: List<Actividad> get() = _activities
    // Constantes para los nombres de directorios y archivos usados en el almacenamiento local
    companion object {
        private const val JSON_DIR = "LifSec_Json_Data" // Directorio para archivos JSON
        private const val IMAGE_SUBDIR = "LifSec_IMG"   // Subdirectorio para imágenes
        private const val JSON_FILENAME = "actividades.json" // Nombre del archivo JSON principal
    }

    init {
        // Primero cargar datos locales y luego sincronizar con Firebase
        loadLocalAndSync()
    }

    private fun loadLocalAndSync() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Cargar datos locales
            loadLocalActivities()

            // 2. Sincronizar con Firebase
            syncActivitiesWithFirebase()
        }
    }

    private suspend fun syncActivitiesWithFirebase() {
        authViewModel.getCurrentUserID()?.let { userId ->
            _activities.forEach { actividad ->
                // Subir cada actividad a Firestore
                uploadActivityToFirebase(actividad)
            }

            // Limpiar almacenamiento local después de sincronizar
            clearLocalStorage()
        }
    }

    private suspend fun uploadActivityToFirebase(actividad: Actividad) {
        try {
            // 1. Si hay imagen, subirla primero a Storage
            val imageUrl = actividad.imagen?.let { bitmap ->
                uploadImageToStorage(bitmap)
            }

            // 2. Crear documento de actividad para Firestore
            val activityMap = hashMapOf(
                "ancianoID" to actividad.ancianoID,
                "actividad" to actividad.actividad,
                "ubicacion" to actividad.ubicacion,
                "infoAdicional" to actividad.infoAdicional,
                "imagenUrl" to imageUrl
            )

            // 3. Subir a Firestore
            firestore.collection("actividades")
                .add(activityMap)
                .await()

        } catch (e: Exception) {
            Log.e("ActivityViewModel", "Error sincronizando actividad: ${e.message}")
        }
    }

    private suspend fun uploadImageToStorage(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageRef = storage.reference.child("actividades/${UUID.randomUUID()}.jpg")

            val uploadTask = imageRef.putBytes(baos.toByteArray()).await()
            return@withContext imageRef.downloadUrl.await().toString()
        }
    }

    private fun clearLocalStorage() {
        val context = getApplication<Application>().applicationContext

        // Eliminar archivo JSON
        val jsonDir = File(context.filesDir, JSON_DIR)
        val jsonFile = File(jsonDir, JSON_FILENAME)
        if (jsonFile.exists()) jsonFile.delete()

        // Eliminar directorio de imágenes
        val imageDir = File(context.filesDir, IMAGE_SUBDIR)
        imageDir.deleteRecursively()

        // Limpiar lista en memoria
        _activities.clear()
    }

    fun addActivity(actividad: String, ubicacion: String, imagen: Bitmap?, infoAdicional: String?) {
        authViewModel.getCurrentUserID()?.let { userID ->
            val newActivity = Actividad(
                ancianoID = userID,
                actividad = actividad,
                ubicacion = ubicacion,
                imagen = imagen,
                infoAdicional = infoAdicional
            )

            viewModelScope.launch {
                // Subir directamente a Firebase
                uploadActivityToFirebase(newActivity)

                // Actualizar lista local
                _activities.add(newActivity)
            }
        }
    }

    // --- Funciones de Carga y Guardado ---

    fun loadLocalActivities() {
        viewModelScope.launch(Dispatchers.IO) { // Ejecutar en hilo de I/O
            val context = getApplication<Application>().applicationContext
            val gson = Gson()
            var loadedList: List<Actividad> = emptyList()

            // 1. Cargar JSON
            try {
                val jsonDir = File(context.filesDir, JSON_DIR)
                val jsonFile = File(jsonDir, JSON_FILENAME)
                if (jsonFile.exists()) {
                    jsonFile.inputStream().use { fis ->
                        val jsonString = fis.bufferedReader().use { it.readText() }
                        // Definir el tipo de lista para Gson
                        val listType = object : TypeToken<List<Actividad>>() {}.type
                        loadedList = gson.fromJson(jsonString, listType) ?: emptyList()
                        Log.d("ActivityViewModel", "JSON cargado exitosamente.")
                    }
                } else {
                    Log.d("ActivityViewModel", "Archivo JSON no encontrado, empezando con lista vacía.")
                }
            } catch (e: IOException) {
                Log.e("ActivityViewModel", "Error al leer JSON: ${e.message}", e)
                // Podrías querer manejar este error de forma más específica
            } catch (e: Exception) { // Captura errores de deserialización de Gson
                Log.e("ActivityViewModel", "Error al deserializar JSON: ${e.message}", e)
            }


            // 2. Cargar Imágenes para cada actividad
            val activitiesWithImages = loadedList.map { activity ->
                if (activity.imagenFilename != null) {
                    val bitmap = loadBitmapFromFile(context, activity.imagenFilename!!)
                    activity.copy(imagen = bitmap) // Crear copia con el Bitmap cargado
                } else {
                    activity // Mantener la actividad sin cambios si no hay nombre de archivo
                }
            }

            // 3. Actualizar la lista en el hilo principal
            withContext(Dispatchers.Main) {
                _activities.clear()
                _activities.addAll(activitiesWithImages)
                Log.d("ActivityViewModel", "Lista de actividades actualizada en UI.")
            }
        }
    }

    // Guarda la lista ACTUAL (_activities) en el almacenamiento interno
    fun storeLocalActivities() {
        viewModelScope.launch(Dispatchers.IO) { // Ejecutar en hilo de I/O
            val context = getApplication<Application>().applicationContext
            val gson = Gson()
            val activitiesToStore = mutableListOf<Actividad>()

            // 1. Preparar datos y guardar imágenes
            val imageDir = File(context.filesDir, IMAGE_SUBDIR)
            if (!imageDir.exists()) {
                imageDir.mkdirs() // Crear directorio si no existe
            }

            // Lista temporal para evitar modificar _activities mientras se itera
            val currentActivities = _activities.toList()

            currentActivities.forEach { activity ->
                var filename: String? = activity.imagenFilename
                // Si hay un Bitmap y no tiene nombre de archivo O si el Bitmap cambió (difícil de detectar directamente, asumimos que si hay bitmap nuevo, se guarda)
                if (activity.imagen != null && filename == null) {
                    // Generar un nuevo nombre de archivo único si no existe
                    filename = "${UUID.randomUUID()}.png"
                    saveBitmapToFile(context, activity.imagen!!, filename!!)
                }
                // Añadir a la lista para serializar (con el nombre de archivo actualizado)
                // Asegúrate de que el Bitmap no se incluya en la serialización (ya es @Transient)
                activitiesToStore.add(activity.copy(imagenFilename = filename, imagen = null)) // Copia sin el bitmap para guardar
            }


            // 2. Convertir a JSON
            val jsonString = gson.toJson(activitiesToStore)

            // 3. Guardar JSON en archivo interno
            try {
                val jsonDir = File(context.filesDir, JSON_DIR)
                if (!jsonDir.exists()) jsonDir.mkdirs()
                val jsonFile = File(jsonDir, JSON_FILENAME)
                jsonFile.outputStream().use { fos ->
                    fos.write(jsonString.toByteArray(Charsets.UTF_8))
                    Log.d("ActivityViewModel", "JSON guardado exitosamente.")
                }
            } catch (e: IOException) {
                Log.e("ActivityViewModel", "Error al guardar JSON: ${e.message}", e)
            }

            // Opcional: Limpiar imágenes antiguas que ya no se referencian
            cleanupOrphanedImages(context, activitiesToStore.mapNotNull { it.imagenFilename })
        }
    }

    // --- Funciones auxiliares para manejo de Bitmaps ---

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): Boolean {
        val imageDir = File(context.filesDir, IMAGE_SUBDIR)
        val imageFile = File(imageDir, filename)
        var success = false
        try {
            FileOutputStream(imageFile).use { fos ->
                // Comprimir el bitmap en formato PNG (puedes usar JPEG si prefieres)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos) // 90 es calidad (0-100)
                success = true
                Log.d("ActivityViewModel", "Imagen guardada: ${imageFile.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("ActivityViewModel", "Error al guardar bitmap $filename: ${e.message}", e)
        }
        return success
    }

    private fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val imageDir = File(context.filesDir, IMAGE_SUBDIR)
        val imageFile = File(imageDir, filename)
        var bitmap: Bitmap? = null
        if (imageFile.exists()) {
            try {
                bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                Log.d("ActivityViewModel", "Imagen cargada: ${imageFile.absolutePath}")
            } catch (e: Exception) { // Captura errores al decodificar
                Log.e("ActivityViewModel", "Error al cargar bitmap $filename: ${e.message}", e)
            }
        } else {
            Log.w("ActivityViewModel", "Archivo de imagen no encontrado: $filename")
        }
        return bitmap
    }

    private fun deleteImageFile(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val imageDir = File(context.filesDir, IMAGE_SUBDIR)
            val imageFile = File(imageDir, filename)
            try {
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        Log.d("ActivityViewModel", "Imagen eliminada: ${imageFile.absolutePath}")
                    } else {
                        Log.w("ActivityViewModel", "No se pudo eliminar la imagen: ${imageFile.absolutePath}")
                    }
                }
            } catch (e: SecurityException) {
                Log.e("ActivityViewModel", "Error de seguridad al eliminar imagen $filename: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error al eliminar imagen $filename: ${e.message}", e)
            }
        }
    }

    // Opcional: Elimina imágenes en el directorio que no están referenciadas en la lista actual
    private fun cleanupOrphanedImages(context: Context, referencedFilenames: List<String>) {
        val imageDir = File(context.filesDir, IMAGE_SUBDIR)
        if (imageDir.exists() && imageDir.isDirectory) {
            val referencedSet = referencedFilenames.toSet()
            imageDir.listFiles()?.forEach { file ->
                if (file.isFile && !referencedSet.contains(file.name)) {
                    try {
                        if (file.delete()) {
                            Log.d("ActivityViewModel", "Imagen huérfana eliminada: ${file.name}")
                        } else {
                            Log.w("ActivityViewModel", "No se pudo eliminar imagen huérfana: ${file.name}")
                        }
                    } catch (e: Exception) {
                        Log.e("ActivityViewModel", "Error limpiando imagen huérfana ${file.name}: ${e.message}", e)
                    }
                }
            }
        }
    }
}
