package com.github.luoyemyy.bill.activity.main

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var mBinding: FragmentMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.toolbar.setNavigationOnClickListener {
            (mBinding.toolbar.navigationIcon as? AnimatedVectorDrawable)?.apply {
                Handler().postDelayed({
                    mBinding.toolbar.setNavigationIcon(R.drawable.animator_menu_back)
                }, 450)
            }?.start()
        }
    }

}