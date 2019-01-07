package com.github.luoyemyy.bill.activity.main

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.databinding.ActivityMainBinding
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult

class MainActivity : BaseActivity(), BusResult {

    companion object {
        const val OPEN_DRAWER = "com.github.luoyemyy.bill.activity.main.OPEN_DRAWER"
        const val CLOSE_DRAWER = "com.github.luoyemyy.bill.activity.main.CLOSE_DRAWER"
        const val DISABLE_DRAWER = "com.github.luoyemyy.bill.activity.main.DISABLE_DRAWER"
        const val ENABLE_DRAWER = "com.github.luoyemyy.bill.activity.main.ENABLE_DRAWER"
    }

    private lateinit var mBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.navigationView.setupWithNavController(findNavController(R.id.navFragment))

        Bus.addCallback(lifecycle, this, OPEN_DRAWER, CLOSE_DRAWER, DISABLE_DRAWER, ENABLE_DRAWER)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            OPEN_DRAWER -> {
            }
            CLOSE_DRAWER -> {
            }
            DISABLE_DRAWER -> {
            }
            ENABLE_DRAWER -> {
            }
        }
    }

}
