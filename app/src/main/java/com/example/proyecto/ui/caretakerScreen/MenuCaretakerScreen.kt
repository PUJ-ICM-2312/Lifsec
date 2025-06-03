package com.example.proyecto.ui.caretakerScreen

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.Screen
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.elderlyScreens.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.elderlyScreens.LocationCaretakerScreen
import com.example.proyecto.ui.elderlyScreens.MainScreen
import com.example.proyecto.ui.elderlyScreens.ReminderListScreen
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.MenuCareTakerViewModel

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuCaretakersScreen(
    navController: NavController,
    menuCareTakerViewModel: MenuCareTakerViewModel,
    authViewModel: AuthViewModel,
    activityViewModel: ActivityViewModel,
    repositorioUsuarios: RepositorioUsuarios
) {

    val context = LocalContext.current
    val notificationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    )

    LaunchedEffect(Unit) {
        notificationPermissionState.launchMultiplePermissionRequest()
    }

    val recompositionKey by menuCareTakerViewModel.cambio

    val currentEntity by authViewModel.currentEntity.collectAsState()

    val conectadoState = remember(currentEntity?.conectado) {
        mutableStateOf(currentEntity?.conectado ?: false)
    }

    val scope = rememberCoroutineScope()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBarHome(authViewModel: AuthViewModel) {
        TopAppBar(
            title = {
                Text(
                    text = "Abuelo",
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
                Switch(
                    checked = conectadoState.value,
                    onCheckedChange = { newValue ->
                        conectadoState.value = newValue
                        scope.launch {
                            currentEntity?.let { entity ->
                                try {
                                    repositorioUsuarios.actualizarEstadoConexion(
                                        usuarioId = entity.userID,
                                        esAnciano = false,
                                        conectado = newValue
                                    )
                                    Log.d("MenuCaretakersScreen", "Actualizado: ${entity.userID} -> $newValue")
                                } catch (e: Exception) {
                                    Log.e("MenuCaretakersScreen", "Error: ${e.message}")
                                }
                            } ?: Log.e("MenuCaretakersScreen", "currentEntity es null")
                        }
                    }
                )


            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBarCareTaker(navController,menuCareTakerViewModel) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                TopBarHome(authViewModel)

                key(recompositionKey) {

                    if(menuCareTakerViewModel.leerApartado() == "Ubicacion"){

                        LocationOldPersonScreen(authViewModel)

                    }else if(menuCareTakerViewModel.leerApartado() == "Actividades"){

                        ActivitiesCaretaker(navController, activityViewModel)

                    }else if(menuCareTakerViewModel.leerApartado() == "Recordatorios"){

                        RemindersCaretakerScreen()

                    }
                }

            }

        }
    }
}




@Composable
fun BottomNavigationBarCareTaker(navController: NavController, menuCareTakerViewModel:MenuCareTakerViewModel) {
    val recompositionKey by menuCareTakerViewModel.cambio

    key(recompositionKey) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Ubicación") },
                label = { Text(text = "Ubicación", style = MaterialTheme.typography.labelSmall) },

                selected = menuCareTakerViewModel.apartado == "Ubicacion" ,
                onClick = {
                    menuCareTakerViewModel.cambiarApartado("Ubicacion")
                    menuCareTakerViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                )
            )

            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Recordatorios") },
                label = { Text(text = "Recordatorios", style = MaterialTheme.typography.labelSmall) },
                selected = menuCareTakerViewModel.apartado == "Recordatorios" ,
                onClick = {
                    menuCareTakerViewModel.cambiarApartado("Recordatorios")
                    menuCareTakerViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,

                    )
            )

            NavigationBarItem(
                icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Actividades") },
                label = { Text(text = "Actividades", style = MaterialTheme.typography.labelSmall) },
                selected = menuCareTakerViewModel.apartado == "Actividades" ,
                onClick = {
                    menuCareTakerViewModel.cambiarApartado("Actividades")
                    menuCareTakerViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                )
            )
        }
    }


}

