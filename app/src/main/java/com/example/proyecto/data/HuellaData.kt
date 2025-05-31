package com.example.proyecto.data

import kotlinx.serialization.Serializable

@Serializable
data class HuellaData(
    val correo: String,
    val contra: String
)
