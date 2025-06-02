package com.example.proyecto.ui.elderlyScreens

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue


import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.Screen
import com.example.proyecto.data.HuellaData
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.LocatCareViewModel
import com.example.proyecto.ui.viewmodel.MenuOldPersonViewModel
import com.example.proyecto.ui.viewmodel.ReminderViewModel
import com.example.proyecto.ui.viewmodel.internalStorageViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.pow
import kotlin.math.sqrt


@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuOldPersonScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    menuOldPersonViewModel: MenuOldPersonViewModel,
    activityViewModel: ActivityViewModel,
    reminderViewModel: ReminderViewModel,
    locatCareViewModel: LocatCareViewModel,
    internalViewModel: internalStorageViewModel = viewModel()
) {
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

    // Verifica que currentEntity esté configurado correctamente
    LaunchedEffect(authViewModel.currentAnciano) {
        if (authViewModel.currentAnciano == null && authViewModel.isAnciano()) {
            Log.i("MenuOldPersonScreen", "Cargando datos del usuario...")
            authViewModel.getCurrentState()
        }
    }

    // Solo verificar la huella si hay usuario activo
    var huellaEqualsUser by remember(currentUser?.uid) {
        mutableStateOf(
            currentEntity?.let {
                internalViewModel.huellaIgualAUser(context, authViewModel.currentEntity.value?.email )
            } ?: false
        )

    }
    var mostrarCard by remember { mutableStateOf(true) }
    val notificationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    )

    LaunchedEffect(Unit) {
        notificationPermissionState.launchMultiplePermissionRequest()
    }


    //escucah activamente al cambio
    val recompositionKey by menuOldPersonViewModel.cambio




    // Implementación del shake detector
    DisposableEffect(navController) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            private var lastUpdate = 0L
            private var lastX = 0f
            private var lastY = 0f
            private var lastZ = 0f
            private val shakeThreshold = 800
            private var lastShakeTime = 0L // Para evitar múltiples activaciones

            override fun onSensorChanged(event: SensorEvent) {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) > 100) {
                    val diffTime = (currentTime - lastUpdate).toFloat()
                    lastUpdate = currentTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt(
                        (x - lastX).pow(2) +
                                (y - lastY).pow(2) +
                                (z - lastZ).pow(2)
                    ) / diffTime * 10000

                    // Agregamos debouncing (2 segundos entre shakes)
                    if (speed > shakeThreshold && currentTime - lastShakeTime > 2000) {

                        lastShakeTime = currentTime
                        // Navegación directa a SOS
                        navController.navigate(Screen.SosScreen.route) {
                            // Opcional: limpia el back stack
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    LaunchedEffect(Unit) {
        notificationPermissionState.launchMultiplePermissionRequest()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(menuOldPersonViewModel) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (huellaEqualsUser) {
                    Log.i("MenuOldPersonScreen", "Huella coincide con el usuario")
                    // Carga el grafo de navegación interno

                    // Card flotante con datos de huella solo si ambas condiciones son true
//                    if (mostrarCard) {
//                        var huellaData: HuellaData = internalViewModel.leerJsonHuella(context)
//                        Card(
//                            modifier = Modifier
//                                .padding(16.dp)
//                                .align(Alignment.Center),
//                            elevation = CardDefaults.cardElevation(8.dp),
//                            shape = RoundedCornerShape(16.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(16.dp),
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                Text(
//                                    text = "Datos de Huella",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    fontWeight = FontWeight.Bold
//                                )
//                                Text(text = "Contraseña: ${huellaData.contra}")
//                                Text(text = "Correo: ${huellaData.correo}")
//
//                                Button(
//                                    onClick = { mostrarCard = false },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.error
//                                    )
//                                ) {
//                                    Text("Descartar", color = MaterialTheme.colorScheme.onError)
//                                }
//                            }
//                        }
//                    }else{

                        key(recompositionKey) {
                            if(menuOldPersonViewModel.leerApartado() == "Menu"){

                                MainScreen(navController, authViewModel)

                            }else if(menuOldPersonViewModel.leerApartado() == "Ubicacion"){

                                LocationCaretakerScreen(
                                    locatCareViewModel = locatCareViewModel,
                                    authViewModel = authViewModel,
                                    navController = navController
                                )

                            }else if(menuOldPersonViewModel.leerApartado() == "Actividades"){

                                ListActivitiesOldPersonScreen(activityViewModel,navController)

                            }else if(menuOldPersonViewModel.leerApartado() == "Recordatorios"){

                                ReminderListScreen(navController, reminderViewModel)

                            }
                        }




                } else {
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
                                                    mostrarCard = false
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
    }



}


@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Observar el estado del usuario. Si cambia a null, navegar.
    val currentUser by authViewModel.currentUser.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBarHome(authViewModel)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MenuCard(
                        title = "Notificar Emergencia",
                        icon = Icons.Default.Warning,
                        onClick = {
                            navController.navigate(Screen.SosScreen.route)
                        }
                    )
                    MenuCard(
                        title = "Registrar Actividad",
                        icon = Icons.Default.Create,
                        onClick = {
                            navController.navigate(Screen.CreateActivity.route)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MenuCard(
                        title = "Crear Recordatorio",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            navController.navigate(Screen.CreateReminder.route)
                        }
                    )
                    MenuCard(
                        title = "Ajustes",
                        icon = Icons.Default.Settings,
                        onClick = {
                            navController.navigate(Screen.ConfigScreenElder.route)
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(authViewModel: AuthViewModel) {
    TopAppBar(
        title = {
            Text(
                text = "Cuidadores Activos: 0",
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


@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 140.dp, height = 160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        // Contenido de la Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun BottomNavigationBar(menuOldPersonViewModel: MenuOldPersonViewModel){
    val recompositionKey by menuOldPersonViewModel.cambio

    key(recompositionKey) {
        // Observar la entrada actual del back stack
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            NavigationBarItem(

                icon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                label = {
                    Text(
                        text = "Menú",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                },
                selected = menuOldPersonViewModel.apartado == "Menu" ,
                onClick = {
                    menuOldPersonViewModel.cambiarApartado("Menu")
                    menuOldPersonViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.height(56.dp)
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                label = {
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                },
                selected = menuOldPersonViewModel.apartado == "Ubicacion" ,
                onClick = {
                    menuOldPersonViewModel.cambiarApartado("Ubicacion")
                    menuOldPersonViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.height(56.dp)
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Actividades",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                label = {
                    Text(
                        text = "Actividades",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                },
                selected = menuOldPersonViewModel.apartado == "Actividades" ,
                onClick = {
                    menuOldPersonViewModel.cambiarApartado("Actividades")
                    menuOldPersonViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.height(56.dp)
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Recordatorios",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                label = {
                    Text(
                        text = "Recordatorios",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                },
                selected = menuOldPersonViewModel.apartado == "Recordatorios" ,
                onClick = {
                    menuOldPersonViewModel.cambiarApartado("Recordatorios")
                    menuOldPersonViewModel.generarCambio()
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.background,
                    unselectedTextColor = MaterialTheme.colorScheme.background,
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier.height(56.dp)
            )
        }
    }
}
