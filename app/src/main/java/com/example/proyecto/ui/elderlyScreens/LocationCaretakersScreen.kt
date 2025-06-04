package com.example.proyecto.ui.elderlyScreens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.data.location.LocationHandler
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
    val cuidadoresConectados by locatCareViewModel.cuidadoresConectados.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationHandler = remember { LocationHandler(context) }

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

    LaunchedEffect(uiLocState.cuidadores) {
        locatCareViewModel.processUserLocations(uiLocState.cuidadores)
    }

    // Iniciar observación de cuidadores cuando tengamos el ID del anciano
    LaunchedEffect(uiLocState.currentEntity?.userID) {
        uiLocState.currentEntity?.userID?.let { ancianoId ->
            locatCareViewModel.observarCuidadoresConectados(ancianoId)
            Log.d("LocationScreen", "Iniciando observación de cuidadores para anciano: $ancianoId")
        }
    }


    // Configuramos el launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locatCareViewModel.onPermissionResult(granted)
    }

    // Verificar permisos al iniciar la pantalla
    LaunchedEffect(Unit) {
        if (!locatCareViewModel.handleLocationPermission(context)) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Iniciar/detener actualizaciones de ubicación según el permiso y el estado de conexión
    LaunchedEffect(uiLocState.isPermissionGranted, uiLocState.currentEntity) {
        when {
            uiLocState.currentEntity == null -> {
                Log.d("MapScreen", "Esperando entidad...")
            }
            !uiLocState.isPermissionGranted -> {
                Log.d("MapScreen", "Esperando permisos...")
            }
            uiLocState.currentEntity?.conectado == true -> {
                Log.i("MapScreen", "Iniciando actualizaciones - Usuario: ${uiLocState.currentEntity!!.nombre}")
                locatCareViewModel.startLocationUpdates(locationHandler)
            }
            else -> {
                Log.i("MapScreen", "Deteniendo actualizaciones - Usuario desconectado")
                locatCareViewModel.stopLocationUpdate(locationHandler)
            }
        }
    }
    // Animar cámara cuando cambie la ubicación
    LaunchedEffect(uiLocState.location) {
        uiLocState.location?.let { loc ->
            uiLocState.cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(loc, 15f),
                durationMs = 1000
            )
            locatCareViewModel.setInitialCameraMoveDone(true)
        }
    }

    // Limpieza al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            locatCareViewModel.stopLocationUpdate(locationHandler)
        }
    }

    // Reiniciamos el contador cuando cambia el estado de emergencia
    LaunchedEffect(isEmergency) {
        if (isEmergency) {
            caretakersArrived = 0
            remainingRoutes.clear()
        }
    }

    if (!uiLocState.isPermissionGranted) {
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
                    val userIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)

                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Mi ubicación",
                        snippet = "Estoy aquí",
                        icon = userIcon
                    )
                }

                // Marcadores de cuidadores
                cuidadoresConectados.forEach { cuidador ->
                    val position = LatLng(cuidador.latLng.latitude, cuidador.latLng.longitude)

                    val caretakerIcon = remember(context) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    }

                    Marker(
                        state = MarkerState(position),
                        title = cuidador.nombre,
                        snippet = "Cuidador conectado",
                        icon = caretakerIcon
                    )
                }

                // Cuidadores y sus rutas
                uiLocState.caretakerMarkers.forEach { (userId, markerState) ->
                    val caretakerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

                    Marker(
                        state = markerState,
                        title = "Cuidador",
                        snippet = "Ruta hacia el anciano",
                        icon = caretakerIcon
                    )

                    // Mostrar la ruta si hay emergencia
                    if (isEmergency) {
                        val routePoints = uiLocState.caretakerRoutes[userId] ?: emptyList()
                        val routeColor = uiLocState.caretakerRouteColors[userId] ?: Color.Blue

                        if (routePoints.isNotEmpty()) {
                            Polyline(
                                points = routePoints,
                                clickable = false,
                                width = 8f,
                                color = routeColor
                            )
                        } else {
                            Log.w("LocationScreen", "No hay puntos de ruta para el cuidador: ${markerState.position}")
                        }
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

//@Composable
//fun AnimatedMarker(
//    markerState: MarkerState,
//    baseLocation: LatLng,
//    durationMs: Int = 10000,
//    isEmergency: Boolean
//) {
//    // usamos rememberUpdatedState para leer siempre el último valor
//    val emergencyState by rememberUpdatedState(newValue = isEmergency)
//    val animLat = remember { Animatable(markerState.position.latitude.toFloat()) }
//    val animLng = remember { Animatable(markerState.position.longitude.toFloat()) }
//    val scope = rememberCoroutineScope()
//
//    // Usamos un estado estable para la posición para evitar parpadeos
//    val stablePosition = remember { mutableStateOf(markerState.position) }
//
//    LaunchedEffect(emergencyState) {
//        // Si hay emergencia no iniciamos el loop de movimiento aleatorio
//        if (emergencyState) return@LaunchedEffect
//
//        while (isActive && !emergencyState) {
//            val nextLat = (baseLocation.latitude + Random.nextDouble(-0.005, 0.005)).toFloat()
//            val nextLng = (baseLocation.longitude + Random.nextDouble(-0.005, 0.005)).toFloat()
//
//            scope.launch {
//                animLat.animateTo(nextLat, animationSpec = tween(durationMillis = durationMs))
//            }
//            scope.launch {
//                animLng.animateTo(nextLng, animationSpec = tween(durationMillis = durationMs))
//            }
//
//            delay(durationMs.toLong())
//        }
//    }
//
//    // Actualizamos el estado estable en cada recomposición
//    stablePosition.value = LatLng(
//        animLat.value.toDouble(),
//        animLng.value.toDouble()
//    )
//
//    // Actualizamos la posición del marker solo cuando cambia el valor estable
//    // Esto reduce la frecuencia de actualización y evita parpadeos
//    LaunchedEffect(stablePosition.value) {
//        markerState.position = stablePosition.value
//    }
//}
