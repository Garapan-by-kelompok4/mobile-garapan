package com.app.garapan.presentation.screen.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.garapan.presentation.navigation.Routes
import com.app.garapan.ui.theme.White

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val route = (event as SplashEvent.Navigate).route
            navController.navigate(route) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    // Branding is shown once by the native system splash (see Theme.Garapan.Splash);
    // this route only bridges to the resolved destination, so it stays blank.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    )
}
