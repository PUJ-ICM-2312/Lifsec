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

    private val _currentEntity = MutableStateFlow<Usuario?>(null)
    val currentEntity: StateFlow<Usuario?> = _currentEntity.asStateFlow()

    // callback para manejar actualizaciones de ubicación
    private var locationCallback: LocationCallback? = null

    // Mapa para almacenar las listas de puntos de recorrido para cada usuario
    private val userPathsMap = mutableMapOf<String, MutableList<LatLng>>()

    private val _cuidadoresConectados = MutableStateFlow<List<Cuidador>>(emptyList())
    val cuidadoresConectados: StateFlow<List<Cuidador>> = _cuidadoresConectados.asStateFlow()

    init {
        // Nos suscribimos a los cambios de la entidad en AuthViewModel
        viewModelScope.launch {
            authViewModel.currentEntity.collect { entity ->
                Log.d("LocatCareVM", "Entidad actual cambiada: $entity")
                _uiLocState.update { currentState ->
                    currentState.copy(currentEntity = entity)
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

/*
    @SuppressLint("MissingPermission")
    fun registerLocationUpdates(callback: (LatLng) -> Unit): LocationCallback {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)

                    // Generar marcadores de cuidadores una sola vez
                    val currentMarkers = _uiLocState.value.caretakerMarkers
                    val newMarkers = if (currentMarkers.isEmpty()) {
                        val base = latLng
                        List(caretakersCount) {
                            MarkerState(
                                position = LatLng(
                                    base.latitude + Random.nextDouble(-0.005, 0.005),
                                    base.longitude + Random.nextDouble(-0.005, 0.005)
                                )
                            )
                        }
                    } else currentMarkers

                    // Si tenemos nuevos marcadores, también generamos colores para ellos
                    val currentColors = _uiLocState.value.caretakerRouteColors
                    val newColors = if (currentMarkers.isEmpty()) {
                        // Generar colores aleatorios para cada marcador
                        newMarkers.associate { marker ->
                            marker to Color(
                                Random.nextFloat().coerceIn(0.1f, 0.9f),
                                Random.nextFloat().coerceIn(0.1f, 0.9f),
                                Random.nextFloat().coerceIn(0.1f, 0.9f)
                            )
                        }
                    } else currentColors

                    // Actualizar el estado con ubicación y marcadores
                    _uiLocState.value = _uiLocState.value.copy(
                        location = latLng,
                        caretakerMarkers = newMarkers,
                        caretakerRouteColors = newColors
                    )

                    callback(latLng)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }
*/

    fun setCameraPosition(newLatLng: LatLng) {
        _uiLocState.update { currentState ->
            currentState.copy(
                cameraPositionState = CameraPositionState(
                    CameraPosition.fromLatLngZoom(newLatLng, 15f)
                )
            )
        }
    }

/*    fun unregisterLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    *//**
     * Trae la ruta deserializada de google maps entre dos puntos.
     *//*
    suspend fun getRouteBetweenPoints(origin: LatLng, destination: LatLng): List<LatLng> {
        return routesService.getRoutePoints(origin, destination)
    }*/

    /**
     * Carga y guarda la ruta para un cuidador específico
     */
/*    fun loadRouteForCaretaker(markerState: MarkerState, destination: LatLng?) {
        if (destination == null) return

        viewModelScope.launch {
            val route = routesService.getRoutePoints(markerState.position, destination)

            // Actualizamos el mapa de rutas con la nueva ruta
            val updatedRoutes = _uiLocState.value.caretakerRoutes.toMutableMap()
            updatedRoutes[markerState] = route

            _uiLocState.value = _uiLocState.value.copy(
                caretakerRoutes = updatedRoutes
            )
        }
    }*/

    /**
     * Carga rutas para todos los cuidadores
     */
/*    fun loadAllCaretakerRoutes(destination: LatLng?) {
        if (destination == null) return

        for (marker in _uiLocState.value.caretakerMarkers) {
            loadRouteForCaretaker(marker, destination)
        }
    }*/

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

    /**
     * Convierte coordenadas en dirección en texto.
     */
/*    fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/

    fun setInitialCameraMoveDone(done: Boolean) {
        _uiLocState.update { it.copy(isInitialCameraMoveDone = done) }
    }

    /**
     * Limpia los recursos cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
    }
}
