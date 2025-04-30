package com.example.proyecto.ui

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun FingerprintPrompt(
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(Unit) {
        val promptInfo = PromptInfo.Builder()
            .setTitle("Autenticación por huella")
            .setSubtitle("Usa tu huella para continuar")
            .setNegativeButtonText("Cancelar")
            .build()

        val activity = context as? FragmentActivity
        if (activity == null) {
            onAuthError("No se pudo acceder a la actividad.")
            return@DisposableEffect onDispose { }
        }

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onAuthError(errString.toString())
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)

        onDispose { /* No cleanup needed */ }
    }
}
