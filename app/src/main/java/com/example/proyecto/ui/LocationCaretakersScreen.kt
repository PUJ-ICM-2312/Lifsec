package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import com.example.proyecto.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun LocationCaretakerScreen(){
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.oldpersonlocationplaceholder),
            contentDescription = "Mock ubicación de cuidadores",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
