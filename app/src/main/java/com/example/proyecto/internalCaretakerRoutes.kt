package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto.ui.caretakerScreen.ActivitiesCaretaker
import com.example.proyecto.ui.caretakerScreen.LocationOldPersonScreen
import com.example.proyecto.ui.caretakerScreen.MenuCaretakersScreen
import com.example.proyecto.ui.caretakerScreen.RemindersCaretakerScreen
import com.example.proyecto.ui.caretakerScreen.UserListScreen
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel


// La anotación @RequiresApi indica que este código requiere Android Oreo (API 26) o superior,
// ya que utiliza funcionalidades que se introdujeron a partir de esa versión
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InternalCaretakerRoutesStack(navController: NavHostController){
    val activityViewModel: ActivityViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = Screen.PersonSelector.route) {
        composable(Screen.PersonSelector.route) { UserListScreen(navController) }
        composable(Screen.MenuCaretaker.route) { MenuCaretakersScreen(navController) }
        composable(Screen.LocationOldPerson.route) { LocationOldPersonScreen(navController,authViewModel ) }
        composable(Screen.RemindersCaretaker.route) { RemindersCaretakerScreen(navController) }
        composable(Screen.ActivitiesCaretaker.route) { ActivitiesCaretaker(navController,activityViewModel) }
    }
}

