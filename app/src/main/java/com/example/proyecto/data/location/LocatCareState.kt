package com.example.proyecto.data.location

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState

data class LocatCareState(
    val location: LatLng? = null,
    val cameraPositionState: CameraPositionState = CameraPositionState(),
    val isInitialCameraMoveDone: Boolean = false,
    val caretakerMarkers: List<MarkerState> = emptyList()
)