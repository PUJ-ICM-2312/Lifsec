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
    // TODO: cambiar a ancianos obtenidos de la base de datos
    private val _elderlyUsers = mutableStateListOf<Anciano>().apply {
        addAll(Anciano.ancianoListStarter())
    }
    val elderlyUsers: List<Anciano> = _elderlyUsers

    //TODO: cambiar a cuidadores obtenidos de la bd
    private val _caretakersList = mutableStateListOf<Cuidador>()
    val caretakersList: List<Cuidador> = _caretakersList

    //Expone la entidad con la info correspondiente al usuario
    private val _currentEntity = MutableStateFlow<Usuario?>(null)
    val currentEntity: StateFlow<Usuario?> = _currentEntity.asStateFlow()

    // Expone el usuario actual (null si no está en sesión)
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    val currentAnciano: Anciano?
        get() = _currentEntity.value as? Anciano

    //Expone si ha ocurrido una emergencia o no
    private val _emergencia = MutableStateFlow(false)
    val emergencia: StateFlow<Boolean> = _emergencia.asStateFlow()

    // Otro helper para cuidadores:
    val currentCuidador: Cuidador?
        get() = _currentEntity.value as? Cuidador

    // Estados para UI
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var feedbackMessage by mutableStateOf<String?>(null)
        private set


    init {
        // Cada vez que cambie el currentUser, buscas y asignas un Usuario (Anciano o Cuidador)
        viewModelScope.launch {
            _currentUser.collect { firebaseUser ->
                _currentEntity.value = firebaseUser
                    ?.email
                    ?.let { email ->
                        elderlyUsers.firstOrNull { it.email == email }
                            ?: caretakersList.firstOrNull { it.email == email }
                    }

                _emergencia.value = (currentAnciano?.emergencia ?: false)
            }
        }
    }

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

    fun checkUserElderlyType(): Boolean {
        if (_currentUser.value != null) {
            val currentUserEmail = _currentUser.value!!.email
            return currentUserEmail in elderlyUsers.map { it.email }
        } else {
            return false
        }
    }

    fun setEmergencia(value: Boolean) {
        (currentAnciano ?: return).emergencia = value
        _emergencia.value = value
    }

    fun signInUser() {
        isLoading = true
        feedbackMessage = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _currentUser.value = user

                    // ——— ACTUALIZAR currentEntity ———
                    _currentEntity.value = user?.email
                        ?.let { mail ->
                            elderlyUsers.firstOrNull { it.email == mail }
                                ?: caretakersList.firstOrNull { it.email == mail }
                        }

                    // La UI reaccionará y realizará la navegación
                } else {
                    _currentUser.value = null
                    _currentEntity.value = null
                    feedbackMessage = "Error: ${task.exception?.localizedMessage}"
                }
            }
    }

    /**
     * Registra usuario en Firebase y lo agrega a la lista correspondiente.
     * @param isElderly true si es anciano, false si es cuidador
     */
    fun signUpUser(isElderly: Boolean, onSuccess: () -> Unit = {}) {
        isLoading = true
        feedbackMessage = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _currentUser.value = user
                    feedbackMessage = "Usuario registrado exitosamente."

                    user?.email?.let { mail ->
                        if (isElderly) {
                            _elderlyUsers.add(Anciano(email = mail))
                            _currentEntity.value = _elderlyUsers.last()
                        } else {
                            _caretakersList.add(Cuidador(email = mail))
                            _currentEntity.value = _caretakersList.last()
                        }
                    }
                    onSuccess()
                } else {
                    feedbackMessage = "Error Registro: ${task.exception?.localizedMessage ?: "No se pudo registrar"}"
                }
            }
    }


    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _currentEntity.value = null
        email = ""
        password = ""
        feedbackMessage = "Sesión cerrada."
    }

}