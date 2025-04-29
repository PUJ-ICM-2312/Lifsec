package com.example.proyecto.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.random.Random

@Composable
fun LocationCaretakerScreen(
    locatCareViewModel: LocatCareViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    navController: NavController
) {
    val uiLocState by locatCareViewModel.uiLocState.collectAsStateWithLifecycle()
    val isEmergency by authViewModel.emergencia.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    LocationPermissionHandler { hasPermission = true }

    var locationCallback: LocationCallback? by remember { mutableStateOf(null) }
    DisposableEffect(hasPermission) {
        if (hasPermission) {
            locationCallback = locatCareViewModel.registerLocationUpdates { newLocation ->
                Log.i("Location", "New location: $newLocation")
            }
        }
        onDispose {
            locationCallback?.let { locatCareViewModel.unregisterLocationUpdates(it) }
        }
    }

    // Animar cámara solo una vez
    var isInitialCameraMoveDone by remember { mutableStateOf(false) }
    LaunchedEffect(uiLocState.location) {
        uiLocState.location?.takeIf { !isInitialCameraMoveDone }?.let { loc ->
            uiLocState.cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(loc, 15f),
                durationMs = 1000
            )
            isInitialCameraMoveDone = true
        }

        if (authViewModel.emergencia.value) {
            Log.i("authVM","Emergencia: ${authViewModel.emergencia.value}")
        }
    }

    if (!hasPermission) {
        Text(
            "Se requiere permiso de ubicación para usar el mapa.",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .statusBarsPadding(),
            textAlign = TextAlign.Center
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            contentPadding = PaddingValues(top = 150.dp),
            properties = MapProperties(
                mapStyleOptions = if (isSystemInDarkTheme()) MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map_style) else null,
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = true),
            cameraPositionState = uiLocState.cameraPositionState
        ) {
            // Mi ubicación
            uiLocState.location?.let { currentLocation ->
                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Mi ubicación",
                    snippet = "Estoy aquí"
                )
            }

            // Cuidadores
            uiLocState.caretakerMarkers.forEachIndexed { index, markerState ->
                // Pasamos isEmergency a AnimatedMarker
                AnimatedMarker(
                    markerState = markerState,
                    baseLocation = uiLocState.location ?: markerState.position,
                    durationMs = 30000,
                    isEmergency = isEmergency
                )
                Marker(
                    state = markerState,
                    title = "Cuidador #${index + 1}"
                )
            }

            if (isEmergency && uiLocState.location != null) {
                uiLocState.caretakerMarkers.forEachIndexed { index, markerState ->
                    // Genera un color aleatorio (evitando blanco o negro)
                    val lineColor = remember(markerState.position) {
                        mutableListOf<Color>().apply {
                            var color: Color
                            do {
                                color = Color(
                                    Random.nextFloat().coerceIn(0.1f, 0.9f),
                                    Random.nextFloat().coerceIn(0.1f, 0.9f),
                                    Random.nextFloat().coerceIn(0.1f, 0.9f)
                                )
                            } while (color == Color.Black || color == Color.White)
                            add(color)
                        }.first()
                    }
                    val routePoints by produceState(
                        initialValue = emptyList<LatLng>(),
                        key1 = markerState.position,
                        key2 = uiLocState.location
                    ) {
                        value = locatCareViewModel.getRouteBetweenPoints(
                            origin = markerState.position,
                            destination = uiLocState.location!!
                        )
                    }
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            clickable = false,
                            width = 8f,
                            color = lineColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedMarker(
    markerState: MarkerState,
    baseLocation: LatLng,
    durationMs: Int = 10000,
    isEmergency: Boolean
) {
    // usamos rememberUpdatedState para leer siempre el último valor
    val emergencyState by rememberUpdatedState(newValue = isEmergency)
    val animLat = remember { Animatable(markerState.position.latitude.toFloat()) }
    val animLng = remember { Animatable(markerState.position.longitude.toFloat()) }

    LaunchedEffect(emergencyState) {
        // Si hay emergencia no iniciamos el loop
        if (emergencyState) return@LaunchedEffect
        while (isActive && !emergencyState) {
            val nextLat = (baseLocation.latitude + Random.nextDouble(-0.005, 0.005)).toFloat()
            val nextLng = (baseLocation.longitude + Random.nextDouble(-0.005, 0.005)).toFloat()
            coroutineScope {
                launch {
                    animLat.animateTo(nextLat, animationSpec = tween(durationMillis = durationMs))
                }
                launch {
                    animLng.animateTo(nextLng, animationSpec = tween(durationMillis = durationMs))
                }
            }
        }
    }

    // Actualizamos la posición en cada frame
    markerState.position = LatLng(
        animLat.value.toDouble(),
        animLng.value.toDouble()
    )
}
