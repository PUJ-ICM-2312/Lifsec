package com.example.proyecto.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.location.LocatOldPerState
import com.example.proyecto.data.location.LocationHandler
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.Usuario
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocatOldPerViewModel(
    private val context: Context,
    private val authViewModel: AuthViewModel,
    private val repositorioUsuarios: RepositorioUsuarios
) : ViewModel() {
    private val _uiLocState = MutableStateFlow(LocatOldPerState())
    val uiLocState: StateFlow<LocatOldPerState> = _uiLocState.asStateFlow()

    private val _currentEntity = MutableStateFlow<Cuidador?>(null)
    val currentEntity: StateFlow<Cuidador?> = _currentEntity.asStateFlow()

    private var locationCallback: LocationCallback? = null

    init {
        viewModelScope.launch {
            authViewModel.currentEntity.collect { entity ->
                if (entity is Cuidador) {
                    Log.d("LocatOldPerVM", "Cuidador actual: ${entity.nombre}")
                    _uiLocState.update { currentState ->
                        currentState.copy(currentEntity = entity)
                    }
                    // Cargar ancianos cuando se actualiza el cuidador
                    observarAncianosConectados(entity.userID)
                }
            }
        }
        setCameraPosition(LatLng(4.60971, -74.08175))
    }

    fun handleLocationPermission(context: Context): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _uiLocState.update { it.copy(isPermissionGranted = hasPermission) }
        return hasPermission
    }

    /**
     * Procesa el resultado de la solicitud de permiso
     */
    fun onPermissionResult(granted: Boolean) {
        _uiLocState.update { it.copy(isPermissionGranted = granted) }
    }

    /**
     * Observa ancianos conectados y actualiza sus ubicaciones
     */
    fun observarAncianosConectados(cuidadorId: String) {
        viewModelScope.launch {
            try {
                Log.d("LocatOldPerVM", "Observando ancianos del cuidador: $cuidadorId")
                repositorioUsuarios.getAncianosConectadosPorCuidadorIdFlow(cuidadorId)
                    .collect { ancianos ->
                        Log.d("LocatOldPerVM", "Ancianos encontrados: ${ancianos.size}")
                        _uiLocState.update { it.copy(ancianos = ancianos) }
                        actualizarMarcadoresAncianos(ancianos)
                    }
            } catch (e: Exception) {
                Log.e("LocatOldPerVM", "Error al observar ancianos: ${e.message}")
            }
        }
    }

    private fun actualizarMarcadoresAncianos(ancianos: List<Usuario>) {
        val newMarkers = ancianos.mapNotNull { anciano ->
            if (anciano.latLng != null) {
                MarkerState(
                    position = LatLng(
                        anciano.latLng.latitude,
                        anciano.latLng.longitude
                    )
                )
            } else null
        }
        Log.d("LocatOldPerVM", "Actualizando ${newMarkers.size} marcadores de ancianos")
        _uiLocState.update { it.copy(oldPersonMarkers = newMarkers) }
    }

    fun setCameraPosition(newLatLng: LatLng) {
        _uiLocState.update { currentState ->
            currentState.copy(
                cameraPositionState = currentState.cameraPositionState.apply {
                    position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(newLatLng, 15f)
                }
            )
        }
    }

    /**
     * Inicia las actualizaciones de ubicación
     */
    fun startLocationUpdates(locationHandler: LocationHandler) {
        when {
            locationCallback != null -> {
                Log.e("LocatOldPerVM", "Actualizaciones ya registradas")
                return
            }
            !_uiLocState.value.isPermissionGranted -> {
                Log.e("LocatOldPerVM", "Permisos no concedidos")
                return
            }
            _uiLocState.value.currentEntity == null -> {
                Log.e("LocatOldPerVM", "No hay cuidador actual")
                return
            }
            !_uiLocState.value.currentEntity!!.conectado -> {
                Log.e("LocatOldPerVM", "Cuidador no conectado")
                return
            }
            else -> {
                Log.d("LocatOldPerVM", "Iniciando rastreo de ubicación")
                locationCallback = locationHandler.registerLocationUpdates { latLng ->
                    Log.d("LocatOldPerVM", "Nueva ubicación recibida: $latLng")
                    updateLocation(latLng)
                }
            }
        }
    }

    /**
     * Actualiza la ubicación en el estado local y en Firebase
     */
    private fun updateLocation(latLng: LatLng) {
        Log.i("LocatOldPerVM", "Intentando actualizar ubicación: $latLng")
        _uiLocState.update { it.copy(location = latLng) }

        when (val entity = _uiLocState.value.currentEntity) {
            null -> {
                Log.e("LocatOldPerVM", "No hay cuidador para actualizar ubicación")
                return
            }
            else -> {
                viewModelScope.launch {
                    try {
                        repositorioUsuarios.actualizarUbicacionCuidador(entity.userID, latLng)
                        Log.i("LocatOldPerVM", "Ubicación actualizada en Firebase: $latLng")
                    } catch (e: Exception) {
                        Log.e("LocatOldPerVM", "Error al actualizar ubicación en Firebase: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    fun stopLocationUpdate(locationHandler: LocationHandler) {
        locationCallback?.let { callback ->
            locationHandler.stopLocationUpdates(callback)
            locationCallback = null
            Log.d("LocatOldPerVM", "Rastreo de ubicación detenido")
        }
    }

    /**
     * Limpia las rutas de los ancianos
     */
    fun clearAllRoutes() {

    }

    fun setInitialCameraMoveDone(done: Boolean) {
        _uiLocState.update { it.copy(isInitialCameraMoveDone = done) }
    }

    /**
     * Inicia/detiene actualizaciones de ubicación según el permiso y el estado de conexión
     */
    fun gestionarActualizacionesUbicacion(locationHandler: LocationHandler) {
        viewModelScope.launch {
            val uiState = uiLocState.value
            when {
                uiState.currentEntity == null -> {
                    Log.d("LocatOldPerVM", "Esperando entidad...")
                }
                !uiState.isPermissionGranted -> {
                    Log.d("LocatOldPerVM", "Esperando permisos...")
                }
                uiState.currentEntity?.conectado == true -> {
                    Log.i("LocatOldPerVM", "Iniciando actualizaciones - Usuario: ${uiState.currentEntity!!.nombre}")
                    startLocationUpdates(locationHandler)
                }
                else -> {
                    Log.i("LocatOldPerVM", "Deteniendo actualizaciones - Usuario desconectado")
                    stopLocationUpdate(locationHandler)
                }
            }
        }
    }

    /**
     * Limpia los recursos cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
    }

    fun cancelarServicio(locationHandler: LocationHandler) {
        stopLocationUpdate(locationHandler) // Detiene las actualizaciones de ubicación
        Log.d("LocatOldPerVM", "Servicio cancelado y recursos liberados")
    }
}
