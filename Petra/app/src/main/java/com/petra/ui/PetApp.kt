package com.petra.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petra.viewmodel.PetViewModel
import com.petra.viewmodel.PetViewModelFactory

@Composable
fun PetApp() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as Application
    val viewModel: PetViewModel = viewModel(factory = PetViewModelFactory(context))

    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(viewModel, navController)
        }
        composable("create") {
            CreatePetScreen(viewModel, navController)
        }
    }
}
