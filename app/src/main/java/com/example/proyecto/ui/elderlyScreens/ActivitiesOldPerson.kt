package com.example.proyecto.ui.elderlyScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.R
import com.example.proyecto.data.Actividad
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext



@Composable
fun ListActivitiesOldPersonScreen(
    viewModel: ActivityViewModel = viewModel()
) {
    val showDialog = remember { mutableStateOf(false) }
    val newTitle = remember { mutableStateOf("") }
    val newLocation = remember { mutableStateOf("") }
    val selectedImage = remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            selectedImage.value = bitmap
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(viewModel.activities.size) { index ->
                val activity = viewModel.activities[index]
                ActivityCard(activity = activity)
            }
        }

        // FAB
        androidx.compose.material3.FloatingActionButton(
            onClick = { showDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text("+", color = Color.White, fontSize = 24.sp)
        }

        // Diálogo
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Nueva actividad") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTitle.value,
                            onValueChange = { newTitle.value = it },
                            label = { Text("Título") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newLocation.value,
                            onValueChange = { newLocation.value = it },
                            label = { Text("Ubicación") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Seleccionar imagen")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        selectedImage.value?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Imagen seleccionada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (
                            newTitle.value.isNotBlank() &&
                            newLocation.value.isNotBlank() &&
                            selectedImage.value != null
                        ) {
                            viewModel.addActivity(
                                actividad = newTitle.value,
                                ubicacion = newLocation.value,
                                imagen = selectedImage.value!!
                            )
                            newTitle.value = ""
                            newLocation.value = ""
                            selectedImage.value = null
                            showDialog.value = false
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}




@Composable
fun ActivityCard(activity: Actividad) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ejemploperfil),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = activity.actividad,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = activity.ubicacion,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "hace 5 mins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = androidx.compose.ui.graphics.painter.BitmapPainter(activity.imagen.asImageBitmap()),
                contentDescription = "Activity Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}


