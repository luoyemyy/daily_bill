package com.github.luoyemyy.bill.activity.base

import android.graphics.drawable.AnimatedVectorDrawable
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.main.MainActivity
import com.github.luoyemyy.bus.Bus

abstract class BaseFragment : Fragment() {

    

    open fun setupToolbarDrawerLayout(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.animator_menu_back)
        toolbar.setNavigationOnClickListener {
            Bus.post(MainActivity.OPEN_DRAWER)
            val anim = toolbar.navigationIcon as? AnimatedVectorDrawable ?: return@setNavigationOnClickListener

        }
    }
}