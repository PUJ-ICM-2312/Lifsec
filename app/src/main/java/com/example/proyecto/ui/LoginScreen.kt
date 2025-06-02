package com.example.proyecto.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.proyecto.data.Anciano
import com.example.proyecto.data.Cuidador
import com.example.proyecto.data.HuellaData
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.internalStorageViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LogScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    internalViewModel: internalStorageViewModel
) {


    val currentEntity by authViewModel.currentEntity.collectAsState()
    // Observar el estado del usuario. Si cambia a != null, navegar.
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoadingData.collectAsState()
    LaunchedEffect(currentEntity,currentEntity,isLoading) {
        Log.d("LogScreen", "Usuario autenticado: ${currentUser?.email}")
        when {
            isLoading -> {
                Log.d("ComparacionEntity", "cargando el authviewmodel")
            }
            currentEntity is Anciano -> {
                Log.d("ComparacionEntity", "Usuario autenticado: ${currentUser?.email} como anciano")
                navController.navigate(Screen.MenuOldPerson.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            currentEntity is Cuidador -> {
                Log.d("ComparacionEntity", "Usuario autenticado: ${currentUser?.email} como cuidador")
                navController.navigate(Screen.PersonSelector.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {
                Log.d("ComparacionEntity", "Usuario autenticado: ${currentUser?.email} pero no se sabe si es cuidador o anciano")
            }
        }

        if (currentUser != null) {

            Log.d("LogScreen", "Usuario autenticado: ${currentUser?.email}")
            if(currentEntity is Anciano){

            } else if(currentEntity is Cuidador){


            }else{


            }


        } else {
            Log.d("LogScreen", "Usuario no autenticado")
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
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        //imagen
        Image(
            painter = painterResource(id = R.drawable.lifesec_logo),
            contentDescription = ""
        )
        //Ingreso de numero de celular
        LogPhone(navController,internalViewModel,authViewModel)

        //boton de registro
        ButtonRegistry(navController)
    }
}

@Composable
fun LogPhone(
    navController: NavController,
    internalViewModel: internalStorageViewModel,
    authViewModel: AuthViewModel

) {
    var huellaGuardada by remember { mutableStateOf(false) }
    val showFingerprint = remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            "Inicio de sesión",
            color = MaterialTheme.colorScheme.primary,
        )

        // Campo email
        OutlinedTextField(
            value = authViewModel.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = {
                Text(
                    "Ingrese su correo electrónico",
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("correo", ignoreCase = true) == true
        )

        // Campo contraseña
        OutlinedTextField(
            value = authViewModel.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = {
                Text(
                    "Ingrese su contraseña",
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("contraseña", ignoreCase = true) == true
        )

        // Mensaje de error
        Box(modifier = Modifier.height(24.dp).fillMaxWidth()) {
            authViewModel.feedbackMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }

        // Iniciar sesión
        Button(
            onClick = {
                authViewModel.signInAndLoadUser(
                    onSuccess = {
                        val route = when (authViewModel.userType.value) {
                            AuthViewModel.UserType.ANCIANO -> Screen.MenuOldPerson.route
                            AuthViewModel.UserType.CUIDADOR -> Screen.PersonSelector.route
                            else -> {
                                authViewModel.updateFeedbackMessage("No se pudo identificar el tipo de usuario")
                                return@signInAndLoadUser
                            }
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onError = { errorMessage ->
                        authViewModel.updateFeedbackMessage(errorMessage)
                    }
                )
            },
            enabled = !authViewModel.isLoading,
            modifier = Modifier.padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión")
            }
        }

        // Botón huella dummy
        Button(
            onClick = {
                try {
                    val existeArchivo = internalViewModel.existeJson(context)
                    if (existeArchivo) {
                        val huellaData: HuellaData = internalViewModel.leerJsonHuella(context = context)
                        if (huellaData != null &&
                            huellaData.correo?.isNotEmpty() == true &&
                            huellaData.contra?.isNotEmpty() == true) {
                            showFingerprint.value = true
                        } else {
                            Toast.makeText(
                                context,
                                "No hay credenciales guardadas para la huella",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Primero debe iniciar sesión y guardar sus credenciales",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error al leer datos de huella",
                        Toast.LENGTH_SHORT
                    ).show()
                    showFingerprint.value = false
                }
            },
            modifier = Modifier.padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Seguir con huella", color = MaterialTheme.colorScheme.primary)
        }

        if (showFingerprint.value) {
            FingerprintPrompt(
                onAuthSuccess = {
                    val huellaData: HuellaData = internalViewModel.leerJsonHuella(context)


                    authViewModel.onEmailChange(huellaData.correo)
                    authViewModel.onPasswordChange(huellaData.contra)
                    showFingerprint.value = false
                },
                onAuthError = {
                    authViewModel.clearFeedbackMessage()
                    showFingerprint.value = false
                }
            )
        }
    }
}

@Composable
fun ButtonRegistry(
    navController: NavController,
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
    navController.navigate(Screen.Registry.route)
}
