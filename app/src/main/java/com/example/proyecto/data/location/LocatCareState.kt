package com.example.proyecto.data.location

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState

data class LocatCareState(
    val location: LatLng? = null,
    val cameraPositionState: CameraPositionState = CameraPositionState(),
    val isInitialCameraMoveDone: Boolean = false,
    val caretakerMarkers: List<MarkerState> = emptyList(),
    val caretakerRoutes: Map<MarkerState, List<LatLng>> = emptyMap(),
    val caretakerRouteColors: Map<MarkerState, Color> = emptyMap()
)