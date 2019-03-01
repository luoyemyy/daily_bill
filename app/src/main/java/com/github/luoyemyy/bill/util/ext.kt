package com.github.luoyemyy.bill.util

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEachIndexed
import androidx.core.view.plusAssign
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.ext.hideKeyboard
import com.github.luoyemyy.mvp.recycler.LinearDecoration
import com.github.luoyemyy.mvp.recycler.RecyclerPresenterSupport
import com.github.luoyemyy.mvp.recycler.setLinearManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.lang.reflect.Method
import java.text.DecimalFormat
import kotlin.math.abs

fun summary(context: Context, money: Double, labels: List<Label>?, desc: String?): String {
    val join = labels?.mapTo(mutableListOf()) { it.name }?.apply { add(desc) }?.filter { !it.isNullOrEmpty() }?.joinToString("-")
    return context.getString(R.string.money_unit) + formatMoney(money) + (if (join.isNullOrEmpty()) "" else "：$join")
}

fun formatMoney(money: Double): String {
    return DecimalFormat("0.00").format(money)
}

fun formatMoney2(money: Double): String {
    return DecimalFormat("0.##").format(money)
}

fun RecyclerView.setup(decoration: RecyclerView.ItemDecoration = LinearDecoration.middle(context), hasFixed: Boolean = false) {
    setHasFixedSize(hasFixed)
    setLinearManager()
    addItemDecoration(decoration)
}

fun SwipeRefreshLayout.setup(presenter: RecyclerPresenterSupport<*>) {
    setOnRefreshListener { presenter.loadRefresh() }
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

fun EditText.enableSubmit(enableView: View) {
    addTextChangedListener(object : TextChangeAdapter() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableView.isEnabled = s?.length ?: 0 > 0
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

fun ChipGroup.chips(chips: List<Label>?) {
    if (chips == null || chips.isEmpty()) {
        removeAllViews()
    } else {
        val i = chips.size - childCount
        if (i > 0) {
            val inflater = LayoutInflater.from(context)
            for (x in 0 until i) {
                this += inflater.inflate(R.layout.layout_chip_label, this, false)
            }
        } else if (i < 0) {
            removeViews(0, abs(i))
        }
        forEachIndexed { index, view ->
            val chip = chips[index]
            (view as? Chip)?.apply {
                text = chip.name
                isChecked = chip.selected
                setOnCheckedChangeListener { _, isChecked ->
                    chip.selected = isChecked
                }
            }
        }
    }
}
