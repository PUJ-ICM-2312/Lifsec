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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {
    enum class UserType {
        ANCIANO,
        CUIDADOR,
        NONE
    }

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val _currentEntity = MutableStateFlow<Usuario?>(null)
    private val _currentUser = MutableStateFlow(auth.currentUser)
    private val _userType = MutableStateFlow(UserType.NONE)
    private val _emergencia = MutableStateFlow(false)

    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    val userType: StateFlow<UserType> = _userType.asStateFlow()
    val emergencia: StateFlow<Boolean> = _emergencia.asStateFlow()

    val currentAnciano: Anciano?
        get() = _currentEntity.value as? Anciano

    // Estados para UI
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var feedbackMessage by mutableStateOf<String?>(null)
        private set


    fun setCurrentEntity(user: Usuario?) {
        _currentEntity.value = user
        _userType.value = when(user) {
            is Anciano -> UserType.ANCIANO
            is Cuidador -> UserType.CUIDADOR
            else -> UserType.NONE
        }
    }

    fun getCurrentUserID(): String? = _currentEntity.value?.userID

    fun isAnciano(): Boolean = _userType.value == UserType.ANCIANO

    fun isCuidador(): Boolean = _userType.value == UserType.CUIDADOR

    fun onEmailChange(newEmail: String) {
        email = newEmail
        feedbackMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        feedbackMessage = null
    }

    fun getAuth(): FirebaseAuth = auth

    fun clearFeedbackMessage() {
        feedbackMessage = null
    }

    fun setEmergencia(value: Boolean) {
        currentAnciano?.let {
            it.emergencia = value
            _emergencia.value = value
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _currentEntity.value = null
        _userType.value = UserType.NONE
        email = ""
        password = ""
        feedbackMessage = "Sesión cerrada."
    }

    fun registerUser(isAnciano: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Los campos no pueden estar vacíos")
            return
        }

        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    firebaseUser?.let { user ->
                        // Crear el GeoPoint con una ubicación inicial por defecto
                        val defaultLocation = GeoPoint(0.0, 0.0)

                        // Crear objeto según tipo de usuario con los constructores correctos
                        val nuevoUsuario = if (isAnciano) {
                            Anciano(
                                userID = user.uid,
                                email = email,
                                nombre = "", // Nombre vacío prueba
                                password = password,
                                latLng = defaultLocation
                            ).apply {
                                emergencia = false
                            }
                        } else {
                            Cuidador(
                                userID = user.uid,
                                email = email,
                                nombre = "", // Nombre vacío prueba
                                password = password,
                                latLng = defaultLocation
                            ).apply {
                                ancianosACargo = mutableListOf()
                            }
                        }

                        // Guardar en Firestore
                        val coleccion = if (isAnciano) "ancianos" else "cuidadores"
                        firestore.collection(coleccion)
                            .document(user.uid)
                            .set(nuevoUsuario)
                            .addOnSuccessListener {
                                setCurrentEntity(nuevoUsuario)
                                isLoading = false
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                onError("Error al guardar datos: ${e.message}")
                            }
                    }
                } else {
                    isLoading = false
                    onError(task.exception?.message ?: "Error al registrar usuario")
                }
            }
    }

    fun signInAndLoadUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Los campos no pueden estar vacíos")
            return
        }

        isLoading = true

        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        firebaseUser?.let { user ->
                            // Buscar en ambas colecciones usando el uid
                            firestore.collection("ancianos").document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val anciano = document.toObject(Anciano::class.java)
                                        setCurrentEntity(anciano)
                                        isLoading = false
                                        onSuccess()
                                    } else {
                                        firestore.collection("cuidadores").document(user.uid).get()
                                            .addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    val cuidador = document.toObject(Cuidador::class.java)
                                                    setCurrentEntity(cuidador)
                                                    isLoading = false
                                                    onSuccess()
                                                } else {
                                                    isLoading = false
                                                    onError("No se pudo encontrar la entidad con el UID proporcionado.")
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                onError("Error al buscar cuidador: ${e.message}")
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    onError("Error al buscar anciano: ${e.message}")
                                }
                        }
                    } else {
                        isLoading = false
                        val errorMessage = when (task.exception?.message) {
                            "The supplied auth credential is incorrect, malformed or has expired." ->
                                "Error de autenticación. Por favor, intente nuevamente."
                            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." ->
                                "Error de conexión. Verifique su conexión a internet."
                            else -> task.exception?.message ?: "Error al iniciar sesión"
                        }
                        onError(errorMessage)
                        password = ""
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            onError("Error inesperado: ${e.message}")
        }
    }

    // Método para verificar el estado de la conexión
    private fun checkNetworkConnection(): Boolean {
        // Implementar verificación de conexión
        return true
    }

    // Método para limpiar la sesión y reiniciar
    fun resetSession() {
        auth.signOut()
        _currentUser.value = null
        _currentEntity.value = null
        _userType.value = UserType.NONE
        password = ""  // Limpiar contraseña pero mantener email para conveniencia
        feedbackMessage = null
    }

    fun updateFeedbackMessage(message: String?) {
        feedbackMessage = message
    }
}
