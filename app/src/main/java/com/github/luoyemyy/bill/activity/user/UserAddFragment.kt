package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.activity.main.MainActivity
import com.github.luoyemyy.bill.databinding.FragmentUserAddBinding
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.TextChangeAdapter
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter

class UserAddFragment : Fragment() {

    private lateinit var mBinding: FragmentUserAddBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstanceFromLogin() = UserAddFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isLogin", true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            if (isLogin()) {
                startActivity(Intent(context, MainActivity::class.java))
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        })

        mBinding.apply {
            layoutName.editText?.apply {
                setKeyAction(requireActivity())
                addTextChangedListener(object : TextChangeAdapter() {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAdd.isEnabled = count > 0
                    }
                })
            }
            btnAdd.setOnClickListener {
                val name = layoutName.editText?.text?.toString() ?: return@setOnClickListener
                mPresenter.add(name)
            }
        }

        if (isLogin()) {
            mBinding.switchView.hide()
        }
    }

    private fun isLogin(): Boolean = arguments?.getBoolean("isLogin") == true

    class Presenter(var app: Application) : AbstractPresenter<Boolean>(app) {

        fun add(name: String) {
            runOnWorker {
                UserInfo.setDefaultUser(app, name) {
                    Bus.post(BusEvent.UPDATE_USER)
                    data.postValue(true)
                }
            }
        }
    }
}