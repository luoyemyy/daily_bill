package com.github.luoyemyy.bill.activity.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.activity.user.UserAddFragment
import com.github.luoyemyy.bill.activity.user.UserChangeFragment
import com.github.luoyemyy.bill.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        mBinding.apply {
            viewPager.adapter = ViewPagerAdapter()
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    inner class ViewPagerAdapter : FragmentPagerAdapter(supportFragmentManager) {
        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment {
            return if (position == 0) UserAddFragment.newInstanceFromLogin() else UserChangeFragment.newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (position == 0) getString(R.string.user_add) else getString(R.string.user_change)
        }
    }
}