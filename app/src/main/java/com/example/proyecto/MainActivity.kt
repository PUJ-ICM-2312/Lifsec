package com.example.proyecto

import android.os.Build
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.ui.viewmodel.SensorService

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Arranca aquí tu SensorService en primer plano
        Intent(this, SensorService::class.java).also { intent ->
            ContextCompat.startForegroundService(this, intent)
        }

        enableEdgeToEdge()
        setContent {
            ProyectoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegationStack()
                }

            }
        }
    }
}

