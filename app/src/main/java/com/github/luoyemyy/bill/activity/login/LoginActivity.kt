package com.github.luoyemyy.bill.activity.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity() {

    private lateinit var mBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        findNavController(R.id.navLoginFragment).addOnDestinationChangedListener { controller, destination, arguments ->
            mBinding.collapsingToolbarLayout.title = destination.label
            controller.navigate(destination.id, arguments)
        }
    }

}