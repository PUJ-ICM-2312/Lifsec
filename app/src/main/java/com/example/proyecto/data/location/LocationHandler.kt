package com.example.proyecto.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHandler(private val context: Context) {

    internal val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la ubicación actual del usuario de manera asíncrona (una sola vez).
     *
     * @param onSuccess Callback con la LatLng si la obtención es exitosa.
     * @param onError Callback con una excepción si ocurre un error o el permiso es denegado.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onSuccess: (LatLng) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Check for permission first (though the screen should also do this)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onError(SecurityException("Location permission not granted."))
            return
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(LatLng(location.latitude, location.longitude))
            } else {
                onError(Exception("Failed to get location. Location was null."))
            }
        }.addOnFailureListener { exception ->
            onError(exception)
        }
    }

    /**
     * Registra actualizaciones de ubicación continuas.
     */
    @SuppressLint("MissingPermission")
    fun registerLocationUpdates(callback: (LatLng) -> Unit): LocationCallback {
        // Check for permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider throwing an exception or returning an error state
            // For simplicity here, just logging and returning a dummy callback, but this should be handled robustly.
            println("Location permission not granted for updates.")
            return object : LocationCallback() {} // Return a no-op callback
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // Intervalo de actualización en ms
        ).setWaitForAccurateLocation(true) // Optional: wait for a more accurate location initially
            .setMinUpdateIntervalMillis(2000) // Optional: minimum interval
            .setMaxUpdateDelayMillis(10000)    // Optional: maximum batching delay
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    Log.i("LocationHandler", "Ubicación recibida: $latLng") // Log adicional
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

    /**
     * Detiene las actualizaciones de ubicación.
     */
    fun stopLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
