package com.example.proyecto.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.R
import com.example.proyecto.Screen
import com.example.proyecto.ui.viewmodel.AuthViewModel

@Composable
fun LogScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {

    // Observar el estado del usuario. Si cambia a != null, navegar.
    val currentUser by authViewModel.currentUser.collectAsState()
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Navegar a la pantalla principal de anciano o cuidador según sea necesario.
            if (authViewModel.checkUserElderlyType()) {
                navController.navigate(route = Screen.MenuOldPerson.route) {
                    popUpTo("login_screen") { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(route = Screen.MenuCaretaker.route) {
                    popUpTo("login_screen") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Limpiar mensaje de feedback cuando la pantalla se recompone en el caso que ya no aplique
    DisposableEffect(authViewModel.feedbackMessage) {
        onDispose {
            // No limpiar aquí directamente para que el mensaje persista hasta la interacción
        }
    }

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
fun LogPhone(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Inicio de sesión",
            color = MaterialTheme.colorScheme.primary,

            )

        //Campo email
        OutlinedTextField(
            value = authViewModel.email,
            onValueChange = { authViewModel.onEmailChange(it)},
            label = { Text("Ingrese su correo electronico", color = MaterialTheme.colorScheme.secondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("correo", ignoreCase = true) == true
        )

        //Campo contraseña
        OutlinedTextField(
            value = authViewModel.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Ingrese su contraseña", color = MaterialTheme.colorScheme.secondary) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("contraseña", ignoreCase = true) == true
        )

        /*
        * Esos dos botones son para probar el d1111213233iseño
        * Cuando se coloque la funcionalidad debe detectar
        * a partir de los datos dados que tipo de usuario es
         */

        // Mensaje de Feedback/Error
        Box(modifier = Modifier.height(24.dp).fillMaxWidth()) { // Contenedor para estabilizar layout
            authViewModel.feedbackMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }

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
            onClick = { authViewModel.signInUser() },
            enabled = !authViewModel.isLoading,
            modifier = Modifier
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp), // Bordes redondeados
            colors = ButtonDefaults.buttonColors( // colores
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Iniciar Sesión como anciano")
            }
        }
    }
}

@Composable
fun ButtonRegistry(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
){

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "¿Aun no tienes cuenta? registrate aquí",
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = { navController.navigate(route = Screen.Registry.route) },
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LogScreenPreview() {
    // We need a NavController for the preview since the screen uses navigation.
    val navController = rememberNavController()
    LogScreen(navController = navController)
}
