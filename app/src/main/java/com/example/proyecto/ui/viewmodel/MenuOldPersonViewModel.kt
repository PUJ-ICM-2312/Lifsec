package com.example.proyecto.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.State


class MenuOldPersonViewModel: ViewModel() {
    var apartado: String = "Menu"
    private val _cambio = mutableStateOf(false)
    val cambio: State<Boolean> = _cambio

    fun cambiarApartado(nuevoApartado: String) {
        apartado = nuevoApartado
    }

    fun leerApartado(): String {
        return apartado
    }

    fun generarCambio(){
        _cambio.value = !_cambio.value

    }

}