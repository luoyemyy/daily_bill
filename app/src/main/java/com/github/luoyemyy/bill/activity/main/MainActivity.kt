package com.github.luoyemyy.bill.activity.main

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.toolbar.setupWithNavController(findNavController(R.id.navFragment), mBinding.drawerLayout)
        mBinding.navigationView.setupWithNavController(findNavController(R.id.navFragment))
    }

    override fun onSupportNavigateUp() = findNavController(R.id.navFragment).navigateUp()
}
