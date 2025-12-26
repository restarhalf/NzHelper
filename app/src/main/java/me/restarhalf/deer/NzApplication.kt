package me.restarhalf.deer

import android.app.Application
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.ui.util.NotificationUtil

class NzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeRepository.init(this)
        // 在应用启动时创建通知渠道
        NotificationUtil.createChannel(this)
    }
}