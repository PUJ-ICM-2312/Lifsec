package com.example.proyecto.data

import com.google.firebase.firestore.GeoPoint

data class Anciano(
    override var userID: String = "",
    override var email: String = "",
    override var nombre: String = "",
    override var password: String = "",
    override var latLng: GeoPoint = GeoPoint(0.0, 0.0),
    var emergencia: Boolean = false
) : Usuario(userID, email, nombre, password, latLng) {

    val actividades: MutableList<Actividad> = mutableListOf()
    val Recordatorio: MutableList<Recordatorio> = mutableListOf()
}