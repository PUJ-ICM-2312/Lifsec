package com.example.proyecto.ui.elderlyScreens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.Screen
import com.example.proyecto.data.Recordatorio
import com.example.proyecto.ui.viewmodel.ReminderViewModel

@Composable
fun ReminderListScreen(
    navController: NavController,
    viewModel: ReminderViewModel
) {
    val context = LocalContext.current

    // 1) Lanza efectos al componer: carga anciano guardado y recarga recordatorios
    LaunchedEffect(Unit) {
        viewModel.cargarAncianoActualDesdeJson(context)
        viewModel.relaunch()
    }

    // 2) Observa la lista de recordatorios desde el ViewModel
    val reminders by remember { derivedStateOf { viewModel.recordatorios } }

    Column {
        TopBarReminder(navController = navController)


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            items(reminders) { reminder ->
                ReminderListItem(reminder)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReminderListItem(reminder: Recordatorio) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = reminder.titulo,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = reminder.fecha,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            reminder.infoAdicional?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarReminder(

    navController: NavController
) {
    CenterAlignedTopAppBar(

        title = {
            Text(
                text = "Recordatorios",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(onClick = { navController.navigate(Screen.CreateReminder.route) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = MaterialTheme.colorScheme.background,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.outline
        )
    )
}