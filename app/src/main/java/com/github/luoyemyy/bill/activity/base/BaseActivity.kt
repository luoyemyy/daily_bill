package com.github.luoyemyy.bill.activity.base

import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.ext.autoCloseKeyboardAndClearFocus

abstract class BaseActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        autoCloseKeyboardAndClearFocus(this, ev)
        return super.dispatchTouchEvent(ev)
    }
}