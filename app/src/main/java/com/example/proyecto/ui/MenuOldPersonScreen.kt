package com.example.proyecto.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.InternalNavegationStack
import com.example.proyecto.InternalScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuOldPersonScreen(navController: NavController) {
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


    // NavController interno para la navegación anidada
    val internalNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(internalNavController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            // Aquí se carga el grafo de navegación interno
            InternalNavegationStack(navController = internalNavController)
        }
    }
}


@Composable
fun MainScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBarHome()

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
                            navController.navigate(InternalScreen.SosScreen.route)
                        }
                    )
                    MenuCard(
                        title = "Registrar Actividad",
                        icon = Icons.Default.Create,
                        onClick = {
                            navController.navigate(InternalScreen.CreateActivity.route)
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
                            navController.navigate(InternalScreen.CreateReminder.route)
                        }
                    )
                    MenuCard(
                        title = "Ajustes",
                        icon = Icons.Default.Settings,
                        onClick = {
                            // Acción al hacer clic
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome() {
    // Usamos TopAppBar de Material3 para definir el topBar
    // TODO: Colocar icono de usuario al extremo derecho
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
        )
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
fun BottomNavigationBar(navController: NavController) {
    // Observar la entrada actual del back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Función interna para verificar si la ruta está seleccionada
    fun isSelected(route: String) = currentRoute == route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            },
            selected = isSelected(InternalScreen.MainScreen.route),
            onClick = { navController.navigate(InternalScreen.MainScreen.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                unselectedTextColor = MaterialTheme.colorScheme.background,
                selectedIconColor = MaterialTheme.colorScheme.surface,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
            ),
            modifier = Modifier.height(56.dp)
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            },
            selected = isSelected(InternalScreen.LocationCaretaker.route),
            onClick = { navController.navigate(InternalScreen.LocationCaretaker.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                unselectedTextColor = MaterialTheme.colorScheme.background,
                selectedIconColor = MaterialTheme.colorScheme.surface,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
            ),
            modifier = Modifier.height(56.dp)
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Actividades",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Actividades",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            },
            selected = isSelected(InternalScreen.ActivityList.route),
            onClick = { navController.navigate(InternalScreen.ActivityList.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                unselectedTextColor = MaterialTheme.colorScheme.background,
                selectedIconColor = MaterialTheme.colorScheme.surface,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
            ),
            modifier = Modifier.height(56.dp)
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Recordatorios",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Recordatorios",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            },
            selected = isSelected(InternalScreen.ReminderList.route),
            onClick = { navController.navigate(InternalScreen.ReminderList.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.background,
                unselectedTextColor = MaterialTheme.colorScheme.background,
                selectedIconColor = MaterialTheme.colorScheme.surface,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
            ),
            modifier = Modifier.height(56.dp)
        )
    }
}




