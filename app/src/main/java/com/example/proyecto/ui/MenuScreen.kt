package com.example.proyecto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.proyecto.ui.theme.outlineLight

@Composable
fun MenuScreen() {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {  Column(
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
                text = "Cuidadores Activos: 0",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 28.sp,
                textAlign = TextAlign.Center ,
                modifier = Modifier.fillMaxWidth()

            )

        }
        // A PARTIR DE ACA FALTA MODIFICAR (FALTA CENTRAR LAS 4 FUNCIONES, AMPLIAR IMAGENES Y CAMBIAR LOS COLORES)
        // Cards en dos filas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ocupa espacio disponible
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "Notificar Emergencia",
                    description = "Card de emergencia",
                    icon = Icons.Default.KeyboardArrowRight, // Cambia por un ícono adecuado
                    onClick = { /* Acción */ }
                )
                MenuCard(
                    title = "Registrar Actividad",
                    description = "Card de registro actividad",
                    icon = Icons.Default.Create, // Cambia por un ícono adecuado
                    onClick = { /* Acción */ }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "Crear Recordatorio",
                    description = "Card de recordatorios",
                    icon = Icons.Default.Notifications, // Cambia por un ícono adecuado
                    onClick = { /* Acción */ }
                )
                MenuCard(
                    title = "Ajustes",
                    description = "Card de ajustes",
                    icon = Icons.Default.Settings,
                    onClick = { /* Acción */ }
                )
            }
        }

        // Barra de navegación inferior
        BottomNavigationBar()
    }
    }}


@Composable
fun MenuCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background) // Fondo verde claro
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceContainer
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
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu Principal") },
            label = { Text("Menu Principal") },
            selected = true,
            onClick = { /* Acción */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Ubicación") },
            label = { Text("Ubicación") },
            selected = false,
            onClick = { /* Acción */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Actividades") },
            label = { Text("Actividades") },
            selected = false,
            onClick = { /* Acción */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Recordatorios") },
            label = { Text("Recordatorios") },
            selected = false,
            onClick = { /* Acción */ }
        )
    }
}
