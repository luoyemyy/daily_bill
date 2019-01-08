package com.github.luoyemyy.bill.activity.base

import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.main.MainActivity

abstract class BaseFragment : Fragment() {

    fun setupToolbarTitle(@StringRes titleId: Int) {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar) ?: return
        if (titleId != 0) {
            toolbar.setTitle(titleId)
        }
    }

    fun setupToolbarTitle(title: String? = null) {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar) ?: return
        if (title != null) {
            toolbar.title = title
        }
    }

    fun setupToolbarBack() {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar) ?: return
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            toolbar.findNavController().navigateUp()
        }
    }

    fun setupToolbarDrawer() {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar) ?: return
        toolbar.setupWithNavController(
            toolbar.findNavController(),
            (requireActivity() as MainActivity).mBinding.drawerLayout
        )
    }

    fun setupToolbarMenu(@MenuRes menuId: Int, menuClick: (itemId: Int) -> Boolean) {
        val toolbar = view?.findViewById<Toolbar>(R.id.toolbar) ?: return
        toolbar.inflateMenu(menuId)
        toolbar.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener menuClick(it.itemId)
        }
    }
}