package com.newstudio.ujkhataapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.newstudio.ujkhataapp.ui.screen.CustomerScreen
import com.newstudio.ujkhataapp.ui.screen.EntriesScreen
import com.newstudio.ujkhataapp.ui.screen.EntryInEntryScreen
import com.newstudio.ujkhataapp.viewModel.KhataViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: KhataViewModel
) {
    NavHost(navController = navController, startDestination = Screen.CustomerScreen.route) {
        composable(Screen.CustomerScreen.route) {
            CustomerScreen(
                navController = navController,
                viewModel = viewModel,
            )
        }

        composable("entry_screen/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            EntriesScreen(customerId = customerId, viewModel = viewModel, navController = navController)
        }

        composable("entry_in_entry_screen/{customerId}/{entryId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            EntryInEntryScreen(
                customerId = customerId,
                entryId = entryId,
                viewModel = viewModel
            )
        }
    }
}
