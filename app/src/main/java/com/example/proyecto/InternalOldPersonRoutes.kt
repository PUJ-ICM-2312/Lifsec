package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import com.example.proyecto.ui.CamaraScreen
import com.example.proyecto.ui.CreateActivityScreen
import com.example.proyecto.ui.CreateReminderScreen
import com.example.proyecto.ui.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.LocatCareViewModel
import com.example.proyecto.ui.LocationCaretakerScreen
import com.example.proyecto.ui.ReminderListScreen
import com.example.proyecto.ui.SOSScreen
import com.example.proyecto.ui.MainScreen
import com.example.proyecto.ui.theme.SharedViewModel

sealed class InternalScreen(val route: String) {
    object MainScreen: Screen("main_screen")
    object LocationCaretaker: Screen("location_caretaker_screen")
    object ReminderList: Screen("reminder_list_screen")
    object ActivityList: Screen("activity_list_screen")
    object SosScreen: Screen("sos_screen")
    object CreateReminder: Screen("create_reminder_screen")
    object CreateActivity: Screen("create_activity_screen")
    object CamaraActivityScreen: Screen("camara_activity_screen")
}

// La anotación @RequiresApi indica que este código requiere Android Oreo (API 26) o superior,
// ya que utiliza funcionalidades que se introdujeron a partir de esa versión
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InternalNavegationStack(navController: NavHostController){
    val sharedViewModel: SharedViewModel = viewModel()
    val context = LocalContext.current

    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = InternalScreen.MainScreen.route) {
        composable(InternalScreen.MainScreen.route) { MainScreen(navController) }
        composable(InternalScreen.LocationCaretaker.route) {
            val locatCareViewModel: LocatCareViewModel = viewModel(
                factory = LocatCareViewModelFactory(context)
            )
            LocationCaretakerScreen(
                locatCareViewModel = locatCareViewModel,
                navController = navController
            )
        }
        composable(InternalScreen.ReminderList.route) { ReminderListScreen(navController) }
        composable(InternalScreen.ActivityList.route) { ListActivitiesOldPersonScreen() }
        composable(InternalScreen.SosScreen.route) { SOSScreen(navController) }
        composable(InternalScreen.CreateReminder.route) { CreateReminderScreen(navController) }
        composable(InternalScreen.CreateActivity.route) { CreateActivityScreen(navController,sharedViewModel) }
        composable(InternalScreen.CamaraActivityScreen.route) { CamaraScreen(navController,sharedViewModel) }
    }
}

