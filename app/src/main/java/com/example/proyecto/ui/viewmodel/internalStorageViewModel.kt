package com.example.proyecto.ui.viewmodel

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import androidx.lifecycle.ViewModel
import com.example.proyecto.data.HuellaData
import java.io.File


class internalStorageViewModel : ViewModel() {



    // guardar el json del correo y la contraseña relacionada con la huella, lo crea si no existe y lo sobreescribe si existe
    fun guardarJsonHuella(context: Context, correo: String, contrasenia: String) {
        val objeto = HuellaData(correo, contrasenia)
        val json = Json.encodeToString(objeto)
        context.openFileOutput("huella.json", Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
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
    fun huellaIgualAUser(context: Context, correo:String, contra:String): Boolean {
        //esto se hace porque si nm oexiste se muere la app
        if (!existeJson(context)) {
            return false
        }
        var huellaData: HuellaData = leerJsonHuella(context)
        return (huellaData.correo == correo && huellaData.contra == contra)
    }



}