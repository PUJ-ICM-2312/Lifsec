package com.example.proyecto.ui.elderlyScreens

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.Screen
import com.example.proyecto.ui.showNotification
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.LocatCareViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.google.maps.android.SphericalUtil

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationCaretakerScreen(
    locatCareViewModel: LocatCareViewModel = viewModel(),
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val uiLocState by locatCareViewModel.uiLocState.collectAsStateWithLifecycle()
    val isEmergency by authViewModel.emergencia.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    LocationPermissionHandler { hasPermission = true }

    // Control para rastrear los cuidadores que han llegado a su destino
    var caretakersArrived by rememberSaveable { mutableIntStateOf(0) }
    val totalCaretakers = uiLocState.caretakerMarkers.size

    // Estado para almacenar las rutas restantes por cada cuidador
    val remainingRoutes = remember { mutableStateMapOf<MarkerState, List<LatLng>>() }

    // Caché de direcciones por marcador
    val addressCache = remember { mutableStateMapOf<MarkerState, String?>() }

    // Función para manejar la llegada de un cuidador
    val handleCaretakerArrival = {
        caretakersArrived++
        // Si todos los cuidadores han llegado, desactivamos la emergencia
        if (caretakersArrived >= totalCaretakers && totalCaretakers > 0) {
            caretakersArrived = 0
            authViewModel.setEmergencia(false)
            Log.i("Emergency", "Todos los cuidadores han llegado. Emergencia desactivada.")
            showNotification(
                context = context,
                title = "Emergencia en progreso",
                content = "Todos los cuidadores han llegado a su ubicacion"
            )
        }
    }

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

    // Reiniciamos el contador cuando cambia el estado de emergencia
    LaunchedEffect(isEmergency) {
        if (isEmergency) {
            caretakersArrived = 0
            remainingRoutes.clear()
        }
    }

    // Control de emergencia - carga rutas cuando el estado de emergencia cambia
    LaunchedEffect(isEmergency, uiLocState.location) {
        if (isEmergency && uiLocState.location != null) {
            locatCareViewModel.loadAllCaretakerRoutes(uiLocState.location)
            uiLocState.caretakerMarkers.forEach { markerState ->
                uiLocState.caretakerRoutes[markerState]?.takeIf { it.isNotEmpty() }?.let { route ->
                    remainingRoutes[markerState] = route
                }
            }
        } else {
            locatCareViewModel.clearAllRoutes()
            remainingRoutes.clear()
        }
    }

    //Lanzamos un solo efecto que, cada minuto, recalcule todas las direcciones
    LaunchedEffect(uiLocState.caretakerMarkers) {
        while (isActive) {
            uiLocState.caretakerMarkers.forEach { markerState ->
                // llama a tu función de geocodificación
                val addr = locatCareViewModel.getAddressFromLatLng(
                    context,
                    markerState.position.latitude,
                    markerState.position.longitude
                )
                addressCache[markerState] = addr
            }
            delay(60_000L) // 1 minuto
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

    Scaffold (
        topBar = {
            LocationTopBar(
                isEmergency = isEmergency,
                navController = navController
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            GoogleMap(
                properties = MapProperties(
                    mapStyleOptions = if (isSystemInDarkTheme()) MapStyleOptions.loadRawResourceStyle(
                        context,
                        R.raw.dark_map_style
                    ) else null,
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true
                ),
                cameraPositionState = uiLocState.cameraPositionState
            ) {
                // Mi ubicación
                uiLocState.location?.let { currentLocation ->
                    // Creamos un ícono personalizado a partir de un recurso drawable
                    val userIcon = remember(context) { // Recordamos para eficiencia
                        BitmapDescriptorFactory.fromResource(R.drawable.caretaker)
                        // Si el recurso no existe, usa el marcador predeterminado
                            ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }

                    Marker(
                        state = remember { MarkerState(position = currentLocation) },
                        title = "Mi ubicación",
                        snippet = "Estoy aquí",
                        icon = userIcon
                    )
                }

                // Cuidadores y sus rutas
                uiLocState.caretakerMarkers.forEachIndexed { index, markerState ->

                    // Visualizamos los cuidadores
                    val caretakerIcon = remember(context) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    }

                    Marker(
                        state = markerState,
                        title = "Cuidador #${index + 1}",
                        snippet = addressCache[markerState] ?: "Obteniendo dirección…",
                        icon = caretakerIcon
                    )

                    // Si hay emergencia, mostramos la ruta almacenada en el estado
                    if (isEmergency && uiLocState.location != null) {
                        // Obtenemos la ruta y color del estado
                        val routePoints = remainingRoutes[markerState] ?: emptyList()
                        val routeColor = uiLocState.caretakerRouteColors[markerState] ?: Color.Blue

                        if (routePoints.isNotEmpty()) {
                            // Dibujamos solo la ruta restante
                            Polyline(
                                points = routePoints,
                                clickable = false,
                                width = 8f,
                                color = routeColor
                            )

                            // Animamos el marcador a lo largo de la ruta
                            LaunchedEffect(isEmergency, key2 = markerState) {
                                val initialRoute =
                                    uiLocState.caretakerRoutes[markerState] ?: emptyList()
                                if (initialRoute.isNotEmpty()) {
                                    animateMarkerAlongRoute(
                                        markerState = markerState,
                                        route = initialRoute,
                                        onCompleted = handleCaretakerArrival,
                                        onRouteUpdate = { remaining ->
                                            // Actualizamos la ruta restante en el mapa
                                            remainingRoutes[markerState] = remaining
                                        }
                                    )
                                }
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTopBar(
    isEmergency: Boolean,
    navController: NavController
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEmergency) "EMERGENCIA ACTIVA" else "Ubicación",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Screen.MenuOldPerson.route) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver al menú",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.outline
        )
    )
}

suspend fun animateMarkerAlongRoute(
    markerState: MarkerState,
    route: List<LatLng>,
    totalDuration: Long = 60_000L,  // ms
    onCompleted: () -> Unit = {},
    onRouteUpdate: (List<LatLng>) -> Unit = {}
) {
    if (route.size < 2) return

    // 1. Distancia total (en metros)
    val segmentDistances = route.zipWithNext { a, b ->
        SphericalUtil.computeDistanceBetween(a, b)
    }
    val totalDistance = segmentDistances.sum()

    // 2. Velocidad constante (m/ms)
    val speed = totalDistance / totalDuration.toDouble()

    // 3. Variables de control
    val remainingRoute = route.toMutableList()
    onRouteUpdate(remainingRoute)

    val startTime = System.currentTimeMillis()
    var travelled = 0.0

    while (true) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed >= totalDuration) break

        travelled = speed * elapsed  // metros recorridos hasta ahora

        // 4. Determinar en qué segmento estamos y la fracción dentro de él
        var acc = 0.0
        var index = 0
        while (index < segmentDistances.size && acc + segmentDistances[index] < travelled) {
            acc += segmentDistances[index]
            index++
        }

        // Si ya nos pasamos del último, salimos
        if (index >= segmentDistances.size) break

        // fracción dentro del segmento actual
        val inSegDist = travelled - acc
        val fraction = (inSegDist / segmentDistances[index]).coerceIn(0.0, 1.0)

        // 5. Interpolar LatLng
        val start = route[index]
        val end = route[index + 1]
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        markerState.position = LatLng(lat, lng)

        // 6. Construir ruta restante una sola vez por frame
        val remaining = mutableListOf<LatLng>(markerState.position)
        remaining.addAll(route.subList(index + 1, route.size))
        onRouteUpdate(remaining)

        delay(16)  // ~60fps
    }

    // Aseguramos el final
    markerState.position = route.last()
    onRouteUpdate(emptyList())
    onCompleted()
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

    // Usamos un estado estable para la posición para evitar parpadeos
    val stablePosition = remember { mutableStateOf(markerState.position) }

    LaunchedEffect(emergencyState) {
        // Si hay emergencia no iniciamos el loop de movimiento aleatorio
        if (emergencyState) return@LaunchedEffect

        while (isActive && !emergencyState) {
            val nextLat = (baseLocation.latitude + Random.nextDouble(-0.005, 0.005)).toFloat()
            val nextLng = (baseLocation.longitude + Random.nextDouble(-0.005, 0.005)).toFloat()

            scope.launch {
                animLat.animateTo(nextLat, animationSpec = tween(durationMillis = durationMs))
            }
            scope.launch {
                animLng.animateTo(nextLng, animationSpec = tween(durationMillis = durationMs))
            }

            delay(durationMs.toLong())
        }
    }

    // Actualizamos el estado estable en cada recomposición
    stablePosition.value = LatLng(
        animLat.value.toDouble(),
        animLng.value.toDouble()
    )

    // Actualizamos la posición del marker solo cuando cambia el valor estable
    // Esto reduce la frecuencia de actualización y evita parpadeos
    LaunchedEffect(stablePosition.value) {
        markerState.position = stablePosition.value
    }
}