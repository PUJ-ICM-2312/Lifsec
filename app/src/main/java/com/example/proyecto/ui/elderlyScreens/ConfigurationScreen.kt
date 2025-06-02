package com.example.proyecto.ui.elderlyScreens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import com.example.proyecto.Screen
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreenElder(
    navController: NavController,
    authViewModel: AuthViewModel,
    repositorioUsuarios: RepositorioUsuarios
) {
    var conectado by remember { mutableStateOf(authViewModel.currentAnciano?.conectado ?: false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authViewModel.currentEntity) {
        conectado = authViewModel.currentAnciano?.conectado ?: false
        Log.d("ConfigScreen", "Conectado: ${authViewModel.currentEntity.value?.conectado}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.MenuOldPerson.route) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                title = { Text(if (conectado) "Conectado" else "Desconectado") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Switch(
                            checked = conectado,
                            onCheckedChange = { newValue ->
                                scope.launch {
                                    authViewModel.currentAnciano?.let { anciano ->
                                        try {
                                            repositorioUsuarios.actualizarEstadoConexion(
                                                usuarioId = anciano.userID,
                                                esAnciano = true,
                                                conectado = newValue
                                            )
                                            conectado = newValue
                                            Log.d("ConfigScreen", "Usuario ID: ${anciano.userID}, Conectado: $newValue")
                                        } catch (e: Exception) {
                                            Log.e("ConfigScreen", "Error al actualizar estado: ${e.message}")
                                        }
                                    }
                                }
                            }
                        )
                        IconButton(onClick = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesión"
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Lista de Cuidadores Card
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Lista de Cuidadores",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.CaretakersConfigScreen.route)
                    }
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            // Plan de aplicación Card
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Plan de aplicación actual",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.AppPlanScreen.route)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfigurationScreenElderPreview() {
    ProyectoTheme {
        ConfigurationScreenElder(navController = rememberNavController(), authViewModel = AuthViewModel(), repositorioUsuarios = RepositorioUsuarios())
    }
}

