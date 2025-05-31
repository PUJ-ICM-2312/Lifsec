package com.example.proyecto.data

data class Recordatorio(
    val ancianoID: String,
    val titulo: String,
    val fecha: String,
    val infoAdicional: String? = null
)
