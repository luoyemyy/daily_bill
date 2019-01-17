package com.github.luoyemyy.bill.util

import android.app.Activity
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.ext.hideKeyboard
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.lang.reflect.Method
import java.text.DecimalFormat

fun formatMoney(money: Double): String {
    return DecimalFormat("0.00").format(money)
}

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

fun EditText.limitMoney() {
    addTextChangedListener(object : TextChangeAdapter() {
        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            val str = s.toString()
            if (str == ".") { //禁止第一个字符为.
                this@limitMoney.setText("")
                return
            }
            val a = str.split(".")
            if (a.size == 2 && a[1].length > 2) { //小数部分不能超过2位
                val reset = a[0] + "." + a[1].substring(0, 2)
                this@limitMoney.setText(reset)
                this@limitMoney.setSelection(reset.length)
            }
        }
    })
}

fun getMethod(obj: Any, name: String, vararg args: Class<*>): Method {
    return obj::class.java.getMethod(name, *args).apply {
        isAccessible = true
    }
}

fun getField(obj: Any, name: String): Any {
    return obj::class.java.getDeclaredField(name).apply {
        isAccessible = true
    }.get(obj)
}

fun PopupMenu.showAnchor(anchor: View, touchX: Int, touchY: Int) {
    val location = intArrayOf(0, 0)
    anchor.getLocationInWindow(location)
    val x = touchX - location[0]
    val y = touchY - location[1] - anchor.height

    val popup = getField(this, "mPopup")
    getMethod(popup, "show", Int::class.java, Int::class.java).invoke(popup, x, y)
}

fun chips(chipGroup: ChipGroup, chips: List<Label>?) {
    if (chips == null || chips.isEmpty()) return
    chipGroup.removeAllViews()
    val inflater = LayoutInflater.from(chipGroup.context)
    chips.forEach {
        val chip = inflater.inflate(R.layout.layout_chip_label, chipGroup, false) as Chip
        chip.text = it.name
        chip.isChecked = it.selected

        chipGroup.addView(chip)
        chip.setOnCheckedChangeListener { _, isChecked ->
            it.selected = isChecked
        }
    }
}