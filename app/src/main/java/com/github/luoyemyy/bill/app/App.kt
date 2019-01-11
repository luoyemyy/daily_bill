package com.github.luoyemyy.bill.app

import android.app.Application
import com.github.luoyemyy.config.AppInfo

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)
    }
}