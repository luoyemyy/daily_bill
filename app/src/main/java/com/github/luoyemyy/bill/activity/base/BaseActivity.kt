package com.github.luoyemyy.bill.activity.base

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val focusView = currentFocus as? EditText
        val x = ev?.rawX?.toInt() ?: 0
        val y = ev?.rawY?.toInt() ?: 0
        if (x >= 0 && y >= 0 && !isTouchEditText(x, y) && focusView != null && !pointInView(x, y, focusView)) {
            hideKeyboard()
            focusView.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isTouchEditText(x: Int, y: Int): Boolean {
        val root = window.peekDecorView() as? ViewGroup ?: return false
        return isTouchEditText(x, y, root)
    }

    private fun isTouchEditText(x: Int, y: Int, viewGroup: ViewGroup): Boolean {
        val count = viewGroup.childCount
        (0 until count).forEach {
            val view = viewGroup.getChildAt(it)
            if (pointInView(x, y, view)) {
                return when (view) {
                    is EditText -> true
                    is ViewGroup -> isTouchEditText(x, y, view)
                    else -> false
                }
            }
        }
        return false
    }

    private fun pointInView(x: Int, y: Int, view: View): Boolean {
        val location = intArrayOf(0, 0)
        view.getLocationOnScreen(location)
        val rect = Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(x, y)
    }

    private fun hideKeyboard() {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusView = currentFocus
        if (manager.isActive && focusView != null && focusView.windowToken != null) {
            manager.hideSoftInputFromWindow(focusView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun showKeyboard() {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusView = currentFocus
        if (manager.isActive && focusView != null && focusView.windowToken != null) {
            manager.showSoftInput(focusView, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}