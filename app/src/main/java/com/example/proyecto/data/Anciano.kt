package com.example.proyecto.data

class Anciano(
    override val email: String
) : Usuario(email) {
    companion object {
        fun ancianoListStarter(): List<Anciano> {
            return listOf(
                Anciano("anciano1@example.com"),
                Anciano("anciano2@example.com"),
                Anciano("anciano3@example.com"),
                Anciano("anciano4@example.com"),
                Anciano("anciano5@example.com"),
                Anciano("simondiaz@yopmail.com")
            )
        }
    }

    var emergencia: Boolean = false
        set(value) {
            field = value
        }
}
