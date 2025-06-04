package com.example.proyecto.ui.elderlyScreens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.Screen
import com.example.proyecto.ui.viewmodel.ReminderViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel
import java.util.*

@Composable
fun CreateReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel,
) {
    var titleText by remember { mutableStateOf("") }
    var additionalInfoText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    // Observa el estado de carga del ViewModel
    val isSaving by viewModel.isLoading.collectAsState()

    // Lanza el efecto para cargar el anciano actual antes de usarlo
    LaunchedEffect(Unit) {
        viewModel.cargarAncianoActualDesdeJson(context)
        viewModel.relaunch()
    }

    // Navegar cuando termine de guardar (de isSaving = true -> false)
    LaunchedEffect(isSaving) {
        if (!isSaving && titleText.isNotBlank() && selectedDate.isNotBlank()) {
            // Solo navegamos si acabamos de guardar (isSaving pasó de true a false)

        }
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
                    "Crear Recordatorio",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Recordatorioinfo(label = "Título", value = titleText) {
                    titleText = it
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Fecha", fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedDate.isEmpty()) "Seleccionar fecha" else selectedDate,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Recordatorioinfo(
                    label = "Información adicional (opcional)",
                    value = additionalInfoText
                ) {
                    additionalInfoText = it
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (titleText.isNotBlank() && selectedDate.isNotBlank()) {
                                viewModel.addRecordatorio(
                                    titulo = titleText,
                                    fecha = selectedDate,
                                    infoAdicional = additionalInfoText.ifBlank { null }
                                )
                                navController.popBackStack()
                            } else {
                                Toast
                                    .makeText(context, "Campos obligatorios no rellenados", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        },
                        enabled = !isSaving && titleText.isNotBlank() && selectedDate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun Recordatorioinfo(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}