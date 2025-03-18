package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.proyecto.R
import com.example.proyecto.ui.theme.backgroundLight
import com.example.proyecto.ui.theme.primaryLight

@Composable
fun LogScreen() {
        Column(

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(180.dp, Alignment.CenterVertically)


        ) {
            //imagen
            Image(
                painter = painterResource(id = R.drawable.lifesec_logo),
                contentDescription = ""

            )
            //Ingreso de numero de celular
            LogPhone()

            //boton de registro
            ButtonRegistry()
        }

}
@Composable
fun LogPhone(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Ingrese su celular",
            color = MaterialTheme.colorScheme.primary,

            )
        var num = remember { mutableStateOf("") }
        OutlinedTextField(
            value = num.value,
            onValueChange = { num.value = it },
            label = { Text("ingrese numero registrado", color = MaterialTheme.colorScheme.secondary)}
        )

        Button(
            onClick = { /*nav pero todavia no lo hacemos */ },
            modifier = Modifier
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp), // Bordes redondeados
            colors = ButtonDefaults.buttonColors( // colores
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "iniciar sesion")
        }
    }
}

@Composable
fun ButtonRegistry(){
    Button(
        onClick = { /*nav pero todavia no lo hacemos */ },
        modifier = Modifier
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp), // Bordes redondeados
        colors = ButtonDefaults.buttonColors( // colores
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text = "REGISTRARSE")
    }
}