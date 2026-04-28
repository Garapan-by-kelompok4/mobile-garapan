package com.app.garapan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.garapan.presentation.screen.auth.LoginScreen
import com.app.garapan.presentation.screen.auth.RegisterScreen
import com.app.garapan.presentation.screen.auth.SetupAccountScreen
import com.app.garapan.presentation.screen.auth.SplashScreen
import com.app.garapan.presentation.screen.home.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }
        composable(
            route = "${Routes.SETUP}?role={role}",
            arguments = listOf(navArgument("role") {
                type = NavType.StringType
                defaultValue = "student"
            })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "student"
            SetupAccountScreen(navController = navController, role = role)
        }
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }
    }
}
