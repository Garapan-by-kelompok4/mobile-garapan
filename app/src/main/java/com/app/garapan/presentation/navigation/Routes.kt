package com.app.garapan.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETUP = "setup"
    const val HOME = "home"
    const val SEARCH = "search"
    const val PESAN = "pesan"
    const val POST_PROJECT = "post_project"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val ORDER_HISTORY = "order_history"
    const val SECURITY = "security"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    const val JASA_DETAIL = "jasa_detail/{jasaId}"
    const val CHAT = "chat/{workerId}"

    fun setupRoute(role: String) = "$SETUP?role=$role"
    fun projectDetailRoute(projectId: String) = "project_detail/$projectId"
    fun jasaDetailRoute(jasaId: String) = "jasa_detail/$jasaId"
    fun chatRoute(workerId: String) = "chat/$workerId"
}
