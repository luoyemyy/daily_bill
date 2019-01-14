package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.databinding.FragmentUserEditBinding
import com.github.luoyemyy.bill.db.getDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.TextChangeAdapter
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter

class UserEditFragment : Fragment() {

    private lateinit var mBinding: FragmentUserEditBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstance() = UserEditFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserEditBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            findNavController().navigateUp()
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
                mPresenter.edit(name)
            }
        }
        mBinding.name = UserInfo.getUsername(requireContext())
    }

    class Presenter(var app: Application) : AbstractPresenter<Boolean>(app) {

        private val dao = getDao(app)

        fun edit(name: String) {
            val userId = UserInfo.getUserId(app)
            if (userId == 0L) {
                return
            }
            runOnWorker {
                val user = dao.getUser(userId)
                if (user != null) {
                    user.nickname = name
                    if (dao.updateUser(user) > 0) {
                        UserInfo.saveUser(app, user)
                        data.postValue(true)
                        Bus.post(BusEvent.UPDATE_USER)
                    }
                }
            }
        }
    }
}