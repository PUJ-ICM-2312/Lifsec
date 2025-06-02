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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RemindersCaretakerScreen() {
    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ReminderCaretakerListScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCaretakerListScreen() {
    val reminders = listOf(
        ReminderCaretaker("Asegurarse de que haya tomado su medicina", LocalDateTime.now()),
        ReminderCaretaker("Aplicar cremas y tratamientos", LocalDateTime.now().plusHours(2)),
        ReminderCaretaker("Revisar la presion arterial / azucar", LocalDateTime.now().plusDays(1))
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text(
            text = "Recordatorios",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            items(reminders.size) { index ->
                ReminderCaretakerListItem(reminders[index])
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCaretakerListItem(reminder: ReminderCaretaker) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = formatDateTimeCaretaker(reminder.date),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
data class ReminderCaretaker(val title: String, val date: LocalDateTime)

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateTimeCaretaker(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy, h:mm a", Locale("es", "ES"))
    return dateTime.format(formatter)
}


