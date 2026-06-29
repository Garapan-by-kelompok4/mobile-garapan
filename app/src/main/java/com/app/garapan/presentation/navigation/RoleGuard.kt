package com.app.garapan.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.app.garapan.domain.model.Role
import com.app.garapan.ui.theme.BrandNavy
import com.app.garapan.ui.theme.Surface

@Composable
fun RoleGuard(
    allowedRoles: Set<Role>,
    navController: NavController,
    fallbackRoute: String,
    authNavController: NavController = navController,
    viewModel: RoleGuardViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val role = currentUser?.role

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RoleGuardEvent.NavigateToLogin -> {
                    authNavController.navigate(Routes.LOGIN) {
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

    when {
        role == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandNavy)
            }
        }
        role in allowedRoles -> content()
        else -> {
            LaunchedEffect(role) {
                if (!navController.popBackStack()) {
                    navController.navigate(fallbackRoute) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
