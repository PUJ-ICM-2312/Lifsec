package com.example.proyecto.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.Usuario
import com.example.proyecto.data.location.LocatCareState
import com.example.proyecto.data.location.LocationHandler
import com.example.proyecto.data.location.RoutesService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.Anciano

/**
 * ViewModel que controla la ubicación y genera los marcadores de cuidadores.
 * @param context Context de Android.
 * @param caretakersCount Número de cuidadores a generar (parametrizable).
 */
class LocatCareViewModel(
    context: Context,
    private val authViewModel: AuthViewModel,
    private val RepositorioUsuario: RepositorioUsuarios,
    private val caretakersCount: Int = 5
) : ViewModel() {
/*    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)*/

    /*private val routesService = RoutesService()*/
    private val _uiLocState = MutableStateFlow(LocatCareState())
    val uiLocState: StateFlow<LocatCareState> = _uiLocState.asStateFlow()

    private val _currentEntity = MutableStateFlow<Anciano?>(null)
    val currentEntity: StateFlow<Anciano?> = _currentEntity.asStateFlow()

    // callback para manejar actualizaciones de ubicación
    private var locationCallback: LocationCallback? = null

    // Mapa para almacenar las listas de puntos de recorrido para cada usuario
    private val userPathsMap = mutableMapOf<String, MutableList<LatLng>>()

    private val _cuidadoresConectados = MutableStateFlow<List<Cuidador>>(emptyList())
    val cuidadoresConectados: StateFlow<List<Cuidador>> = _cuidadoresConectados.asStateFlow()

    init {
        viewModelScope.launch {
            authViewModel.currentEntity.collect { entity ->
                if (entity is Anciano) {
                    Log.d("LocatCareVM", "Entidad actual cambiada: $entity")
                    _uiLocState.update { currentState ->
                        currentState.copy(currentEntity = entity)
                    }
                }
            }
        }

        viewModelScope.launch {
            authViewModel.currentEntity.collect { entity ->
                if (entity is Anciano && entity.emergencia) {
                    obtenerCuidadoresCercanos()
                } else {
                    clearAllRoutes()
                }
            }
        }
    }

    /**
     * Verifica y gestiona el resultado del permiso de ubicación
     */
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
     * Inicia las actualizaciones de ubicación
     */
    fun startLocationUpdates(locationHandler: LocationHandler) {
        when {
            locationCallback != null -> {
                Log.e("LocatCareVM", "Actualizaciones ya registradas")
                return
            }
            !_uiLocState.value.isPermissionGranted -> {
                Log.e("LocatCareVM", "Permisos no concedidos")
                return
            }
            _uiLocState.value.currentEntity == null -> {
                Log.e("LocatCareVM", "No hay usuario actual")
                return
            }
            !_uiLocState.value.currentEntity!!.conectado -> {
                Log.e("LocatCareVM", "Usuario no conectado")
                return
            }
            else -> {
                Log.d("LocatCareVM", "Iniciando rastreo de ubicación")
                locationCallback = locationHandler.registerLocationUpdates { latLng ->
                    Log.d("LocatCareVM", "Nueva ubicación recibida: $latLng")
                    updateLocation(latLng)
                }
            }
        }
    }

    /**
     * Actualiza la ubicación en el estado local y en Firebase
     */
    private fun updateLocation(latLng: LatLng) {
        Log.i("LocatCareVM", "Intentando actualizar ubicación: $latLng")
        _uiLocState.update { it.copy(location = latLng) }

        when (val entity = _uiLocState.value.currentEntity) {
            null -> {
                Log.e("LocatCareVM", "No hay usuario para actualizar ubicación")
                return
            }
            else -> {
                viewModelScope.launch {
                    try {
                        RepositorioUsuario.actualizarUbicacionAnciano(entity.userID, latLng)
                        Log.i("LocatCareVM", "Ubicación actualizada en Firebase: $latLng")
                    } catch (e: Exception) {
                        Log.e("LocatCareVM", "Error al actualizar ubicación en Firebase: ${e.message}")
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
            Log.d("LocatCareVM", "Rastreo de ubicación detenido")
        }
    }

    /**
     * Procesa las ubicaciones de los usuarios para detectar cambios
     * y actualizar sus paths correspondientes
     */
    public fun processUserLocations(users: List<Usuario>) {
        // 1. Calcula el set de usuarios actualmente conectados (excluyendo al propio)
        val activeIds = users
            .filter { it.conectado && it.userID != _currentEntity.value?.userID }
            .map { it.userID }
            .toSet()

        // 2. Limpia rutas de quienes se desconectaron
        val removed = userPathsMap.keys - activeIds
        removed.forEach { id ->
            userPathsMap.remove(id)
        }
    }

    fun setCameraPosition(newLatLng: LatLng) {
        _uiLocState.update { currentState ->
            currentState.copy(
                cameraPositionState = CameraPositionState(
                    CameraPosition.fromLatLngZoom(newLatLng, 15f)
                )
            )
        }
    }

    fun observarCuidadoresConectados(ancianoId: String) {
        viewModelScope.launch {
            try {
                RepositorioUsuario.getCuidadoresConectadosPorAncianoIdFlow(ancianoId)
                    .collect { cuidadores ->
                        _cuidadoresConectados.value = cuidadores
                        actualizarMarcadoresCuidadores(cuidadores)
                    }
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al observar cuidadores: ${e.message}")
            }
        }
    }

    private fun actualizarMarcadoresCuidadores(cuidadores: List<Cuidador>) {
        val newMarkers = cuidadores.map { cuidador ->
            MarkerState(
                position = LatLng(
                    cuidador.latLng.latitude,
                    cuidador.latLng.longitude
                )
            )
        }
        _uiLocState.update { it.copy(caretakerMarkers = newMarkers) }
    }

    /**
     * Limpia las rutas de los cuidadores
     */
    fun clearAllRoutes() {
        _uiLocState.value = _uiLocState.value.copy(
            caretakerRoutes = emptyMap()
        )
    }

    //TODO: Metodo para cargar de la BD los cuidadores y colocarlos en el mapa

    init {
        setCameraPosition(LatLng(4.60971, -74.08175))
    }

    fun setInitialCameraMoveDone(done: Boolean) {
        _uiLocState.update { it.copy(isInitialCameraMoveDone = done) }
    }

    /**
     * Limpia los recursos cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
    }

    /**
     * Obtiene los cuidadores cercanos dentro de un radio de 2km y traza rutas si hay emergencia
     */
    fun obtenerCuidadoresCercanos() {
        val currentEntity = _uiLocState.value.currentEntity ?: return
        val currentLocation = _uiLocState.value.location ?: return

        viewModelScope.launch {
            try {
                // Filtrar cuidadores por distancia
                val cuidadoresCercanos = _cuidadoresConectados.value.filter { cuidador ->
                    val distance = calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cuidador.latLng.latitude, cuidador.latLng.longitude
                    )
                    distance <= 2000 // 2km en metros
                }

                actualizarMarcadoresCuidadores(cuidadoresCercanos)

                // Si el anciano está en emergencia, trazar rutas hacia su ubicación
                if (currentEntity.emergencia) {
                    val routesService = RoutesService()
                    val newRoutes = mutableMapOf<MarkerState, List<LatLng>>()
                    
                    cuidadoresCercanos.forEach { cuidador ->
                        val origin = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude)
                        val route = routesService.getRoutePoints(origin, currentLocation)
                        val markerState = MarkerState(position = origin)
                        newRoutes[markerState] = route
                    }

                    _uiLocState.update { it.copy(caretakerRoutes = newRoutes) }
                }

                Log.d("LocatCareVM", "Cuidadores cercanos encontrados: ${cuidadoresCercanos.size}")
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al obtener cuidadores cercanos: ${e.message}")
            }
        }
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     * @return distancia en metros
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000 // Radio de la Tierra en metros
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val sinDeltaLatSquared = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
        val cosLat1RadCosLat2Rad = Math.cos(lat1Rad) * Math.cos(lat2Rad)
        val sinDeltaLonSquared = Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)
        
        val haversine = sinDeltaLatSquared + cosLat1RadCosLat2Rad * sinDeltaLonSquared
        val angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))

        return earthRadius * angularDistance // Distancia en metros
    }
}
