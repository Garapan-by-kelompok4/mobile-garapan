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
import com.app.garapan.presentation.screen.blog_detail.BlogDetailScreen
import com.app.garapan.presentation.screen.chat.ChatScreen
import com.app.garapan.presentation.screen.checkout.CheckoutScreen
import com.app.garapan.presentation.screen.edit_profile.EditProfileScreen
import com.app.garapan.presentation.screen.edit_service.EditServiceScreen
import com.app.garapan.presentation.screen.home.HomeScreen
import com.app.garapan.presentation.screen.jasa_detail.JasaDetailScreen
import com.app.garapan.presentation.screen.order_history.OrderHistoryScreen
import com.app.garapan.presentation.screen.pesan.PesanScreen
import com.app.garapan.presentation.screen.post_project.PostProjectScreen
import com.app.garapan.presentation.screen.profile.ProfileScreen
import com.app.garapan.presentation.screen.portfolio.PortfolioScreen
import com.app.garapan.presentation.screen.profile_services.ProfileServicesScreen
import com.app.garapan.presentation.screen.project_detail.ProjectDetailScreen
import com.app.garapan.presentation.screen.search.SearchScreen
import com.app.garapan.presentation.screen.security.SecurityScreen

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
        composable(Routes.SEARCH) {
            SearchScreen(navController = navController)
        }
        composable(Routes.PESAN) {
            PesanScreen(navController = navController)
        }
        composable(Routes.POST_PROJECT) {
            PostProjectScreen(navController = navController)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }
        composable(Routes.PROFILE_PORTFOLIO) {
            PortfolioScreen(navController = navController)
        }
        composable(Routes.PROFILE_SERVICES) {
            ProfileServicesScreen(navController = navController)
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController = navController)
        }
        composable(
            route = Routes.EDIT_SERVICE,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) {
            EditServiceScreen(navController = navController)
        }
        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(navController = navController)
        }
        composable(Routes.SECURITY) {
            SecurityScreen(navController = navController)
        }
        composable(
            route = Routes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) {
            ProjectDetailScreen(navController = navController)
        }
        composable(
            route = Routes.JASA_DETAIL,
            arguments = listOf(navArgument("jasaId") { type = NavType.StringType })
        ) {
            JasaDetailScreen(navController = navController)
        }
        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("workerId") { type = NavType.StringType })
        ) {
            ChatScreen(navController = navController)
        }
        composable(Routes.CHECKOUT) {
            CheckoutScreen(navController = navController)
        }
        composable(
            route = Routes.BLOG_DETAIL,
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) {
            BlogDetailScreen(navController = navController)
        }
    }
}
