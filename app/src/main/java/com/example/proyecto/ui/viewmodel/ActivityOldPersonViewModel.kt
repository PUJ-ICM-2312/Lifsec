package com.example.proyecto.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Actividad
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID // Para nombres de archivo únicos

// Heredar de AndroidViewModel para obtener el contexto de la aplicación
class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val _activities = mutableStateListOf<Actividad>()
    val activities: List<Actividad> get() = _activities

    // Constantes para nombres de archivo y directorio
    private val JSON_FILENAME = "activities_data.json"
    private val IMAGE_SUBDIR = "activity_images" // Subdirectorio para imágenes

    // Inicializador para cargar datos al crear el ViewModel
    init {
        loadLocalActivities()
    }

    // --- Funciones existentes (modificadas para guardar después de cambios) ---

    fun addActivity(actividad: String, ubicacion: String, imagen: Bitmap?, infoAdicional: String?) {
        // Crear la actividad (aún sin nombre de archivo de imagen)
        val newActivity = Actividad(actividad, ubicacion, imagen, infoAdicional)
        _activities.add(newActivity)
        // Guardar toda la lista actualizada (incluyendo la nueva actividad con su imagen)
        storeLocalActivities()
    }

    fun removeActivity(index: Int) {
        if (index in _activities.indices) {
            // Obtener el nombre del archivo de imagen antes de eliminar la actividad
            val filenameToRemove = _activities[index].imagenFilename
            _activities.removeAt(index)
            // Guardar la lista actualizada
            storeLocalActivities()
            // Eliminar el archivo de imagen asociado (si existe) en segundo plano
            filenameToRemove?.let { deleteImageFile(it) }
        }
    }

    fun updateActivity(index: Int, updatedActivity: Actividad) {
        if (index in _activities.indices) {
            val oldFilename = _activities[index].imagenFilename
            // Si la imagen cambió (o se añadió una nueva), el 'updatedActivity' tendrá un Bitmap
            // y 'imagenFilename' será null inicialmente. storeLocalActivities se encargará
            // de guardar el nuevo Bitmap y asignar el nuevo nombre de archivo.
            _activities[index] = updatedActivity
            // Guardar la lista actualizada
            storeLocalActivities()
            // Si la imagen anterior era diferente y existía, eliminar el archivo antiguo
            if (oldFilename != null && oldFilename != updatedActivity.imagenFilename) {
                deleteImageFile(oldFilename)
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
                val jsonFile = File(context.filesDir, JSON_FILENAME)
                if (jsonFile.exists()) {
                    context.openFileInput(JSON_FILENAME).use { fis ->
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
            val imageDir = context.getDir(IMAGE_SUBDIR, Context.MODE_PRIVATE)
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
                context.openFileOutput(JSON_FILENAME, Context.MODE_PRIVATE).use { fos ->
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
        val imageDir = context.getDir(IMAGE_SUBDIR, Context.MODE_PRIVATE)
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
        val imageDir = context.getDir(IMAGE_SUBDIR, Context.MODE_PRIVATE)
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
            val imageDir = context.getDir(IMAGE_SUBDIR, Context.MODE_PRIVATE)
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
        val imageDir = context.getDir(IMAGE_SUBDIR, Context.MODE_PRIVATE)
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