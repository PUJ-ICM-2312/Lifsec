package com.example.proyecto.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Anciano
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {




    private val auth: FirebaseAuth = Firebase.auth

    //Expone la entidad con la info correspondiente al usuario
    private val _currentEntity = MutableStateFlow<Usuario?>(null)

    // Expone el usuario actual (null si no está en sesión)
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    val currentAnciano: Anciano?
        get() = _currentEntity.value as? Anciano

    //Expone si ha ocurrido una emergencia o no
    private val _emergencia = MutableStateFlow(false)
    val emergencia: StateFlow<Boolean> = _emergencia.asStateFlow()



    // Estados para UI
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var feedbackMessage by mutableStateOf<String?>(null)
        private set




    // Funciones para actualizar el estado desde la UI
    fun onEmailChange(newEmail: String) {
        email = newEmail
        feedbackMessage = null // Limpiar al escribir
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        feedbackMessage = null // Limpiar al escribir
    }

    fun getAuth(): FirebaseAuth {
        return auth
    }
    fun clearFeedbackMessage() {
        feedbackMessage = null
    }

    fun setEmergencia(value: Boolean) {
        (currentAnciano ?: return).emergencia = value
        _emergencia.value = value
    }



    /**
     * Registra usuario en Firebase y lo agrega a la lista correspondiente.
     * @param isElderly true si es anciano, false si es cuidador
     */



    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _currentEntity.value = null
        email = ""
        password = ""
        feedbackMessage = "Sesión cerrada."
    }

}