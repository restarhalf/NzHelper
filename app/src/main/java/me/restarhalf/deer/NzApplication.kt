package me.restarhalf.deer

import android.app.Application
import me.restarhalf.deer.ui.MD3.util.NotificationUtil

class NzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在应用启动时创建通知渠道
        NotificationUtil.createChannel(this)
    }
}