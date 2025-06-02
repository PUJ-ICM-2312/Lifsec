package com.example.proyecto.data

import com.google.firebase.firestore.GeoPoint

open class Usuario(
    open val userID: String,
    open val email: String,
    open val nombre: String,
    open val password: String,
    open val latLng: GeoPoint,
    open val conectado: Boolean = false
)