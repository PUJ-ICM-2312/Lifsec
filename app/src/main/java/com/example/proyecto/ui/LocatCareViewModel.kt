package com.example.proyecto.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.proyecto.data.location.LocatCareState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocatCareViewModel(context: Context) : ViewModel() {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _uiLocState = MutableStateFlow(LocatCareState())
    val uiLocState: StateFlow<LocatCareState> = _uiLocState.asStateFlow()

    /**
     * Obtains the current user's location asynchronously.
     */
    @SuppressLint("MissingPermission")
    fun registerLocationUpdates(callback: (LatLng) -> Unit): LocationCallback {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // Update interval in ms
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiLocState.value = _uiLocState.value.copy(
                        location = latLng
                    )
                    callback(latLng) // Llama al callback con la nueva ubicación
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback // So you can stop updates later
    }
    /**
     * Sets the camera to a new position.
     */
    fun setCameraPosition(newLatLng: LatLng) {
        val newCameraPositionState = CameraPosition.fromLatLngZoom(newLatLng, 15f)
        _uiLocState.value = _uiLocState.value.copy(
            cameraPositionState = CameraPositionState(newCameraPositionState)
        )
    }

    /**
     * Stops location updates using a LocationCallback.
     *
     * @param locationCallback The LocationCallback to remove.
     */
    fun unregisterLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    init {
        setCameraPosition(LatLng(4.60971, -74.08175))
    }
}