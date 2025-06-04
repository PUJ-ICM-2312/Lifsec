package com.example.proyecto.ui.elderlyScreens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.Screen
import com.example.proyecto.R
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.SharedImageViewModel

@Composable
fun CreateActivityScreen(
    navController: NavController,
    sharedImageViewModel: SharedImageViewModel,
    activityViewModel: ActivityViewModel
) {
    var activityText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var additionalInfoText by remember { mutableStateOf("") }
    var imageSelected by remember { mutableStateOf<Bitmap?>(null) }

    val url by activityViewModel.ultimaImagenUrl.collectAsState()
    val isLoading by activityViewModel.isLoading.collectAsState()

    imageSelected = sharedImageViewModel.capturedImage

    LaunchedEffect(imageSelected) {
        activityText = sharedImageViewModel.actividad
        locationText = sharedImageViewModel.ubicacion
        additionalInfoText = sharedImageViewModel.infoAdicional
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear Actividad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                InputField(label = "¿Qué hiciste?", value = activityText) {
                    activityText = it
                    sharedImageViewModel.actividad = activityText
                }

                InputField(label = "¿Dónde fue?", value = locationText) {
                    locationText = it
                    sharedImageViewModel.ubicacion = locationText
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Agregar foto",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clickable { navController.navigate(Screen.CamaraActivityScreen.route) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageSelected == null) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_placeholder),
                            contentDescription = "Seleccionar imagen",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Image(
                            bitmap = imageSelected!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                InputField(label = "Información adicional (opcional)", value = additionalInfoText) {
                    additionalInfoText = it
                    if (additionalInfoText.isNotBlank()) {
                        sharedImageViewModel.infoAdicional = additionalInfoText
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            activityViewModel.guardarActividadCompleta(
                                imagen = imageSelected,
                                actividad = activityText,
                                ubicacion = locationText,
                                infoAdicional = additionalInfoText.takeIf { it.isNotBlank() }
                            ) {
                                sharedImageViewModel.capturedImage = null
                                sharedImageViewModel.actividad = ""
                                sharedImageViewModel.ubicacion = ""
                                sharedImageViewModel.infoAdicional = ""
                                navController.popBackStack()
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }

        if (isLoading) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Guardando actividad...") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Por favor espera...")
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),

        )
    }
}
