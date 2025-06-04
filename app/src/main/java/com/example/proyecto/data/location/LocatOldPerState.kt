package com.example.proyecto.data.location

import androidx.compose.ui.graphics.Color
import com.example.proyecto.data.Usuario
import com.example.proyecto.data.Cuidador
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState

data class LocatOldPerState(
    val location: LatLng? = null,
    val isPermissionGranted: Boolean = false,
    val cameraPositionState: CameraPositionState = CameraPositionState(),
    val isInitialCameraMoveDone: Boolean = false,
    val ancianos: List<Usuario> = emptyList(),
    val oldPersonMarkers: List<MarkerState> = emptyList(),
    val caretakerRoute: List<LatLng> = emptyList(), // Ruta del cuidador
    val caretakerRouteColor: Color? = null, // Color de la ruta del cuidador
    val currentEntity: Cuidador? = null,
    val cuidadores: List<Cuidador> = emptyList() // Lista de cuidadores no relacionados
)
