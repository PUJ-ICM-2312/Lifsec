package com.example.proyecto.ui.caretakerScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto.data.Recordatorio
import com.example.proyecto.ui.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RemindersCaretakerScreen(
    reminderViewModel: ReminderViewModel
) {
    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ReminderCaretakerListScreen(reminderViewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCaretakerListScreen(
    viewModel: ReminderViewModel
) {
    // 1) Colecta la lista de recordatorios desde el ViewModel
    val recordatorios = viewModel.recordatorios

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Recordatorios",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = Modifier.padding(8.dp)) {
            items(recordatorios) { recordatorio ->
                ReminderCaretakerListItem(recordatorio)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCaretakerListItem(recordatorio: Recordatorio) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recordatorio.titulo,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = formatFechaCaretaker(recordatorio.fecha),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Convierte la fecha (String) almacenada en el recordatorio
 * a un formato de tipo "EEEE d 'de' MMMM yyyy" en español.
 * Si recordatorio.fecha ya es "dd/MM/yyyy", primero parsea y luego formatea.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun formatFechaCaretaker(fechaStr: String): String {
    return try {
        val formatterIn = DateTimeFormatter.ofPattern("d/M/yyyy", Locale("es", "ES"))
        val formatterOut = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", Locale("es", "ES"))
        val date = LocalDate.parse(fechaStr, formatterIn)
        date.format(formatterOut)
    } catch (e: Exception) {
        // En caso de que el formato no coincida, devolver el string tal cual
        fechaStr
    }
}