package com.example.proyecto

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import com.example.proyecto.ui.CreateActivityScreen
import com.example.proyecto.ui.CreateReminderScreen
import com.example.proyecto.ui.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.LocationCaretakerScreen
import com.example.proyecto.ui.ReminderListScreen
import com.example.proyecto.ui.SOSScreen
import com.example.proyecto.ui.MainScreen

sealed class InternalScreen(val route: String) {
    object MainScreen: Screen("main_screen")
    object LocationCaretaker: Screen("location_caretaker_screen")
    object ReminderList: Screen("reminder_list_screen")
    object ActivityList: Screen("activity_list_screen")
    object SosScreen: Screen("sos_screen")
    object CreateReminder: Screen("create_reminder_screen")
    object CreateActivity: Screen("create_activity_screen")
}

@Composable
fun InternalNavegationStack(navController: NavHostController){
    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = InternalScreen.MainScreen.route) {
        composable(InternalScreen.MainScreen.route) { MainScreen(navController) }
        composable(InternalScreen.LocationCaretaker.route) { LocationCaretakerScreen() }
        composable(InternalScreen.ReminderList.route) { ReminderListScreen() }
        composable(InternalScreen.ActivityList.route) { ListActivitiesOldPersonScreen() }
        composable(InternalScreen.SosScreen.route) { SOSScreen(navController) }
        composable(InternalScreen.CreateReminder.route) { CreateReminderScreen(navController) }
        composable(InternalScreen.CreateActivity.route) { CreateActivityScreen(navController) }
    }
}

