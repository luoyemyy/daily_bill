package com.github.luoyemyy.bill.activity.main

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.databinding.ActivityMainBinding
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult

class MainActivity : BaseActivity(), BusResult {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mTxtName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(mBinding.toolbar)

        mBinding.toolbar.setupWithNavController(findNavController(R.id.navFragment), mBinding.drawerLayout)
        mBinding.navigationView.setupWithNavController(findNavController(R.id.navFragment))
        mTxtName = mBinding.navigationView.getHeaderView(0).findViewById(R.id.txtName)
        mTxtName.setOnClickListener {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START)
            findNavController(R.id.navFragment).navigate(R.id.userEdit)
        }

        mTxtName.text = UserInfo.getUsername(this)
        Bus.addCallback(lifecycle, this, BusEvent.CHANGE_USER)
    }

    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return false
    }

    override fun busResult(event: String, msg: BusMsg) {
        mTxtName.text = UserInfo.getUsername(this)
    }
}
