package com.example.proyecto.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.Screen
import com.example.proyecto.ui.viewmodel.AuthViewModel

@Composable
fun RegistryScreen(navController: NavController, authViewModel: AuthViewModel) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(180.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.lifesec_logo),
            contentDescription = null
        )

        RegistryForm(navController, authViewModel)

        Button(
            onClick = { navController.navigate(route = Screen.Login.route) },
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Iniciar sesión")
        }
    }
}

@Composable
fun RegistryForm(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Estados locales
    val email by remember { derivedStateOf { authViewModel.email } }
    val password by remember { derivedStateOf { authViewModel.password } }
    var confirmInput by remember { mutableStateOf("") }
    var isElderly by remember { mutableStateOf(false) }

    // Verificar coincidencia de contraseñas localmente
    val passwordsMatch = confirmInput == password
    val auth = authViewModel.getAuth()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Campo de confirmación de contraseña (solo verificación local)
        OutlinedTextField(
            value = confirmInput,
            onValueChange = { confirmInput = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = confirmInput.isNotEmpty() && !passwordsMatch
        )
        if (confirmInput.isNotEmpty() && !passwordsMatch) {
            Text(
                text = "Las contraseñas no coinciden",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Segmented button
        SegmentToggle(
            options = listOf("Cuidador", "Anciano"),
            selectedIndex = if (isElderly) 1 else 0,
            onOptionSelected = { idx -> isElderly = (idx == 1) }
        )

        if (authViewModel.feedbackMessage != null) {
            Text(
                text = authViewModel.feedbackMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }
        val context = LocalContext.current

        Button(
            onClick = {
                if (password.length >= 6) {
                    if (email.contains("@") && !email.contains("@.") && email.contains(".co")) {
                        authViewModel.registerUser(
                            isAnciano = isElderly,
                            onSuccess = {
                                navController.navigate(
                                    if (isElderly) Screen.MenuOldPerson.route
                                    else Screen.PersonSelector.route
                                ) {
                                    // Limpiar el back stack
                                    popUpTo("registry_screen") { inclusive = true }
                                }
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Por favor ingrese un correo válido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "La contraseña necesita más de 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            enabled = !authViewModel.isLoading && email.isNotBlank() &&
                    password.isNotBlank() && passwordsMatch
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Registrarse")
            }
        }
    }
}

@Composable
fun SegmentToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentWidth()
    ) {
        options.forEachIndexed { index, option ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                options.lastIndex -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(0.dp)
            }
            val selected = index == selectedIndex
            OutlinedButton(
                onClick = { onOptionSelected(index) },
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(option)
            }
        }
    }
}
