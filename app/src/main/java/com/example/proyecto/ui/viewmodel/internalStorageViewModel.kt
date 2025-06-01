package com.example.proyecto.ui.viewmodel

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import androidx.lifecycle.ViewModel
import com.example.proyecto.data.HuellaData
import java.io.File


class internalStorageViewModel : ViewModel() {



    // guardar el json del correo y la contraseña relacionada con la huella, lo crea si no existe y lo sobreescribe si existe
    fun guardarJsonHuella(context: Context, correo: String, contrasenia: String): Boolean {
        return try {
            if (correo.isBlank() || contrasenia.isBlank()) {
                return false
            }

            val objeto = HuellaData(correo.trim(), contrasenia.trim())
            val json = Json.encodeToString(objeto)
            context.openFileOutput("huella.json", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Leer JSON de correo y contrasenia relacionada con la huella

    inline fun <reified T> leerJsonHuella(context: Context): T {
        val json = context.openFileInput("huella.json").bufferedReader().use { it.readText() }
        return Json.decodeFromString(json)
    }


    //saber si existe el archivo json de de correo y contrasenia relacionada con la huella
    fun existeJson(context: Context): Boolean {
        var nombreArchivo = "huella.json"
        val archivo = File(context.filesDir, nombreArchivo)
        return archivo.exists()
    }

    fun huellaIgualAUser(context: Context, correo: String?): Boolean {
        android.util.Log.d("internalStorageViewModel", "Verificando si existe el archivo JSON de huella")
        if (!existeJson(context)) {
            android.util.Log.d("internalStorageViewModel", "El archivo huella.json no existe")
            return false
        }
        val huellaData: HuellaData = leerJsonHuella(context)
        android.util.Log.d("internalStorageViewModel", "Datos leídos: correo=${huellaData.correo}, contra=${huellaData.contra}")
        val resultado = (huellaData.correo == correo)
        android.util.Log.d("internalStorageViewModel", "¿Coincide la huella con el usuario? $resultado")
        return resultado
    }
}