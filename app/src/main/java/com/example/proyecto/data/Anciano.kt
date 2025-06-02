package com.example.proyecto.data

import com.google.firebase.firestore.GeoPoint

data class Anciano(
    override var userID: String = "",
    override var email: String = "",
    override var nombre: String = "",
    override var password: String = "",
    override var latLng: GeoPoint = GeoPoint(0.0, 0.0),
    override var conectado: Boolean = false,
    var emergencia: Boolean = false,
    var cuidadoresIds: MutableList<String> = mutableListOf()
) : Usuario(userID, email, nombre, password, latLng) {

    val actividades: MutableList<Actividad> = mutableListOf()
    val Recordatorio: MutableList<Recordatorio> = mutableListOf()
}