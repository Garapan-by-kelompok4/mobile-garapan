package com.app.garapan.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object GarapanNotificationChannels {
    const val GENERAL_CHANNEL_ID = "garapan_notifications"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            GENERAL_CHANNEL_ID,
            "Notifikasi GARAPAN",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Update pesanan, ulasan, proyek, dan chat GARAPAN"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
