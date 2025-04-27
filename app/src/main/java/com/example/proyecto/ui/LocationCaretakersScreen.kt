package com.example.proyecto.ui

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun LocationCaretakerScreen(
    locatCareViewModel: LocatCareViewModel = viewModel(),
    navController: NavController
) {
    // Observe the state
    val uiLocState by locatCareViewModel.uiLocState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val mapProperties = MapProperties(
        mapStyleOptions = if (isSystemInDarkTheme()) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map_style)
        } else {
            null
        },
        isMyLocationEnabled = true
    )

    val mapUiSettings = MapUiSettings(
        myLocationButtonEnabled = true,
        zoomControlsEnabled = true
    )

    var hasPermission by remember { mutableStateOf(false) }
    LocationPermissionHandler {
        hasPermission = true
    }

    var locationCallback: LocationCallback? by remember { mutableStateOf(null) }
    DisposableEffect(hasPermission) {
        Log.d("DisposableEffect", "hasPermission changed: $hasPermission")
        if (hasPermission) {
            locationCallback = locatCareViewModel.registerLocationUpdates { newLocation ->
                Log.i("Location", "New location: $newLocation")
                locatCareViewModel.setCameraPosition(newLocation)
            }
        }

        onDispose {
            Log.d("onDispose", "Unregistering location updates")
            locationCallback?.let { locatCareViewModel.unregisterLocationUpdates(it) }
            locationCallback = null
        }
    }

    var isInitialCameraMoveDone by remember { mutableStateOf(false) }
    LaunchedEffect(uiLocState.location) {
        if (uiLocState.location != null && !isInitialCameraMoveDone) {
            // Anima la cámara a la nueva posición (ubicación actual) con zoom 15
            uiLocState.cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(uiLocState.location!!, 15f),
                durationMs = 1000 // Duración de la animación en milisegundos (opcional)
            )
            isInitialCameraMoveDone = true // Marca que el movimiento inicial ya se hizo
        }
    }

    if (!hasPermission) {
        Text("Se requiere permiso de ubicación para usar el mapa.",
            modifier = Modifier.padding(16.dp).fillMaxSize().statusBarsPadding(),
            textAlign = TextAlign.Center,
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            contentPadding = PaddingValues(
                top = 150.dp
            ),
            properties = mapProperties,
            uiSettings = mapUiSettings,
            cameraPositionState = uiLocState.cameraPositionState
        ) {
            uiLocState.location?.let { currentLocation ->
                val myLocation = currentLocation
                Marker(
                    state = MarkerState(position = myLocation),
                    title = "Mi ubicación",
                    snippet = "Estoy aquí"
                )
            }
        }
    }
}