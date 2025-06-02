package com.example.proyecto.ui.elderlyScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.viewmodel.AuthViewModel

// Pantalla de Lista de Cuidadores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaretakersConfigScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    repositorioUsuarios: RepositorioUsuarios
) {
    val currentAnciano = authViewModel.currentAnciano
    var cuidadores by remember { mutableStateOf<List<Cuidador>>(emptyList()) }

    // Cargar cuidadores usando el Flow de RepositorioUsuarios
    LaunchedEffect(currentAnciano) {
        currentAnciano?.let { anciano ->
            repositorioUsuarios.getCuidadoresByIdsFlow(anciano.cuidadoresIds).collect { lista ->
                cuidadores = lista
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(cuidadores) { cuidador ->
                CaretakerListItem(
                    cuidador = cuidador,
                    modifier = Modifier.padding(8.dp)
                )
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun CaretakerListItem(cuidador: Cuidador, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    cuidador.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            supportingContent = {
                Text(
                    cuidador.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }
}

