package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.ui.theme.primaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationOldPersonScreen(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Abuelo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.locationoldpersonfromcaretaker),
                contentDescription = "Mock ubicación de cuidadores",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Botón "Cómo Llegar"
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clickable { showBottomSheet = true }
                    .background(Color.Gray.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cómo Llegar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Mostrar BottomSheet con el mapa
    if (showBottomSheet) {
        MapBottomSheet(onDismiss = { showBottomSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet (
        onDismissRequest = { onDismiss() },
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.81f).verticalScroll(
                rememberScrollState()
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ubicación en el mapa", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = painterResource(id = R.drawable.comollegar), // Asegúrate de agregar esta imagen en res/drawable
                contentDescription = "Mapa de ubicación",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(450.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { onDismiss() }) {
                Text("Cerrar")
            }
        }
    }
}
