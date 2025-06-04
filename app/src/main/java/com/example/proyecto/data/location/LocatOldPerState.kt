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
    val oldPersonRoutes: Map<MarkerState, List<LatLng>> = emptyMap(),
    val oldPersonRouteColors: Map<MarkerState, Color> = emptyMap(),
    val currentEntity: Cuidador? = null,
    val cuidadores: List<Cuidador> = emptyList(), // Lista de cuidadores no relacionados
    val otherCaretakerMarkers: List<MarkerState> = emptyList() // Marcadores de cuidadores no relacionados
)
