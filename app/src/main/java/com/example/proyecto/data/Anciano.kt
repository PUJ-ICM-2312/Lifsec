package com.example.proyecto.data

class Anciano(
    override val email: String
) : Usuario(email) {

    var emergencia: Boolean = false
        set(value) {
            field = value
        }
}
