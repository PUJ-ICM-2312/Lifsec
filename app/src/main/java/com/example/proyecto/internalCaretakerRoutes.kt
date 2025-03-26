package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto.ui.ActivitiesCaretaker
import com.example.proyecto.ui.LocationOldPersonScreen
import com.example.proyecto.ui.RemindersCaretakerScreen

sealed class InternalCaretakerRoutes(val route: String) {
    object LocationOldPerson : InternalCaretakerRoutes("location")
    object RemindersCaretaker : InternalCaretakerRoutes("reminders")
    object ActivitiesCaretaker : InternalCaretakerRoutes("activities")
}

// La anotación @RequiresApi indica que este código requiere Android Oreo (API 26) o superior,
// ya que utiliza funcionalidades que se introdujeron a partir de esa versión
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InternalCaretakerRoutesStack(navController: NavHostController){
    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = InternalCaretakerRoutes.LocationOldPerson.route) {
        composable(InternalCaretakerRoutes.LocationOldPerson.route) { LocationOldPersonScreen(navController) }
        composable(InternalCaretakerRoutes.RemindersCaretaker.route) { RemindersCaretakerScreen(navController) }
        composable(InternalCaretakerRoutes.ActivitiesCaretaker.route) { ActivitiesCaretaker(navController) }
    }
}

