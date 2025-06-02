package com.example.proyecto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.data.FirestoreProvider
import com.example.proyecto.data.RepositorioUsuarios
import com.example.proyecto.ui.LogScreen
import com.example.proyecto.ui.caretakerScreen.MenuCaretakersScreen
import com.example.proyecto.ui.elderlyScreens.MenuOldPersonScreen
import com.example.proyecto.ui.RegistryScreen
import com.example.proyecto.ui.caretakerScreen.ActivitiesCaretaker
import com.example.proyecto.ui.caretakerScreen.LocationOldPersonScreen
import com.example.proyecto.ui.caretakerScreen.RemindersCaretakerScreen
import com.example.proyecto.ui.caretakerScreen.UserListScreen
import com.example.proyecto.ui.elderlyScreens.AppPlanScreen
import com.example.proyecto.ui.elderlyScreens.CamaraScreen
import com.example.proyecto.ui.elderlyScreens.CaretakersConfigScreen
import com.example.proyecto.ui.elderlyScreens.ConfigurationScreenElder
import com.example.proyecto.ui.elderlyScreens.CreateActivityScreen
import com.example.proyecto.ui.elderlyScreens.CreateReminderScreen
import com.example.proyecto.ui.elderlyScreens.ListActivitiesOldPersonScreen
import com.example.proyecto.ui.elderlyScreens.LocationCaretakerScreen
import com.example.proyecto.ui.elderlyScreens.MainScreen
import com.example.proyecto.ui.elderlyScreens.ReminderListScreen
import com.example.proyecto.ui.elderlyScreens.SOSScreen
import com.example.proyecto.ui.viewmodel.ActivityViewModel
import com.example.proyecto.ui.viewmodel.AuthViewModel
import com.example.proyecto.ui.viewmodel.LocatCareViewModel
import com.example.proyecto.ui.viewmodel.MenuCareTakerViewModel
import com.example.proyecto.ui.viewmodel.MenuOldPersonViewModel
import com.example.proyecto.ui.viewmodel.ReminderViewModel
import com.example.proyecto.ui.viewmodel.SharedImageViewModel
import com.example.proyecto.ui.viewmodel.internalStorageViewModel

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
    object CaretakersConfigScreen: Screen("caretakers_config_screen")
    object AppPlanScreen: Screen("app_plan_screen")
    object LocationOldPerson : Screen("location")
    object RemindersCaretaker : Screen("reminders")
    object ActivitiesCaretaker : Screen("activities")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavegationStack() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val internalStorageViewModel: internalStorageViewModel = viewModel()
    val activityViewModel: ActivityViewModel = viewModel()
    val menuOldPersonViewModel: MenuOldPersonViewModel = viewModel()
    val context = LocalContext.current
    val menuCareTakerViewModel: MenuCareTakerViewModel= viewModel()
    val repUsuarios: RepositorioUsuarios = RepositorioUsuarios(FirestoreProvider.instance)

    val sharedViewModel: SharedImageViewModel = viewModel()
    val reminderViewModel: ReminderViewModel = viewModel()

    val sharedImageViewModel: SharedImageViewModel = viewModel()
    val locatCareViewModel: LocatCareViewModel = viewModel(factory = LocatCareViewModelFactory(context))


    //Para navegacion entre pantallas
    // Simplificamos la lógica inicial - siempre comenzamos en Login
    NavHost(navController = navController, startDestination = Screen.Login.route) {
//        composable(route = Screen.Login.route) { LogScreen( navController, authViewModel,internalStorageViewModel) }
//        composable(Screen.MenuCaretaker.route) { MenuCaretakersScreen(navController) }
//        composable(route = Screen.MenuOldPerson.route) { MenuOldPersonScreen( navController, authViewModel ) }
//        composable (route = Screen.PersonSelector.route) { UserListScreen(navController) }
//        composable(route = Screen.Registry.route) { RegistryScreen(navController, authViewModel) }

        composable(Screen.PersonSelector.route) { UserListScreen(navController) }
        composable(Screen.MenuCaretaker.route) { MenuCaretakersScreen(navController,menuCareTakerViewModel, authViewModel,activityViewModel) }


        composable(Screen.MainScreen.route) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.LocationCaretaker.route) {


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
            SOSScreen(navController, authViewModel,menuOldPersonViewModel)
        }

        composable(Screen.CreateReminder.route) {
            CreateReminderScreen(navController, reminderViewModel, authViewModel)
        }

        composable(Screen.CreateActivity.route) {

            CreateActivityScreen(navController, sharedViewModel, activityViewModel)
        }

        composable(Screen.CamaraActivityScreen.route) {
            CamaraScreen(navController, sharedImageViewModel)
        }

        composable (Screen.ConfigScreenElder.route){
            ConfigurationScreenElder(navController, authViewModel)
        }

        composable (Screen.CaretakersConfigScreen.route) {
            CaretakersConfigScreen(navController, authViewModel, repUsuarios)
        }

        composable (Screen.AppPlanScreen.route) {
            AppPlanScreen(navController)
        }
        composable(route = Screen.Login.route) {
            LogScreen(navController, authViewModel, internalStorageViewModel)
        }


        composable(route = Screen.MenuOldPerson.route) {
            MenuOldPersonScreen(navController, authViewModel, menuOldPersonViewModel,activityViewModel,reminderViewModel,locatCareViewModel)
        }

        composable(route = Screen.PersonSelector.route) {
            UserListScreen(navController)
        }

        composable(route = Screen.Registry.route) {
            RegistryScreen(navController, authViewModel)
        }
    }
}
