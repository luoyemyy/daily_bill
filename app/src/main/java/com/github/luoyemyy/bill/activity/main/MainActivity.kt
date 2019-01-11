package com.github.luoyemyy.bill.activity.main

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.toolbar.setupWithNavController(findNavController(R.id.navFragment), mBinding.drawerLayout)
        mBinding.navigationView.setupWithNavController(findNavController(R.id.navFragment))
    }

    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
