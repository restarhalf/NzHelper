package me.restarhalf.deer.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationUtil {
    const val CHANNEL_ID = "timer_channel"
    const val CHANNEL_NAME = "计时服务"

    fun createChannel(context: Context) {
        val chan = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(chan)
    }
}
