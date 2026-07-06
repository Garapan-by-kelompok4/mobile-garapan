package com.app.garapan.presentation.navigation

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VERIFY_EMAIL = "verify_email?email={email}&token={token}"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password?email={email}&token={token}"
    const val TWO_FACTOR = "two_factor?preAuthToken={preAuthToken}"
    const val SETUP = "setup"
    const val MAIN = "main"
    const val HOME = "home"
    const val SEARCH = "search?focus={focus}"
    const val SEARCH_FOCUS_BROWSE = "browse"
    const val SEARCH_FOCUS_JASA = "jasa"
    const val PESAN = "pesan"
    const val POST_PROJECT = "post_project"
    const val PROFILE = "profile"
    const val PROFILE_PORTFOLIO = "profile_portfolio"
    const val ADD_PORTFOLIO = "add_portfolio"
    const val EDIT_PORTFOLIO = "edit_portfolio/{portfolioId}"
    const val PROFILE_SERVICES = "profile_services"
    const val MY_PROJECTS = "my_projects"
    const val SKILLS = "skills"
    const val EDIT_PROFILE = "edit_profile"
    const val EDIT_SERVICE = "edit_service/{serviceId}"
    const val ORDER_HISTORY = "order_history"
    const val WALLET = "wallet"
    const val ORDER_DETAIL = "order_detail/{pesananId}"
    const val REVIEW = "review/{pesananId}"
    const val ALL_REVIEWS = "all_reviews/{jasaId}"
    const val SECURITY = "security"
    const val CHANGE_PASSWORD = "change_password"
    const val EDIT_PROJECT = "edit_project/{projectId}"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    const val JASA_DETAIL = "jasa_detail/{jasaId}"
    const val CHAT = "chat/{conversationId}?peerName={peerName}&activePesananId={activePesananId}&activeOrderTitle={activeOrderTitle}&activeOrderStatus={activeOrderStatus}"
    const val SUPPORT_WORKER_ID = "admin-1"
    const val CHECKOUT = "checkout/{jasaId}"
    const val BLOG_DETAIL = "blog_detail/{blogId}"
    const val PUBLIC_PROFILE = "public_profile/{userId}"
    const val TOP_WORKERS = "top_workers"
    const val ARTICLE_LIST = "article_list"
    const val DISPUTE = "dispute/{pesananId}"
    const val REPORT_CONTENT = "report_content/{contentType}/{contentId}"
    const val NOTIFICATIONS = "notifications"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLICY = "privacy_policy"

    fun setupRoute(role: String) = "$SETUP?role=$role"
    fun verifyEmailRoute(email: String) = "verify_email?email=${Uri.encode(email)}"
    fun resetPasswordRoute(email: String) = "reset_password?email=${email.encodeQueryValue()}"
    fun twoFactorRoute(preAuthToken: String) = "two_factor?preAuthToken=${Uri.encode(preAuthToken)}"
    fun projectDetailRoute(projectId: String) = "project_detail/$projectId"
    fun editProjectRoute(projectId: String) = "edit_project/$projectId"
    fun jasaDetailRoute(jasaId: String) = "jasa_detail/$jasaId"
    fun chatRoute(
        conversationId: String,
        peerName: String = "",
        activeOrder: com.app.garapan.domain.model.ActiveOrder? = null
    ): String {
        val encodedPeerName = if (peerName.isBlank()) "" else Uri.encode(peerName)
        val encodedTitle = activeOrder?.title?.let { Uri.encode(it) }.orEmpty()
        val status = activeOrder?.status?.name.orEmpty()
        val pesananId = activeOrder?.pesananId.orEmpty()
        return "chat/$conversationId?peerName=$encodedPeerName&activePesananId=$pesananId&activeOrderTitle=$encodedTitle&activeOrderStatus=$status"
    }
    fun supportChatRoute() = chatRoute(
        conversationId = SUPPORT_WORKER_ID
    )
    fun checkoutRoute(jasaId: String) = "checkout/$jasaId"
    fun orderDetailRoute(pesananId: String) = "order_detail/$pesananId"
    fun reviewRoute(pesananId: String) = "review/$pesananId"
    fun allReviewsRoute(jasaId: String) = "all_reviews/$jasaId"
    fun blogDetailRoute(blogId: String) = "blog_detail/$blogId"
    fun publicProfileRoute(userId: String) = "public_profile/$userId"
    fun editServiceRoute(serviceId: String) = "edit_service/$serviceId"
    fun editPortfolioRoute(portfolioId: String) = "edit_portfolio/$portfolioId"
    fun searchRoute(focus: String = SEARCH_FOCUS_BROWSE) = "search?focus=$focus"
    fun disputeRoute(pesananId: String) = "dispute/$pesananId"
    fun reportContentRoute(contentType: String, contentId: String) =
        "report_content/$contentType/$contentId"

    private fun String.encodeQueryValue(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString()).replace("+", "%20")
}
