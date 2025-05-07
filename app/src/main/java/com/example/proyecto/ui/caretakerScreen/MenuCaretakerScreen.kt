package com.example.proyecto.ui.caretakerScreen

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.InternalCaretakerRoutesStack
import com.example.proyecto.Screen

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuCaretakersScreen(navController: NavController) {
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

    val internalNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBarCareTaker(internalNavController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            InternalCaretakerRoutesStack(navController = internalNavController)
        }
    }
}




@Composable
fun BottomNavigationBarCareTaker(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun isSelected(route: String) = currentRoute == route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Ubicación") },
            label = { Text(text = "Ubicación", style = MaterialTheme.typography.labelSmall) },
            selected = isSelected(Screen.LocationOldPerson.route),
            onClick = { navController.navigate(Screen.LocationOldPerson.route) },
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
            selected = isSelected(Screen.RemindersCaretaker.route),
            onClick = { navController.navigate(Screen.RemindersCaretaker.route) },
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
            selected = isSelected(Screen.ActivitiesCaretaker.route),
            onClick = { navController.navigate(Screen.ActivitiesCaretaker.route) },
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

