package com.example.proyecto.ui

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.InternalScreen
import com.example.proyecto.R
import com.example.proyecto.ui.theme.onSecondaryContainerDark
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.proyecto.ui.theme.secondaryContainerLight
import com.example.proyecto.ui.theme.tertiaryLightMediumContrast


@Parcelize
data class Reminder(
    var title: String,
    var date: LocalDateTime,
    var isDone: Boolean
) : Parcelable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderListScreen(navController: NavController) {
    val reminders = ListStarterReminder()

    Column {
        TopBarReminder(navController,
            onSearchClick = { /*TODO*/ },
            onAddClick = { navController.navigate(InternalScreen.CreateReminder.route) }
        )
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListItem(reminder: Reminder) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna con título y fecha
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = formatDateTime(reminder.date),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            // Checkbox a la derecha
            Checkbox(
                checked = reminder.isDone,
                onCheckedChange = null
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun formatDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy, h:mm a", Locale("es", "ES"))
    return dateTime.format(formatter)
}

/**
 * Lista inicial de recordatorios usando
 */
@RequiresApi(Build.VERSION_CODES.O)
fun ListStarterReminder(): List<Reminder> {
    return listOf(
        Reminder(
            title = "Jugar con mi nieto",
            date = LocalDateTime.of(2025, 3, 16, 11, 30),
            isDone = false
        ),
        Reminder(
            title = "Comprar regalo a mi hija",
            date = LocalDateTime.of(2025, 3, 16, 9, 30),
            isDone = true
        ),
        Reminder(
            title = "Sacar al perro",
            date = LocalDateTime.of(2025, 3, 16, 9, 30),
            isDone = true
        ),
        Reminder(
            title = "Dar un paseo por el parque",
            date = LocalDateTime.of(2025, 3, 15, 16, 0),
            isDone = false
        ),
        Reminder(
            title = "Sacar al perro",
            date = LocalDateTime.of(2025, 3, 15, 9, 30),
            isDone = false
        ),
        Reminder(
            title = "Hacer café",
            date = LocalDateTime.of(2025, 3, 15, 7, 0),
            isDone = true
        ),
        Reminder(
            title = "Comprar Arroz",
            date = LocalDateTime.of(2025, 3, 15, 10, 0),
            isDone = true
        ),
        Reminder(
            title = "Recordar tomar agua",
            date = LocalDateTime.of(2025, 3, 14, 8, 0),
            isDone = false
        ),
        Reminder(
            title = "Enviar correo a Juan",
            date = LocalDateTime.of(2025, 3, 14, 10, 30),
            isDone = false
        ),
        Reminder(
            title = "Ir al gimnasio",
            date = LocalDateTime.of(2025, 3, 14, 18, 0),
            isDone = true
        ),
        Reminder(
            title = "Leer 30 minutos",
            date = LocalDateTime.of(2025, 3, 14, 20, 0),
            isDone = false
        ),
        Reminder(
            title = "Llamar a mamá",
            date = LocalDateTime.of(2025, 3, 14, 21, 0),
            isDone = false
        ),
        Reminder(
            title = "Preparar desayuno",
            date = LocalDateTime.of(2025, 3, 15, 7, 30),
            isDone = true
        ),
        Reminder(
            title = "Revisar agenda",
            date = LocalDateTime.of(2025, 3, 15, 8, 30),
            isDone = false
        ),
        Reminder(
            title = "Reunión de trabajo",
            date = LocalDateTime.of(2025, 3, 15, 14, 0),
            isDone = false
        ),
        Reminder(
            title = "Estudiar Kotlin",
            date = LocalDateTime.of(2025, 3, 15, 17, 0),
            isDone = true
        ),
        Reminder(
            title = "Cenar en familia",
            date = LocalDateTime.of(2025, 3, 15, 20, 0),
            isDone = false
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarReminder(
    navController: NavController,
    caretakerCount: Int = 4,
    onSearchClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        },
        title = {
            Text(
                text = "Cuidadores Activos: $caretakerCount",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(onClick = onAddClick) {
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




@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReminderListScreenPreview() {
    ReminderListScreen(navController = rememberNavController())
}
