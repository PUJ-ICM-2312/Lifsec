package com.example.proyecto.ui.theme

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SharedViewModel : ViewModel() {
    var capturedImage by mutableStateOf<Bitmap?>(null)
}