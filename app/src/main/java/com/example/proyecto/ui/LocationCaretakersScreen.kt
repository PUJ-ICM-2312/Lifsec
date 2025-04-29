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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

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

            // Cuidadores y sus rutas
            uiLocState.caretakerMarkers.forEachIndexed { index, markerState ->
                // Visualizamos los cuidadores
                Marker(
                    state = markerState,
                    title = "Cuidador #${index + 1}"
                )

                // Si hay emergencia, calculamos y mostramos la ruta
                if (isEmergency && uiLocState.location != null) {
                    // Genera un color aleatorio para la ruta (evitando blanco o negro)
                    val lineColor = remember(markerState.position) {
                        Color(
                            Random.nextFloat().coerceIn(0.1f, 0.9f),
                            Random.nextFloat().coerceIn(0.1f, 0.9f),
                            Random.nextFloat().coerceIn(0.1f, 0.9f)
                        )
                    }

                    // Obtenemos los puntos de la ruta
                    val routePoints by produceState(
                        initialValue = emptyList<LatLng>(),
                        key1 = markerState.position,
                        key2 = uiLocState.location,
                        key3 = isEmergency
                    ) {
                        value = locatCareViewModel.getRouteBetweenPoints(
                            origin = markerState.position,
                            destination = uiLocState.location!!
                        )
                    }

                    if (routePoints.isNotEmpty()) {
                        // Dibujamos la ruta
                        Polyline(
                            points = routePoints,
                            clickable = false,
                            width = 8f,
                            color = lineColor
                        )

                        // Animamos el marcador a lo largo de la ruta
                        LaunchedEffect(routePoints, isEmergency) {
                            animateMarkerAlongRoute(
                                markerState = markerState,
                                route = routePoints
                            )
                        }
                    }
                } else {
                    // Si no hay emergencia, animamos el marcador con movimiento aleatorio
                    AnimatedMarker(
                        markerState = markerState,
                        baseLocation = uiLocState.location ?: markerState.position,
                        durationMs = 30000,
                        isEmergency = isEmergency
                    )
                }
            }
        }
    }
}

// Función para animar un marcador a lo largo de una ruta
suspend fun animateMarkerAlongRoute(
    markerState: MarkerState,
    route: List<LatLng>
) {
    // Si la ruta está vacía, no hacemos nada
    if (route.isEmpty()) return

    // Tiempo total para recorrer la ruta (en milisegundos)
    val totalDuration = 60000L // 1 minuto

    // Calculamos el tiempo que debe demorar entre cada punto
    val pointDuration = totalDuration / route.size

    // Animamos el marcador a través de cada punto de la ruta
    for (i in 0 until route.size - 1) {
        val start = route[i]
        val end = route[i + 1]

        // Calculamos la distancia entre los puntos para determinar la duración
        val segmentDuration = (pointDuration * distanceBetween(start, end) / 0.0001).toLong()
            .coerceAtLeast(100) // Al menos 100ms por segmento
            .coerceAtMost(5000) // Máximo 5 segundos por segmento

        // Interpolamos la posición entre los puntos
        val steps = (segmentDuration / 16).toInt().coerceAtLeast(2) // 16ms por frame ~ 60fps

        for (step in 1..steps) {
            val fraction = step.toFloat() / steps
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction

            // Actualizamos la posición del marcador
            markerState.position = LatLng(lat, lng)

            // Pequeña pausa para la animación
            delay(16) // ~60fps
        }
    }

    // Aseguramos que el marcador llegue al punto final
    markerState.position = route.last()
}

// Función para calcular la distancia aproximada entre dos puntos (para uso en animación)
private fun distanceBetween(p1: LatLng, p2: LatLng): Double {
    // Calculamos la distancia euclidiana (suficiente para comparar distancias relativas)
    val dx = p1.latitude - p2.latitude
    val dy = p1.longitude - p2.longitude
    return Math.sqrt(dx * dx + dy * dy)
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(emergencyState) {
        // Si hay emergencia no iniciamos el loop de movimiento aleatorio
        if (emergencyState) return@LaunchedEffect

        while (isActive && !emergencyState) {
            val nextLat = (baseLocation.latitude + Random.nextDouble(-0.005, 0.005)).toFloat()
            val nextLng = (baseLocation.longitude + Random.nextDouble(-0.005, 0.005)).toFloat()

            scope.launch { animLat.animateTo(nextLat, animationSpec = tween(durationMillis = durationMs)) }
            scope.launch { animLng.animateTo(nextLng, animationSpec = tween(durationMillis = durationMs)) }

            delay(durationMs.toLong())
        }
    }

    // Actualizamos la posición en cada frame
    markerState.position = LatLng(
        animLat.value.toDouble(),
        animLng.value.toDouble()
    )
}