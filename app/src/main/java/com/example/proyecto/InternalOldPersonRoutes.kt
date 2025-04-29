package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import com.example.proyecto.ui.AuthViewModel
import com.example.proyecto.ui.CamaraScreen
import com.example.proyecto.ui.CreateActivityScreen
import com.example.proyecto.ui.CreateReminderScreen
import com.example.proyecto.ui.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.LocatCareViewModel
import com.example.proyecto.ui.LocationCaretakerScreen
import com.example.proyecto.ui.ReminderListScreen
import com.example.proyecto.ui.SOSScreen
import com.example.proyecto.ui.MainScreen

// La anotación @RequiresApi indica que este código requiere Android Oreo (API 26) o superior,
// ya que utiliza funcionalidades que se introdujeron a partir de esa versión
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InternalNavegationStack(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    rootNavController: NavController
){
    val sharedViewModel: SharedImageViewModel = viewModel()
    val context = LocalContext.current
    val sharedImageViewModel: SharedImageViewModel = viewModel()

    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) { MainScreen(
            navController = navController,
            rootNavController = rootNavController,
            authViewModel = authViewModel
        ) }
        composable(Screen.LocationCaretaker.route) {
            val locatCareViewModel: LocatCareViewModel = viewModel(
                factory = LocatCareViewModelFactory(context)
            )
            LocationCaretakerScreen(
                locatCareViewModel = locatCareViewModel,
                authViewModel= authViewModel,
                navController = navController
            )
        }
        composable(Screen.ReminderList.route) { ReminderListScreen(navController) }
        composable(Screen.ActivityList.route) { ListActivitiesOldPersonScreen() }
        composable(Screen.SosScreen.route) { SOSScreen(navController, authViewModel) }
        composable(Screen.CreateReminder.route) { CreateReminderScreen(navController) }
        composable(Screen.CreateActivity.route) { CreateActivityScreen(navController,sharedViewModel) }
        composable(Screen.CamaraActivityScreen.route) { CamaraScreen(navController,sharedViewModel) }
    }
}

