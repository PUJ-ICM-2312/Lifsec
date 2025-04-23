package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.ui.theme.SOSButtonColor
import com.example.proyecto.ui.theme.SOSBackgroundColor
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SOSScreen(navController: NavController) {
    var emergencyType by remember { mutableStateOf(TextFieldValue("")) }
    var emergencyMessage by remember { mutableStateOf(TextFieldValue("")) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centra todo verticalmente
    ) {
        Text(
            text = "Cuidadores Activos: 0",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón SOS con imagen grande
        Box(
            modifier = Modifier
                .size(250.dp) // Hacemos el botón más grande
                .clip(RoundedCornerShape(24.dp))
                .background(SOSBackgroundColor)
                .clickable {
                    scope.launch {
                        snackbarHostState.showSnackbar("¡Mensaje de emergencia enviado!")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.sos_icon),
                contentDescription = "Botón de emergencia SOS",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(200.dp) // Asegura que la imagen no quede muy pequeña
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Hacemos los campos un poco más pequeños para centrar mejor
        ) {
            OutlinedTextField(
                value = emergencyType,
                onValueChange = { emergencyType = it },
                label = { Text("Tipo de emergencia (opcional)") },
                modifier = Modifier.fillMaxWidth(),


            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = emergencyMessage,
                onValueChange = { emergencyMessage = it },
                label = { Text("Mensaje (opcional)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
