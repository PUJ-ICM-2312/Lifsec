package com.example.proyecto.data

import android.graphics.Bitmap

data class Actividad(
    val ancianoID: String,
    val actividad: String,
    val ubicacion: String,
    @Transient var imagen: Bitmap? = null, // Gson ignorará este campo
    val infoAdicional: String?,
    var imagenFilename: String? = null // Nombre del archivo para la imagen
)

