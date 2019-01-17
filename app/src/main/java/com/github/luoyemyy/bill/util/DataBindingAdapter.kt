package com.github.luoyemyy.bill.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.luoyemyy.bill.R

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter("user_selected")
    fun userSelected(textView: TextView, isDefault: Int) {
        if (isDefault == 1) {
            val right = textView.context.getDrawable(R.drawable.ic_ok)
            right?.setBounds(0, 0, right.intrinsicWidth, right.intrinsicHeight)
            textView.setCompoundDrawables(null, null, right, null)
        }
    }
}