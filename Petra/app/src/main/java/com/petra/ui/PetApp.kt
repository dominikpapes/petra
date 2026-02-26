package com.petra.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petra.viewmodel.PetViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PetApp() {
    val navController = rememberNavController()

    // Koin intercepts this call and provides the ViewModel from the module we built!
    val viewModel: PetViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(viewModel, navController)
        }
        composable("create") {
            CreatePetScreen(viewModel, navController)
        }
        composable("edit") {
            EditPetScreen(viewModel, navController)
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}
