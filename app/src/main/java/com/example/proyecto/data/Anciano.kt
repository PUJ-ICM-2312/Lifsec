package com.example.proyecto.data

class Anciano(
    override val email: String
) : Usuario(email) {
    companion object {
        fun ancianoListStarter(): List<Anciano> {
            return listOf(
                Anciano("anciano1@exam.com"),
                Anciano("anciano2@exam.com"),
                Anciano("anciano3@exam.com"),
                Anciano("anciano4@exam.com"),
                Anciano("anciano5@exam.com"),
                Anciano("simondiaz@yopmail.com")
            )
        }
    }

    var emergencia: Boolean = false
        set(value) {
            field = value
        }
}
