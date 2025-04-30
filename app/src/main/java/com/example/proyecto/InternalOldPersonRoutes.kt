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
import com.example.proyecto.Screen.ConfigScreenElder
import com.example.proyecto.ui.elderlyScreens.AppPlanScreen
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.elderlyScreens.CamaraScreen
import com.example.proyecto.ui.elderlyScreens.CaretakersConfigScreen
import com.example.proyecto.ui.elderlyScreens.ConfigurationScreenElder
import com.example.proyecto.ui.elderlyScreens.CreateActivityScreen
import com.example.proyecto.ui.elderlyScreens.CreateReminderScreen
import com.example.proyecto.ui.elderlyScreens.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.viewmodel.LocatCareViewModel
import com.example.proyecto.ui.elderlyScreens.LocationCaretakerScreen
import com.example.proyecto.ui.elderlyScreens.ReminderListScreen
import com.example.proyecto.ui.elderlyScreens.SOSScreen
import com.example.proyecto.ui.elderlyScreens.MainScreen
import com.example.proyecto.ui.viewmodel.SharedImageViewModel

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
        composable(Screen.ConfigScreenElder.route) { ConfigurationScreenElder(navController) }
        composable(route = Screen.CaretakersConfigScreen.route) { CaretakersConfigScreen(navController) }
        composable(route = Screen.AppPlanScreen.route) { AppPlanScreen(navController) }
    }
}

