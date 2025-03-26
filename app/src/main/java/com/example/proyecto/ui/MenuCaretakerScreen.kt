package com.example.proyecto.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.InternalNavegationStack
import com.example.proyecto.InternalCaretakerRoutes
import com.example.proyecto.InternalCaretakerRoutesStack
import com.example.proyecto.ui.theme.secondaryContainerLight

@Composable
fun MenuCaretakersScreen(navController: NavController) {
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
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Ubicación") },
            label = { Text(text = "Ubicación", style = MaterialTheme.typography.labelSmall) },
            selected = isSelected(InternalCaretakerRoutes.LocationOldPerson.route),
            onClick = { navController.navigate(InternalCaretakerRoutes.LocationOldPerson.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = secondaryContainerLight,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Recordatorios") },
            label = { Text(text = "Recordatorios", style = MaterialTheme.typography.labelSmall) },
            selected = isSelected(InternalCaretakerRoutes.RemindersCaretaker.route),
            onClick = { navController.navigate(InternalCaretakerRoutes.RemindersCaretaker.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = secondaryContainerLight,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        NavigationBarItem(
            icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Actividades") },
            label = { Text(text = "Actividades", style = MaterialTheme.typography.labelSmall) },
            selected = isSelected(InternalCaretakerRoutes.ActivitiesCaretaker.route),
            onClick = { navController.navigate(InternalCaretakerRoutes.ActivitiesCaretaker.route) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = secondaryContainerLight,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

