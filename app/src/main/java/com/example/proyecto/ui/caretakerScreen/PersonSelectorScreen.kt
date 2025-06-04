package com.example.proyecto.ui.caretakerScreen

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.R
import com.example.proyecto.Screen
import com.example.proyecto.data.Anciano
import com.example.proyecto.data.Cuidador
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.AncianoDelCuidadorViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.internalStorageViewModel
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    internalViewModel: internalStorageViewModel,
    ancianoDelCuidadorViewModel: AncianoDelCuidadorViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentEntity by authViewModel.currentEntity.collectAsState()

    var isAdding by remember { mutableStateOf(false) }
    var isLoadingEmail by remember { mutableStateOf(false) }




    if (currentEntity !is Cuidador) return
    val cuidador = currentEntity as Cuidador

    // 1) Cada vez que cambie cuidador.ancianosIds, recreamos la lista interna
    val listaIdsAncianos = remember(cuidador.ancianosIds) {
        mutableStateListOf<String>().apply { addAll(cuidador.ancianosIds) }
    }


    // 2) Usuario no autenticado → redirigir
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            Log.i("UserListScreen", "Usuario no autenticado, navegando a Login")
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Verificando autenticación...")
        }
        return
    }

    // 3) Huella (no cambia aquí)
    var huellaEqualsUser by remember(currentUser?.uid) {
        mutableStateOf(
            currentEntity.let {
                internalViewModel.huellaIgualAUser(
                    context,
                    authViewModel.currentEntity.value?.email
                )
            } ?: false
        )
    }

    // 4) Cada vez que cambie el tamaño de la lista, recargamos el ViewModel
    LaunchedEffect(listaIdsAncianos.size) {
        if (listaIdsAncianos.isNotEmpty()) {
            ancianoDelCuidadorViewModel.setAncianoIds(listaIdsAncianos)
        }
    }

    // 5) Observamos la lista y el estado de carga
    val ancianosList by ancianoDelCuidadorViewModel.ancianos.collectAsState()
    val isLoadingAncianos by ancianoDelCuidadorViewModel.isLoading.collectAsState()

    // 6) Diálogo “Agregar nuevo anciano”
    var showAddDialog by remember { mutableStateOf(false) }
    var newAncianoEmail by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            if (huellaEqualsUser) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Text("Agregar")
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (huellaEqualsUser) {
                // 7) Lista de ancianos
                LazyColumn {
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outline
                        ) {
                            HeaderG(authViewModel)
                        }
                    }

                    if (isLoadingAncianos) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    items(ancianosList) { anciano ->
                        UserListItem(grandP = anciano, navController = navController, ancianoDelCuidadorViewModel)
                    }
                }
            } else {
                // 8) Pantalla de solicitud de huella…
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "¿Desea que este usuario tenga la huella?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Button(
                                    onClick = {
                                        internalViewModel.guardarJsonHuella(
                                            context,
                                            authViewModel.currentEntity.value?.email ?: "",
                                            authViewModel.password
                                        )
                                        huellaEqualsUser = true
                                    }
                                ) {
                                    Text(text = "Sí", fontSize = 14.sp)
                                }
                                Button(
                                    onClick = {
                                        huellaEqualsUser = true
                                        authViewModel.getCurrentState()
                                    }
                                ) {
                                    Text(text = "No", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 9) Diálogo para ingresar nuevo ID de anciano
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("Agregar nuevo Anciano") },
                    text = {
                        Column {
                            Text(
                                "Ingrese el email del usuario",
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newAncianoEmail,
                                onValueChange = { newAncianoEmail = it },
                                label = { Text("email Anciano") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newAncianoEmail.isNotBlank() && !isAdding && !isLoadingEmail) {
                                    showAddDialog = false
                                    isAdding = true
                                    isLoadingEmail = true

                                    coroutineScope.launch {
                                        val newAncianoId = ancianoDelCuidadorViewModel.obtenerIdAncianoPorEmail(newAncianoEmail)

                                        if (!newAncianoId.isNullOrBlank()) {
                                            try {
                                                Firebase.firestore
                                                    .collection("cuidadores")
                                                    .document(cuidador.userID)
                                                    .update("ancianosIds", FieldValue.arrayUnion(newAncianoId))
                                                    .await()

                                                listaIdsAncianos.add(newAncianoId)
                                                ancianoDelCuidadorViewModel.setAncianoIds(listaIdsAncianos)
                                                newAncianoEmail = ""

                                            } catch (e: Exception) {
                                                Log.e("UserListScreen", "Error al agregar anciano: ${e.message}")
                                            }
                                        } else {
                                            // Aquí podrías mostrar un snackbar o alerta de "email no válido"
                                            Log.e("UserListScreen", "No se encontró anciano con ese email")
                                        }

                                        isAdding = false
                                        isLoadingEmail = false
                                    }
                                }
                            },
                            enabled = newAncianoEmail.isNotBlank() && !isAdding && !isLoadingEmail
                        ) {
                            if (isAdding || isLoadingEmail) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Agregar")
                            }
                        }
                    }
                    ,

                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
// Composable para crear el header de la lista
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderG(authViewModel: AuthViewModel ) {
    TopAppBar(
        title = {
            Text(
                text = "A quien quieres cuidar?",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.outline
        ),
        actions = {
            // User icon button at the end (trailing)
            IconButton(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    )

}

//Composable para crear item en la lista, se usa ListItem de Material3
@Composable
fun UserListItem(grandP: Anciano, navController: NavController, ancianoDelCuidadorViewModel: AncianoDelCuidadorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navega a la pantalla principal del cuidador
                ancianoDelCuidadorViewModel.currentAnciano = grandP
                navController.navigate(Screen.MenuCaretaker.route)
            }
    ) {
        ListItem(
            modifier = Modifier.padding(5.dp),
            leadingContent = {
                Image(
                    painter = painterResource(id = R.drawable.ejemploperfil),
                    contentDescription = "",
                    modifier = Modifier.size(100.dp)
                )
            },
            headlineContent = {
                Text(
                    text = "${grandP.nombre} ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            supportingContent = {
                Text(
                    text = "${grandP.email} años",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            trailingContent = {
                Icon(Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Gray)
            }
        )
        HorizontalDivider(thickness = 1.dp)
    }
}



