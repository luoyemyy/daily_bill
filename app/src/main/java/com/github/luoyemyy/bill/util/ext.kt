package com.github.luoyemyy.bill.util

import android.app.Activity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.github.luoyemyy.ext.hideKeyboard

fun EditText.setKeyAction(activity: Activity, callback: (String) -> Unit = {}) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            clearFocus()
            activity.hideKeyboard()
            callback(text.toString())
        }
        return@setOnEditorActionListener true
    }
}