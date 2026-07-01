package com.app.garapan.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.House
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.ReceiptText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.garapan.domain.model.Role
import com.app.garapan.presentation.screen.edit_service.EditServiceScreen
import com.app.garapan.presentation.screen.home.HomeScreen
import com.app.garapan.presentation.screen.order_history.OrderHistoryScreen
import com.app.garapan.presentation.screen.pesan.PesanScreen
import com.app.garapan.presentation.screen.post_project.PostProjectScreen
import com.app.garapan.presentation.screen.profile.ProfileScreen
import com.app.garapan.presentation.screen.search.SearchScreen
import com.app.garapan.ui.theme.BorderColor
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.MutedText
import com.app.garapan.ui.theme.Surface
import com.app.garapan.ui.theme.White

private data class MainTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterAction: Boolean = false
)

@Composable
fun MainShell(
    rootNavController: NavController,
    viewModel: MainShellViewModel = hiltViewModel()
) {
    val tabNavController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val role = currentUser?.role

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                MainShellEvent.NavigateToLogin -> {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    LaunchedEffect(role) {
        if (role == null) {
            viewModel.resolveSessionIfNeeded()
        }
    }

    if (role == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandNavy)
        }
        return
    }

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let(::leafRoute).orEmpty()
    val tabs = tabsForRole(role)

    val navigateTab: (String) -> Unit = { route ->
        tabNavController.navigateMainTab(route)
    }

    Scaffold(
        containerColor = Surface,
        bottomBar = {
            MainBottomBar(
                tabs = tabs,
                currentRoute = currentRoute,
                onTabSelected = navigateTab
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    navController = rootNavController,
                    onNavigateTab = navigateTab
                )
            }
            composable(
                route = Routes.SEARCH,
                arguments = listOf(
                    navArgument("focus") {
                        type = NavType.StringType
                        defaultValue = Routes.SEARCH_FOCUS_BROWSE
                    }
                )
            ) {
                SearchScreen(
                    navController = rootNavController,
                    showBackButton = false
                )
            }
            composable(Routes.editServiceRoute("new")) {
                RoleGuard(
                    allowedRoles = setOf(Role.MAHASISWA),
                    navController = tabNavController,
                    authNavController = rootNavController,
                    fallbackRoute = Routes.HOME
                ) {
                    EditServiceScreen(navController = tabNavController)
                }
            }
            composable(Routes.ORDER_HISTORY) {
                OrderHistoryScreen(
                    navController = rootNavController,
                    showBackButton = false
                )
            }
            composable(Routes.PESAN) {
                PesanScreen(navController = rootNavController)
            }
            composable(Routes.POST_PROJECT) {
                RoleGuard(
                    allowedRoles = setOf(Role.KLIEN, Role.ADMIN),
                    navController = tabNavController,
                    authNavController = rootNavController,
                    fallbackRoute = Routes.HOME
                ) {
                    PostProjectScreen(
                        navController = tabNavController,
                        rootNavController = rootNavController
                    )
                }
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    navController = rootNavController,
                    showBackButton = false
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    tabs: List<MainTabItem>,
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = White,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = BorderColor,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        )
    ) {
        tabs.forEach { tab ->
            if (tab.isCenterAction) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .clickable { onTabSelected(tab.route) }
                            .background(BrandNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.Plus,
                            contentDescription = tab.label,
                            tint = White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                val selected = isTabSelected(currentRoute, tab.route)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab.route) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandNavy,
                        selectedTextColor = BrandNavy,
                        unselectedIconColor = MutedText,
                        unselectedTextColor = MutedText,
                        indicatorColor = BrandNavy.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

private fun tabsForRole(role: Role): List<MainTabItem> =
    when (role) {
        Role.KLIEN -> listOf(
            MainTabItem(Routes.HOME, "Home", Lucide.House, Lucide.House),
            MainTabItem(
                Routes.ORDER_HISTORY,
                "Pesanan",
                Lucide.ReceiptText,
                Lucide.ReceiptText
            ),
            MainTabItem(Routes.POST_PROJECT, "New", Lucide.Plus, Lucide.Plus, isCenterAction = true),
            MainTabItem(
                Routes.PESAN,
                "Chat",
                Lucide.MessageCircle,
                Lucide.MessageCircle
            ),
            MainTabItem(Routes.PROFILE, "Profile", Lucide.User, Lucide.User)
        )
        Role.ADMIN -> listOf(
            MainTabItem(Routes.HOME, "Home", Lucide.House, Lucide.House),
            MainTabItem(Routes.searchRoute(), "Cari Jasa", Lucide.Search, Lucide.Search),
            MainTabItem(Routes.POST_PROJECT, "New", Lucide.Plus, Lucide.Plus, isCenterAction = true),
            MainTabItem(
                Routes.ORDER_HISTORY,
                "Pesanan",
                Lucide.ReceiptText,
                Lucide.ReceiptText
            ),
            MainTabItem(Routes.PROFILE, "Profile", Lucide.User, Lucide.User)
        )
        Role.MAHASISWA -> listOf(
            MainTabItem(Routes.HOME, "Home", Lucide.House, Lucide.House),
            MainTabItem(
                Routes.ORDER_HISTORY,
                "Pesanan",
                Lucide.ReceiptText,
                Lucide.ReceiptText
            ),
            MainTabItem(
                Routes.editServiceRoute("new"),
                "New",
                Lucide.Plus,
                Lucide.Plus,
                isCenterAction = true
            ),
            MainTabItem(
                Routes.PESAN,
                "Chat",
                Lucide.MessageCircle,
                Lucide.MessageCircle
            ),
            MainTabItem(Routes.PROFILE, "Profile", Lucide.User, Lucide.User)
        )
    }

internal fun mainTabLabelsForRole(role: Role): List<String> =
    tabsForRole(role).map { it.label }

private fun NavController.navigateMainTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun leafRoute(route: String?): String {
    val base = route?.substringBefore('?').orEmpty()
    return base.substringAfterLast('/').ifBlank { base }
}

private fun isTabSelected(currentRoute: String, tabRoute: String): Boolean =
    leafRoute(currentRoute) == leafRoute(tabRoute)
