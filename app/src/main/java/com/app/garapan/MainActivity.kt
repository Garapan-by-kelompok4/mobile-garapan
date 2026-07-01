package com.app.garapan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.app.garapan.notification.GarapanNotificationChannels
import com.app.garapan.notification.GarapanNotificationRouter
import com.app.garapan.presentation.navigation.NavGraph
import com.app.garapan.presentation.screen.auth.SplashViewModel
import com.app.garapan.ui.theme.GarapanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val pendingNotificationRoute = mutableStateOf<String?>(null)
    private val splashViewModel: SplashViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { splashViewModel.uiState.value.isLoading }
        GarapanNotificationChannels.ensureCreated(this)
        requestPostNotificationPermissionIfNeeded()
        pendingNotificationRoute.value = GarapanNotificationRouter.routeFromIntent(intent)
        enableEdgeToEdge()
        setContent {
            GarapanTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    notificationRoute = pendingNotificationRoute.value,
                    onNotificationRouteConsumed = { pendingNotificationRoute.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute.value = GarapanNotificationRouter.routeFromIntent(intent)
    }

    private fun requestPostNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
