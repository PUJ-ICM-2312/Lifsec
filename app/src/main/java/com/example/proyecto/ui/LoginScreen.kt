package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.Screen

@Composable
fun LogScreen(navController: NavController) {
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
            LogPhone(navController)

            //boton de registro
            ButtonRegistry(navController)
        }

}
@Composable
fun LogPhone(navController: NavController){
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

        /*
        * Esos dos botones son para probar el diseño
        * Cuando se coloque la funcionalidad debe detectar
        * a partir de los datos dados que tipo de usuario es
         */

        // Iniciar sesion como cuidador (MOCKUP)
        Button(
            onClick = { navController.navigate(route = Screen.PersonSelector.route) },
            modifier = Modifier
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp), // Bordes redondeados
            colors = ButtonDefaults.buttonColors( // colores
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "iniciar sesion como cuidador")
        }

        //Iniciar sesion como anciano
        Button(
            onClick = { navController.navigate(route = Screen.MenuOldPerson.route) },
            modifier = Modifier
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp), // Bordes redondeados
            colors = ButtonDefaults.buttonColors( // colores
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "iniciar sesion como anciano")
        }
    }
}

@Composable
fun ButtonRegistry(navController: NavController){
    Button(
        onClick = { /*nav pero todavia no lo hacemos para registrarse*/ },
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