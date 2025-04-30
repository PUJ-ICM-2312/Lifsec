package com.example.proyecto.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.location.LocatCareState
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
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel que controla la ubicación y genera los marcadores de cuidadores.
 * @param context Context de Android.
 * @param caretakersCount Número de cuidadores a generar (parametrizable).
 */
class LocatCareViewModel(
    context: Context,
    private val caretakersCount: Int = 5
) : ViewModel() {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val routesService = RoutesService()
    private val _uiLocState = MutableStateFlow(LocatCareState())
    val uiLocState: StateFlow<LocatCareState> = _uiLocState.asStateFlow()

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

    fun setCameraPosition(newLatLng: LatLng) {
        val newCameraPositionState = CameraPositionState(
            CameraPosition.fromLatLngZoom(newLatLng, 15f)
        )
        _uiLocState.value = _uiLocState.value.copy(
            cameraPositionState = newCameraPositionState
        )
    }

    fun unregisterLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Trae la ruta deserializada de google maps entre dos puntos.
     */
    suspend fun getRouteBetweenPoints(origin: LatLng, destination: LatLng): List<LatLng> {
        return routesService.getRoutePoints(origin, destination)
    }

    /**
     * Carga y guarda la ruta para un cuidador específico
     */
    fun loadRouteForCaretaker(markerState: MarkerState, destination: LatLng?) {
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
    }

    /**
     * Carga rutas para todos los cuidadores
     */
    fun loadAllCaretakerRoutes(destination: LatLng?) {
        if (destination == null) return

        for (marker in _uiLocState.value.caretakerMarkers) {
            loadRouteForCaretaker(marker, destination)
        }
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

    //TODO: Metodo para cargar actividades en el mapa

    init {
        setCameraPosition(LatLng(4.60971, -74.08175))
    }
}