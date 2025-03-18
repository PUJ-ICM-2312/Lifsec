package com.example.proyecto

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.LogScreen
import com.example.proyecto.ui.MenuScreen
import com.example.proyecto.ui.UserListScreen

sealed class Screen(val route: String) {
    object Login: Screen("login_screen")
    object PersonSelector: Screen("person_selector_screen")
    object Menu: Screen("menu_screen")
}

@Composable
fun NavegationStack(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route){
        composable(route = Screen.Login.route) { LogScreen( navController) }
        composable(route = Screen.PersonSelector.route) { UserListScreen(navController) }
        composable(route = Screen.Menu.route) { MenuScreen( navController) }
    }

}