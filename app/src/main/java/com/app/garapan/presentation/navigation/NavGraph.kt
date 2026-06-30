package com.app.garapan.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navArgument
import com.app.garapan.presentation.screen.add_portfolio.AddPortfolioScreen
import com.app.garapan.presentation.screen.all_reviews.AllReviewsScreen
import com.app.garapan.presentation.screen.auth.LoginScreen
import com.app.garapan.presentation.screen.auth.RegisterScreen
import com.app.garapan.presentation.screen.auth.SetupAccountScreen
import com.app.garapan.presentation.screen.auth.SplashScreen
import com.app.garapan.presentation.screen.article_list.ArticleListScreen
import com.app.garapan.presentation.screen.blog_detail.BlogDetailScreen
import com.app.garapan.presentation.screen.chat.ChatScreen
import com.app.garapan.presentation.screen.checkout.CheckoutScreen
import com.app.garapan.presentation.screen.edit_profile.EditProfileScreen
import com.app.garapan.presentation.screen.edit_portfolio.EditPortfolioScreen
import com.app.garapan.presentation.screen.edit_project.EditProjectScreen
import com.app.garapan.presentation.screen.edit_service.EditServiceScreen
import com.app.garapan.presentation.screen.forgot_password.ForgotPasswordScreen
import com.app.garapan.presentation.screen.jasa_detail.JasaDetailScreen
import com.app.garapan.presentation.screen.dispute.DisputeScreen
import com.app.garapan.presentation.screen.order_detail.OrderDetailScreen
import com.app.garapan.presentation.screen.order_history.OrderHistoryScreen
import com.app.garapan.presentation.screen.portfolio.PortfolioScreen
import com.app.garapan.presentation.screen.profile_services.ProfileServicesScreen
import com.app.garapan.presentation.screen.public_profile.PublicProfileScreen
import com.app.garapan.presentation.screen.my_projects.MyProjectsScreen
import com.app.garapan.presentation.screen.notifications.NotificationsScreen
import com.app.garapan.presentation.screen.project_detail.ProjectDetailScreen
import com.app.garapan.presentation.screen.review.ReviewScreen
import com.app.garapan.presentation.screen.top_workers.TopWorkersListScreen
import com.app.garapan.presentation.screen.reset_password.ResetPasswordScreen
import com.app.garapan.presentation.screen.change_password.ChangePasswordScreen
import com.app.garapan.presentation.screen.security.SecurityScreen
import com.app.garapan.presentation.screen.skills.SkillsScreen
import com.app.garapan.presentation.screen.two_factor.TwoFactorScreen
import com.app.garapan.presentation.screen.verify_email.VerifyEmailScreen
import com.app.garapan.domain.model.Role
import kotlinx.coroutines.flow.first

@Composable
fun NavGraph(
    navController: NavHostController,
    notificationRoute: String? = null,
    onNotificationRouteConsumed: () -> Unit = {}
) {
    LaunchedEffect(notificationRoute) {
        val route = notificationRoute ?: return@LaunchedEffect
        val currentRoute = navController.currentBackStackEntry
            ?.destination
            ?.route
            ?.takeIf { it != Routes.SPLASH }
            ?: navController.currentBackStackEntryFlow
                .first { entry -> entry.destination.route != Routes.SPLASH }
                .destination
                .route

        if (currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
        onNotificationRouteConsumed()
    }

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
            route = Routes.VERIFY_EMAIL,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("token") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "garapan://verify-email?token={token}" })
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email").orEmpty())
            val token = Uri.decode(backStackEntry.arguments?.getString("token").orEmpty())
            VerifyEmailScreen(navController = navController, email = email, token = token)
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(
            route = Routes.RESET_PASSWORD,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("token") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "garapan://reset-password?token={token}" })
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email").orEmpty())
            val token = Uri.decode(backStackEntry.arguments?.getString("token").orEmpty())
            ResetPasswordScreen(navController = navController, email = email, token = token)
        }
        composable(
            route = Routes.TWO_FACTOR,
            arguments = listOf(navArgument("preAuthToken") {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val preAuthToken = Uri.decode(backStackEntry.arguments?.getString("preAuthToken").orEmpty())
            TwoFactorScreen(navController = navController, preAuthToken = preAuthToken)
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
        composable(Routes.MAIN) {
            MainShell(rootNavController = navController)
        }
        composable(Routes.PROFILE_PORTFOLIO) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                PortfolioScreen(navController = navController)
            }
        }
        composable(Routes.ADD_PORTFOLIO) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA),
                navController = navController,
                fallbackRoute = Routes.PROFILE_PORTFOLIO
            ) {
                AddPortfolioScreen(navController = navController)
            }
        }
        composable(
            route = Routes.EDIT_PORTFOLIO,
            arguments = listOf(navArgument("portfolioId") { type = NavType.StringType })
        ) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA),
                navController = navController,
                fallbackRoute = Routes.PROFILE_PORTFOLIO
            ) {
                EditPortfolioScreen(navController = navController)
            }
        }
        composable(Routes.PROFILE_SERVICES) {
            ProfileServicesScreen(navController = navController)
        }
        composable(Routes.MY_PROJECTS) {
            RoleGuard(
                allowedRoles = setOf(Role.KLIEN, Role.MAHASISWA, Role.ADMIN),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                MyProjectsScreen(navController = navController)
            }
        }
        composable(Routes.SKILLS) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                SkillsScreen(navController = navController)
            }
        }
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController = navController)
        }
        composable(
            route = Routes.EDIT_SERVICE,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                EditServiceScreen(navController = navController)
            }
        }
        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(navController = navController)
        }
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("pesananId") { type = NavType.StringType })
        ) {
            OrderDetailScreen(navController = navController)
        }
        composable(
            route = Routes.REVIEW,
            arguments = listOf(navArgument("pesananId") { type = NavType.StringType })
        ) {
            RoleGuard(
                allowedRoles = setOf(Role.KLIEN),
                navController = navController,
                fallbackRoute = Routes.ORDER_HISTORY
            ) {
                ReviewScreen(navController = navController)
            }
        }
        composable(Routes.SECURITY) {
            SecurityScreen(navController = navController)
        }
        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(navController = navController)
        }
        composable(
            route = Routes.EDIT_PROJECT,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) {
            RoleGuard(
                allowedRoles = setOf(Role.KLIEN, Role.ADMIN),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                EditProjectScreen(navController = navController)
            }
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
            route = Routes.ALL_REVIEWS,
            arguments = listOf(navArgument("jasaId") { type = NavType.StringType })
        ) {
            AllReviewsScreen(navController = navController)
        }
        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("workerId") { type = NavType.StringType },
                navArgument("source") {
                    type = NavType.StringType
                    defaultValue = Routes.CHAT_SOURCE_WORKER
                },
                navArgument("peerName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ChatScreen(navController = navController)
        }
        composable(
            route = Routes.CHECKOUT,
            arguments = listOf(navArgument("jasaId") { type = NavType.StringType })
        ) {
            RoleGuard(
                allowedRoles = setOf(Role.MAHASISWA, Role.KLIEN, Role.ADMIN),
                navController = navController,
                fallbackRoute = Routes.MAIN
            ) {
                CheckoutScreen(navController = navController)
            }
        }
        composable(
            route = Routes.BLOG_DETAIL,
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) {
            BlogDetailScreen(navController = navController)
        }
        composable(
            route = Routes.PUBLIC_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            PublicProfileScreen(navController = navController)
        }
        composable(Routes.TOP_WORKERS) {
            TopWorkersListScreen(navController = navController)
        }
        composable(Routes.ARTICLE_LIST) {
            ArticleListScreen(navController = navController)
        }
        composable(
            route = Routes.DISPUTE,
            arguments = listOf(navArgument("pesananId") { type = NavType.StringType })
        ) {
            DisputeScreen(navController = navController)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }
    }
}
