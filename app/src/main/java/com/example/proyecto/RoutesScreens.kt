package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.LogScreen
import com.example.proyecto.ui.caretakerScreen.MenuCaretakersScreen
import com.example.proyecto.ui.elderlyScreens.MenuOldPersonScreen
import com.example.proyecto.ui.RegistryScreen
import com.example.proyecto.ui.caretakerScreen.UserListScreen

sealed class Screen(val route: String) {
    object Login: Screen("login_screen")
    object PersonSelector: Screen("person_selector_screen")
    object MenuOldPerson: Screen("menu_old_person_screen")
    object MenuCaretaker: Screen("menu_caretaker_screen")
    object Registry: Screen("Registry_screen")

    object MainScreen: Screen("main_screen_elder")
    object LocationCaretaker: Screen("location_caretaker_screen_elder")
    object ReminderList: Screen("reminder_list_screen_elder")
    object ActivityList: Screen("activity_list_screen_elder")
    object SosScreen: Screen("sos_screen")
    object CreateReminder: Screen("create_reminder_screen")
    object CreateActivity: Screen("create_activity_screen")
    object CamaraActivityScreen: Screen("camara_activity_screen")
    object ConfigScreenElder: Screen("config_screen_elder")
    object caretakersConfigScreen: Screen("caretakers_config_screen")
    object packageConfigScreen: Screen("package_config_screen")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavegationStack(){
    val navController = rememberNavController()

    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = Screen.Login.route){
        composable(route = Screen.Login.route) { LogScreen( navController) }
        composable(route = Screen.PersonSelector.route) { UserListScreen(navController) }
        composable(route = Screen.MenuOldPerson.route) { MenuOldPersonScreen( navController) }
        composable (route = Screen.MenuCaretaker.route) { MenuCaretakersScreen( navController) }
        composable(route = Screen.Registry.route) { RegistryScreen(navController) }
    }

}
