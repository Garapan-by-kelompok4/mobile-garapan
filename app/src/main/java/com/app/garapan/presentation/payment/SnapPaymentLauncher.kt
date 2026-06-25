package com.app.garapan.presentation.payment

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.app.garapan.BuildConfig

object SnapPaymentLauncher {
    fun redirectUrl(snapToken: String): String {
        val host = if (BuildConfig.MIDTRANS_IS_SANDBOX) {
            "https://app.sandbox.midtrans.com"
        } else {
            "https://app.midtrans.com"
        }
        return "$host/snap/v2/vtweb/$snapToken"
    }

    fun open(activity: Activity, snapToken: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(activity, Uri.parse(redirectUrl(snapToken)))
    }
}
