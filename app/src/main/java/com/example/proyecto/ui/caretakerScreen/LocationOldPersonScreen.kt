package com.example.proyecto.ui.caretakerScreen

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.data.location.LocationHandler
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.LocatOldPerViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationOldPersonScreen(
    locatOldPerViewModel: LocatOldPerViewModel = viewModel(),
    authViewModel: AuthViewModel
) {
    val uiLocState by locatOldPerViewModel.uiLocState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationHandler = remember { LocationHandler(context) }

    // Configuramos el launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locatOldPerViewModel.onPermissionResult(granted)
    }

    // Verificar permisos al iniciar la pantalla
    LaunchedEffect(Unit) {
        if (!locatOldPerViewModel.handleLocationPermission(context)) {
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
                locatOldPerViewModel.startLocationUpdates(locationHandler)
            }
            else -> {
                Log.i("MapScreen", "Deteniendo actualizaciones - Usuario desconectado")
                locatOldPerViewModel.stopLocationUpdate(locationHandler)
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
            locatOldPerViewModel.setInitialCameraMoveDone(true)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true
                ),
                cameraPositionState = uiLocState.cameraPositionState
            ) {
                // Mi ubicación (Cuidador)
                uiLocState.location?.let { currentLocation ->
                    val caretakerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Mi ubicación",
                        snippet = "Estoy aquí",
                        icon = caretakerIcon
                    )
                }

                // Marcadores de ancianos
                uiLocState.oldPersonMarkers.forEach { markerState ->
                    val oldPersonIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    val anciano = uiLocState.ancianos.find { 
                        it.latLng?.let { latLng ->
                            LatLng(latLng.latitude, latLng.longitude) == markerState.position
                        } == true
                    }

                    Marker(
                        state = markerState,
                        title = anciano?.nombre ?: "Anciano",
                        snippet = "Última ubicación conocida",
                        icon = oldPersonIcon
                    )
                }
            }
        }
    }
}
