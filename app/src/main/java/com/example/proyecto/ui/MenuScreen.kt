package com.example.proyecto.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.ui.theme.outlineLight

@Composable
fun MenuScreen(navController: NavController) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
        modifier = Modifier
            .fillMaxSize(),


    ) {
        // Header con texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outline) // Color de fondo header
                .padding(16.dp, top = 50.dp, end = 20.dp),

            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cuidadores Activos: 0 ",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 28.sp,
                textAlign = TextAlign.Center ,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background

            )

        }
        // A PARTIR DE ACA FALTA MODIFICAR (FALTA CENTRAR LAS 4 FUNCIONES, AMPLIAR IMAGENES Y CAMBIAR LOS COLORES)
        // Cards en dos filas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Ocupa espacio disponible
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuCard(
                    title = "Notificar Emergencia",

                    icon = Icons.Default.KeyboardArrowRight, // Cambia por un ícono adecuado


                )
                MenuCard(
                    title = "Registrar Actividad",
                    icon = Icons.Default.Create, // Cambia por un ícono adecuado

                )
            }
            Spacer(modifier = Modifier.padding(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuCard(
                    title = "Crear Recordatorio",
                    icon = Icons.Default.Notifications, // Cambia por un ícono adecuado

                )
                MenuCard(
                    title = "Ajustes",
                    icon = Icons.Default.Settings,

                )
            }
        }

        // Barra de navegación inferior
        BottomNavigationBar()
        }

    }
}


@Composable
fun MenuCard(title: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background) // Fondo verde claro
            .clickable { /* accion */ }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(95.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomNavigationBar() {
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
                    maxLines = 1 // Prevenir que el texto se extienda
                )
            },
            selected = false,
            onClick = { /* Acción */ },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            selected = false,
            onClick = { /* Acción */ },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            selected = false,
            onClick = { /* Acción */ },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            selected = false,
            onClick = { /* Acción */ },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.height(56.dp)
        )
    }
}


