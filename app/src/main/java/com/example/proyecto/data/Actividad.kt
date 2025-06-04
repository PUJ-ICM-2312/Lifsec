package com.example.proyecto.data

import android.graphics.Bitmap


data class Actividad(
    val ancianoID: String,
    val actividad: String,
    val ubicacion: String,
    val infoAdicional: String? = null,
    val imagenUrl: String? = null,       // Ahora se guarda la URL en lugar de un Bitmap
    val imagenFilename: String? = null    // Nombre del archivo de la imagen, si necesitas referenciarla
)

