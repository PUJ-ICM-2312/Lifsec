package com.example.proyecto.ui.caretakerScreen

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.R
import com.example.proyecto.Screen
import kotlinx.parcelize.Parcelize


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(navController: NavController){
    val grandparents = ListStarter()
    Surface (
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn {
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.outline
                ) {
                    HeaderG()
                }
            }

            items(grandparents) { grandParent ->
                // Aquí mostramos la info de cada abuelo
                UserListItem(grandP = grandParent, navController)
                }

        }
    }

}


// Composable para crear el header de la lista
@Composable
fun HeaderG() {
    Text (
        text = "A quien quieres cuidar?",
        modifier = Modifier
            .fillMaxWidth().padding(16.dp).statusBarsPadding(),
        style = MaterialTheme.typography.headlineSmall
    )
}

//Composable para crear item en la lista, se usa ListItem de Material3
@Composable
fun UserListItem(grandP: GrandParent, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Navega a la pantalla principal del cuidador
                navController.navigate(Screen.MenuCaretaker.route)
            }
    ) {
        ListItem(
            modifier = Modifier.padding(5.dp),
            leadingContent = {
                Image(
                    painter = painterResource(id = R.drawable.ejemploperfil),
                    contentDescription = "",
                    modifier = Modifier.size(100.dp)
                )
            },
            headlineContent = {
                Text(
                    text = "${grandP.firstName} ${grandP.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            supportingContent = {
                Text(
                    text = "${grandP.age} años",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            trailingContent = {
                Icon(Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Gray)
            }
        )
        HorizontalDivider(thickness = 1.dp)
    }
}


@Parcelize
data class GrandParent(
    var firstName: String,
    var lastName: String,
    var age: Int,
    var gender: String,
    var phone: String,
    var height: Double,
    var weight: Double,

    ):Parcelable

fun ListStarter(): List<GrandParent>{

    var grandParent1 = GrandParent(firstName = "Simon", lastName = "Monroy", age = 70, gender = "M", phone = "314 3164108", height = 1.76, 67.1 )
    var grandParent2 = GrandParent(firstName = "Jorge", lastName = "Sierra", age = 73, gender = "M", phone = "302 8540107", height = 1.81, 77.5 )

    val lista: List<GrandParent> = listOf(grandParent1, grandParent2)

    return lista
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserListScreenPreview() {
    UserListScreen(navController = rememberNavController())
}