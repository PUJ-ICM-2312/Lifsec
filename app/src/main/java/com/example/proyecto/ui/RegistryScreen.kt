package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

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
fun RegistryScreen(navController: NavController) {
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
        RegistryPhone(navController)

        //boton de registro
        ButtonLogIn(navController)
    }

}
@Composable
fun RegistryPhone(navController: NavController){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Ingrese su celular para registrarse",
            color = MaterialTheme.colorScheme.primary,

            )
        var num = remember { mutableStateOf("") }
        OutlinedTextField(
            value = num.value,
            onValueChange = { newValue ->
                // Solo permite números
                if (newValue.all { it.isDigit() }) {
                    num.value = newValue
                }
            },
            label = { Text("Ingrese número registrado", color = MaterialTheme.colorScheme.secondary) }
        )




        Spacer(modifier = Modifier.padding(12.dp))

        Text("Su cuenta es de cuidador?",
            color = MaterialTheme.colorScheme.primary,

            )
        var checked = remember { mutableStateOf(true) }
        Switch(
            checked = checked.value,
            onCheckedChange = {
                checked.value = it
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
            )
        )
        // Iniciar sesion como cuidador (MOCKUP)
        Button(
            onClick = {
                if(checked.value == true) {

                    /* aqui se agrega codigo para guardar en base de datos como cuidador*/
                    navController.navigate(route = Screen.PersonSelector.route)
                }
                else{

                    /* aqui se agrega codigo para guardar en base de datos como cuidador*/
                    navController.navigate(route = Screen.MenuOldPerson.route)

                }
            },
            modifier = Modifier
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp), // Bordes redondeados
            colors = ButtonDefaults.buttonColors( // colores
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "registrarse")
        }

        //Iniciar sesion como anciano
    }
}

@Composable
fun ButtonLogIn(navController: NavController){
    Button(
        onClick = { navController.navigate(route = Screen.Login.route) },
        modifier = Modifier
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp), // Bordes redondeados
        colors = ButtonDefaults.buttonColors( // colores
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text = "iniciar sesion")
    }
}