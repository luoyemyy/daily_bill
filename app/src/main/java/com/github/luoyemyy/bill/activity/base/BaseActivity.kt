package com.github.luoyemyy.bill.activity.base

import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.ext.hideKeyboard
import com.github.luoyemyy.ext.pointInEditText

abstract class BaseActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val x = ev?.rawX?.toInt() ?: -1
        val y = ev?.rawY?.toInt() ?: -1
        val viewGroup = window.peekDecorView() as? ViewGroup
        if (x >= 0 && y >= 0 && viewGroup != null && !viewGroup.pointInEditText(x, y)) {
            hideKeyboard()
            (currentFocus as? EditText)?.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}