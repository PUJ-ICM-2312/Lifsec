package com.example.proyecto.ui.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class   SharedImageViewModel : ViewModel() {
    var capturedImage by mutableStateOf<Bitmap?>(null)
}