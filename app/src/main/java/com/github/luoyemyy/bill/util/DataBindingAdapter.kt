package com.github.luoyemyy.bill.util

import android.view.View
import androidx.databinding.BindingAdapter

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter("is_show")
    fun isShow(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("is_hide")
    fun isHide(view: View, hide: Boolean) {
        view.visibility = if (hide) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("is_invisible")
    fun isInvisible(view: View, invisible: Boolean) {
        view.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
    }
}