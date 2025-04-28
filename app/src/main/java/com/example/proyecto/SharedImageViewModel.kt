package com.example.proyecto

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class   SharedImageViewModel : ViewModel() {
    var capturedImage by mutableStateOf<Bitmap?>(null)
}