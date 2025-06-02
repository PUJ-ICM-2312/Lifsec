package com.example.proyecto.ui.elderlyScreens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// Pantalla de Lista de Cuidadores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakersConfigScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    repositorioUsuarios: RepositorioUsuarios
) {
    val currentAnciano = authViewModel.currentAnciano
    var cuidadores by remember { mutableStateOf<List<Cuidador>>(emptyList()) }
    var mostrarDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Cargar cuidadores usando el Flow
    LaunchedEffect(currentAnciano) {
        currentAnciano?.let { anciano ->
            repositorioUsuarios.getCuidadoresPorAncianoIdFlow(anciano.userID).collect { lista ->
                cuidadores = lista
            }
        }
    }

    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = { Text("Agregar cuidador") },
            text = {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val cuidadorId = repositorioUsuarios.buscarCuidadorPorEmail(email)
                        if (cuidadorId.isNotEmpty()) {
                            currentAnciano?.let { anciano ->
                                val agregado = repositorioUsuarios.agregarCuidadorAnciano(anciano.userID, cuidadorId)
                                if (agregado) {
                                    mostrarDialog = false
                                    email = ""
                                    error = ""
                                } else {
                                    error = "El cuidador ya está asociado"
                                }
                            }
                        } else {
                            error = "Cuidador no encontrado"
                        }
                    }
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialog = false
                    email = ""
                    error = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialog = true },
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Agregar cuidador")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(cuidadores) { cuidador ->
                CaretakerListItem(
                    cuidador = cuidador,
                    modifier = Modifier.padding(8.dp)
                )
                Divider()
            }
        }
    }
}

@Composable
private fun CaretakerListItem(cuidador: Cuidador, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    cuidador.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            supportingContent = {
                Text(
                    cuidador.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }
}

