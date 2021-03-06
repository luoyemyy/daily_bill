package com.github.luoyemyy.bill.app

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.LocaleList
import android.util.Log
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.AppInfo

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppInfo.init(this)

        Bus.addDebugListener(object : Bus.DebugListener {
            override fun onPost(event: String, callbacks: List<Bus.Callback>) {
                Log.e("App", "onPost:  event=$event, callbacks=${callbacks.map { it.interceptEvent() }}")
            }

            override fun onRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>) {
                Log.e("App", "onRegister:  current=${currentCallback.interceptEvent()}, callbacks=${allCallbacks.map { it.interceptEvent() }}")
            }

            override fun onUnRegister(currentCallback: Bus.Callback, allCallbacks: List<Bus.Callback>) {
                Log.e("App", "onUnRegister:  current=${currentCallback.interceptEvent()}, callbacks=${allCallbacks.map { it.interceptEvent() }}")
            }
        })

        registerReceiver(object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
//                Log.e("App", "onReceive:  locale change ${LocaleList.getDefault()}")
            }
        }, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
    }
}