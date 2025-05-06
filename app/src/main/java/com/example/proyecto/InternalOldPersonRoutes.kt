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
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.ReminderViewModel
import com.example.proyecto.ui.viewmodel.SharedImageViewModel

// La anotación @RequiresApi indica que este código requiere Android Oreo (API 26) o superior,
// ya que utiliza funcionalidades que se introdujeron a partir de esa versión
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InternalNavegationStack(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    rootNavController: NavController
) {
    val context = LocalContext.current

    val activityViewModel: ActivityViewModel = viewModel()
    val sharedViewModel: SharedImageViewModel = viewModel()
    val reminderViewModel: ReminderViewModel = viewModel()

    val sharedImageViewModel: SharedImageViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                navController = navController,
                rootNavController = rootNavController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.LocationCaretaker.route) {
            val locatCareViewModel: LocatCareViewModel = viewModel(
                factory = LocatCareViewModelFactory(context)
            )
            LocationCaretakerScreen(
                locatCareViewModel = locatCareViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Screen.ReminderList.route) {
            ReminderListScreen(navController, reminderViewModel)
        }

        composable(Screen.ActivityList.route) { ListActivitiesOldPersonScreen(activityViewModel,navController) }

        composable(Screen.SosScreen.route) {
            SOSScreen(navController, authViewModel)
        }

        composable(Screen.CreateReminder.route) {
            CreateReminderScreen(navController, reminderViewModel)
        }

        composable(Screen.CreateActivity.route) {

            CreateActivityScreen(navController, sharedViewModel, activityViewModel)
        }

        composable(Screen.CamaraActivityScreen.route) {
            CamaraScreen(navController, sharedImageViewModel)
        }

        composable (Screen.ConfigScreenElder.route){
            ConfigurationScreenElder(navController)
        }

        composable (Screen.CaretakersConfigScreen.route) {
            CaretakersConfigScreen(navController)
        }

        composable (Screen.AppPlanScreen.route) {
            AppPlanScreen(navController)
        }
    }
}
