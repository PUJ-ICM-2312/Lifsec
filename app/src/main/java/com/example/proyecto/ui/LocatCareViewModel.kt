package com.example.proyecto.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.proyecto.data.location.LocatCareState
import com.example.proyecto.data.location.RoutesService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

                    // Actualizar el estado con ubicación y marcadores
                    _uiLocState.value = _uiLocState.value.copy(
                        location = latLng,
                        caretakerMarkers = newMarkers
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
            com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(newLatLng, 15f)
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
        val routesService = RoutesService()
        return routesService.getRoutePoints(origin, destination)
    }

    //TODO: Metodo para cargar de la BD los cuidadores y colocarlos en el mapa

    //TODO: Metodo para cargar actividades en el mapa

    init {
        setCameraPosition(LatLng(4.60971, -74.08175))
    }
}
