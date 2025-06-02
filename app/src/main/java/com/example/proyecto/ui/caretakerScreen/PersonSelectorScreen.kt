package com.example.proyecto.ui.caretakerScreen

import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.internalStorageViewModel
import kotlinx.parcelize.Parcelize


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(navController: NavController, authViewModel: AuthViewModel, internalViewModel: internalStorageViewModel){


    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentEntity by authViewModel.currentEntity.collectAsState()

    // Si el usuario no está autenticado, no mostrar nada y navegar a Login
    if (currentUser == null) {
        // Navegación segura fuera del árbol de composición
        LaunchedEffect(Unit) {
            Log.i("MenuOldPersonScreen", "Usuario no autenticado, navegando a Login")
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
        // Opcional: indicador de carga
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Verificando autenticación...")
        }
        return
    }


    var huellaEqualsUser by remember(currentUser?.uid) {
        mutableStateOf(
            currentEntity?.let {
                internalViewModel.huellaIgualAUser(context, authViewModel.currentEntity.value?.email )
            } ?: false
        )

    }
    val grandparents = ListStarter()
    Surface (
        color = MaterialTheme.colorScheme.background
    ) {

        if (huellaEqualsUser){
            LazyColumn {
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colorScheme.outline
                    ) {
                        HeaderG(authViewModel)
                    }
                }

                items(grandparents) { grandParent ->
                    // Aquí mostramos la info de cada abuelo
                    UserListItem(grandP = grandParent, navController)
                }

            }
        }else {
            // Pantalla de solicitud de huella
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Log.i("MenuOldPersonScreen", "Huella no coincide con el usuario")
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
                                    internalViewModel.guardarJsonHuella(context, authViewModel.email, authViewModel.password)
                                    huellaEqualsUser = true
                                }
                            ) {
                                Text(text = "Sí", fontSize = 14.sp)
                            }
                            Button(
                                onClick = { huellaEqualsUser = true
                                    authViewModel.getCurrentState()}
                            ) {
                                Text(text = "No", fontSize = 14.sp)
                            }
                        }
                    }
                }
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
fun UserListItem(grandP: GrandParent, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navega a la pantalla principal del cuidador
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
                    text = "${grandP.firstName} ${grandP.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            supportingContent = {
                Text(
                    text = "${grandP.age} años",
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


@Parcelize
data class GrandParent(
    var firstName: String,
    var lastName: String,
    var age: Int,
    var gender: String,
    var phone: String,
    var height: Double,
    var weight: Double,

    ):Parcelable

fun ListStarter(): List<GrandParent>{

    var grandParent1 = GrandParent(firstName = "Simon", lastName = "Monroy", age = 70, gender = "M", phone = "314 3164108", height = 1.76, 67.1 )
    var grandParent2 = GrandParent(firstName = "Jorge", lastName = "Sierra", age = 73, gender = "M", phone = "302 8540107", height = 1.81, 77.5 )

    val lista: List<GrandParent> = listOf(grandParent1, grandParent2)

    return lista
}

