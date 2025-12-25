package me.neko.nzhelper

import android.app.Application
import me.neko.nzhelper.ui.util.NotificationUtil

class NzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 在应用启动时创建通知渠道
        NotificationUtil.createChannel(this)
    }
}