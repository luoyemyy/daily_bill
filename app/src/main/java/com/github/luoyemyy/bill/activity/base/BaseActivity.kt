package com.github.luoyemyy.bill.activity.base

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.ext.autoCloseKeyboardAndClearFocus

abstract class BaseActivity : AppCompatActivity() {

    var touchX: Int = 0
    var touchY: Int = 0

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        touchX = ev?.rawX?.toInt() ?: 0
        touchY = ev?.rawY?.toInt() ?: 0
        autoCloseKeyboardAndClearFocus(ev)
        return super.dispatchTouchEvent(ev)
    }
}