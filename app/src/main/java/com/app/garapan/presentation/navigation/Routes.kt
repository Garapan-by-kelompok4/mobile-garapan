package com.app.garapan.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETUP = "setup"
    const val HOME = "home"

    fun setupRoute(role: String) = "$SETUP?role=$role"
}
