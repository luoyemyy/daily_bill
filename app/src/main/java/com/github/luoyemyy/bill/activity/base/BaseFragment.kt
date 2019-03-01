package com.github.luoyemyy.bill.activity.base

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    fun getTouchX() = (requireActivity() as? BaseActivity)?.touchX ?: 0
    fun getTouchY() = (requireActivity() as? BaseActivity)?.touchY ?: 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}