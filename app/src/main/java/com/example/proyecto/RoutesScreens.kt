package com.example.proyecto

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.LogScreen
import com.example.proyecto.ui.MenuOldPersonScreen
import com.example.proyecto.ui.UserListScreen

sealed class Screen(val route: String) {
    object Login: Screen("login_screen")
    object PersonSelector: Screen("person_selector_screen")
    object MenuOldPerson: Screen("menu_old_person_screen")
    object MenuCaretaker: Screen("menu_caretaker_screen")
}

@Composable
fun NavegationStack(){
    val navController = rememberNavController()

    //Para navegacion entre pantallas
    NavHost(navController = navController, startDestination = Screen.Login.route){
        composable(route = Screen.Login.route) { LogScreen( navController) }
        composable(route = Screen.PersonSelector.route) { UserListScreen(navController) }
        composable(route = Screen.MenuOldPerson.route) { MenuOldPersonScreen( navController) }
    }

}