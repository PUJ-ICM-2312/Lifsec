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
import kotlin.compareTo
import kotlin.text.get

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
                        actualizarRutasCuidadores(cuidadores)
                    }
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al observar cuidadores: ${e.message}")
            }
        }
    }

    private fun actualizarMarcadoresCuidadores(cuidadores: List<Cuidador>) {
        val currentRoutes = _uiLocState.value.caretakerRoutes
        val currentColors = _uiLocState.value.caretakerRouteColors
        val currentEntity = _uiLocState.value.currentEntity
        val currentLocation = _uiLocState.value.location

        viewModelScope.launch {
            try {
                val newMarkers = mutableMapOf<String, MarkerState>()
                val newRoutes = currentRoutes.toMutableMap()
                val newColors = currentColors.toMutableMap()

                if (currentEntity?.emergencia == true && currentLocation != null) {
                    cuidadores.forEach { cuidador ->
                        val markerState = MarkerState(position = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude))
                        newMarkers[cuidador.userID] = markerState

                        val shouldUpdateRoute = !currentRoutes.containsKey(cuidador.userID)

                        if (shouldUpdateRoute) {
                            val route = RoutesService().getRoutePoints(markerState.position, currentLocation)
                            if (route.isNotEmpty()) {
                                newRoutes[cuidador.userID] = route
                                newColors[cuidador.userID] = currentColors[cuidador.userID] ?: Color(
                                    red = Random.nextInt(100, 256),
                                    green = Random.nextInt(100, 256),
                                    blue = Random.nextInt(100, 256)
                                )
                            }
                        }
                    }
                }

                _uiLocState.update {
                    it.copy(
                        caretakerMarkers = newMarkers,
                        caretakerRoutes = newRoutes,
                        caretakerRouteColors = newColors
                    )
                }
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al actualizar marcadores: ${e.message}")
            }
        }
    }

    /**
     * Limpia las rutas de los cuidadores
     */
    fun clearAllRoutes() {
        _uiLocState.update {
            it.copy(
                caretakerRoutes = emptyMap(),
                caretakerRouteColors = emptyMap()
            )
        }
    }

    /**
     * Carga las rutas de los cuidadores hacia el anciano en caso de emergencia
     */
    private fun cargarRutasCuidadores() {
        val currentEntity = _uiLocState.value.currentEntity ?: return
        val currentLocation = _uiLocState.value.location ?: return

        if (!currentEntity.emergencia) return

        viewModelScope.launch {
            try {
                val routesService = RoutesService()
                val newRoutes = mutableMapOf<String, List<LatLng>>()
                val newRouteColors = _uiLocState.value.caretakerRouteColors.toMutableMap()

                _uiLocState.value.caretakerMarkers.forEach { (userId, markerState) ->
                    val origin = markerState.position
                    val route = routesService.getRoutePoints(origin, currentLocation)
                    if (route.isNotEmpty()) {
                        newRoutes[userId] = route

                        // Reutilizar el color existente o generar uno nuevo si no existe
                        newRouteColors[userId] = newRouteColors[userId] ?: Color(
                            red = Random.nextInt(100, 256),
                            green = Random.nextInt(100, 256),
                            blue = Random.nextInt(100, 256)
                        )
                    }
                }

                _uiLocState.update {
                    it.copy(
                        caretakerRoutes = newRoutes,
                        caretakerRouteColors = newRouteColors
                    )
                }

                Log.d("LocatCareVM", "Rutas cargadas para cuidadores: ${newRoutes.size}")
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al cargar rutas de cuidadores: ${e.message}")
            }
        }
    }

    init {
        Log.d("LocatCareVM", "Init del ViewModel ejecutado")
        viewModelScope.launch {
            authViewModel.currentEntity.collect { entity ->
                Log.d("LocatCareVM", "Cambio en currentEntity: $entity")
                if (entity is Anciano && entity.emergencia) {
                    Log.d("LocatCareVM", "Anciano en emergencia, cargando rutas de cuidadores")
                    cargarRutasCuidadores()
                } else {
                    Log.d("LocatCareVM", "No hay emergencia, limpiando rutas")
                    clearAllRoutes()
                }
            }
        }
        setCameraPosition(LatLng(4.60971, -74.08175))
        Log.d("LocatCareVM", "Posición de cámara inicial establecida")
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
                val cuidadoresCercanos = _cuidadoresConectados.value.filter { cuidador ->
                    val distance = calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cuidador.latLng.latitude, cuidador.latLng.longitude
                    )
                    distance <= 10000
                }

                // Primero obtener las rutas
                val routesService = RoutesService()
                val newRoutes = mutableMapOf<MarkerState, List<LatLng>>()
                val newColors = mutableMapOf<MarkerState, Color>()

                if (currentEntity.emergencia) {
                    cuidadoresCercanos.forEach { cuidador ->
                        val origin = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude)
                        val markerState = MarkerState(position = origin)
                        val route = routesService.getRoutePoints(origin, currentLocation)

                        if (route.isNotEmpty()) {
                            newRoutes[markerState] = route
                            newColors[markerState] = Color(
                                red = Random.nextInt(100, 256),
                                green = Random.nextInt(100, 256),
                                blue = Random.nextInt(100, 256)
                            )
                        }
                    }
                }

                // Actualizar todo el estado de una vez
                val newMarkers = cuidadoresCercanos.associate { cuidador ->
                    cuidador.userID to MarkerState(position = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude))
                }

                _uiLocState.update { state ->
                    state.copy(
                        caretakerMarkers = newMarkers
                    )
                }

                Log.d("LocatCareVM", "Cuidadores cercanos encontrados: ${cuidadoresCercanos.size}")
                Log.d("LocatCareVM", "Rutas generadas: ${newRoutes.size}")
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error: ${e.message}")
            }
        }
    }

    fun actualizarRutasCuidadores(cuidadores: List<Cuidador>) {
        val currentLocation = _uiLocState.value.location ?: return
        val currentRoutes = _uiLocState.value.caretakerRoutes.toMutableMap()
        val currentColors = _uiLocState.value.caretakerRouteColors.toMutableMap()

        viewModelScope.launch {
            try {
                val routesService = RoutesService()

                cuidadores.forEach { cuidador ->
                    val userId = cuidador.userID
                    val newPosition = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude)
                    val previousMarkerState = _uiLocState.value.caretakerMarkers[userId]

                    if (previousMarkerState != null) {
                        val previousPosition = previousMarkerState.position
                        val distance = calculateDistance(
                            previousPosition.latitude, previousPosition.longitude,
                            newPosition.latitude, newPosition.longitude
                        )

                        if (distance > 10) {
                            // Eliminar la ruta anterior
                            currentRoutes.remove(userId)
                            currentColors.remove(userId)

                            // Crear nueva ruta
                            val newRoute = routesService.getRoutePoints(newPosition, currentLocation)
                            if (newRoute.isNotEmpty()) {
                                currentRoutes[userId] = newRoute
                                currentColors[userId] = currentColors[userId] ?: Color(
                                    red = Random.nextInt(100, 256),
                                    green = Random.nextInt(100, 256),
                                    blue = Random.nextInt(100, 256)
                                )
                            }
                        }
                    }
                }

                _uiLocState.update {
                    it.copy(
                        caretakerRoutes = currentRoutes,
                        caretakerRouteColors = currentColors
                    )
                }

                Log.d("LocatCareVM", "Rutas actualizadas para cuidadores")
            } catch (e: Exception) {
                Log.e("LocatCareVM", "Error al actualizar rutas de cuidadores: ${e.message}")
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
