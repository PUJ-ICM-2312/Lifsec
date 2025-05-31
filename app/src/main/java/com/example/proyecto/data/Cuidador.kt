package com.example.proyecto.data

import com.google.firebase.firestore.GeoPoint

data class Cuidador(
    override var userID: String = "",
    override var email: String = "",
    override var nombre: String = "",
    override var password: String = "",
    override var latLng: GeoPoint = GeoPoint(0.0, 0.0)
) : Usuario(userID, email, nombre, password, latLng){

    // Lista de ancianos asignados a este cuidador
    var ancianosACargo: MutableList<Anciano> = mutableListOf()
}