package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.activity.main.MainActivity
import com.github.luoyemyy.bill.databinding.FragmentUserAddBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.db.getUserDao
import com.github.luoyemyy.bill.util.*
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.mvp.Flag
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.runOnWorker

class UserAddFragment : BaseFragment() {

    private lateinit var mBinding: FragmentUserAddBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstanceFromLogin() = UserAddFragment().apply {
            arguments = bundleOf(Pair("isLogin", true))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            if (it == Flag.SUCCESS) {
                if (mPresenter.isLogin(arguments)) {
                    startActivity(Intent(context, MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    findNavController().navigateUp()
                }
            }
        })

        mBinding.apply {
            layoutName.editText?.apply {
                setKeyAction(requireActivity())
                enableSubmit(btnAdd)
            }
            btnAdd.setOnClickListener {
                mPresenter.add(layoutName.editText?.text?.toString(), switchView.isChecked)
            }
        }

        if (mPresenter.isLogin(arguments)) {
            mBinding.switchView.hide()
        } else {
            mBinding.switchView.isChecked = false
        }
        mBinding.layoutChips.chips(mPresenter.getLabels())
    }

    class Presenter(var app: Application) : MvpSimplePresenter<String>(app) {

        private var mLabels = app.resources.getStringArray(R.array.suggest_label).mapTo(mutableListOf()) { Label(name = it) }
        private val mUserDao = getUserDao(app)
        private val mLabelDao = getLabelDao(app)

        fun isLogin(bundle: Bundle?): Boolean {
            return bundle?.getBoolean("isLogin") == true
        }

        fun getLabels(): List<Label> {
            return mLabels
        }

        private fun getSelectedLabels(userId: Long): List<Label> {
            return mLabels.filter { it.selected }.mapIndexed { index, label ->
                label.userId = userId
                label.show = 1
                label.sort = index + 1
                label
            }
        }

        fun add(name: String?, setDefault: Boolean) {
            if (name == null) return
            runOnWorker {
                val user = mUserDao.getByRowId(mUserDao.add(User(0, name, 0))) ?: return@runOnWorker
                getSelectedLabels(user.id).apply {
                    if (isNotEmpty()) {
                        mLabelDao.addAll(this)
                    }
                }
                if (setDefault) {
                    UserInfo.setDefaultUser(app, user.id)
                    Bus.post(BusEvent.CHANGE_USER)
                }
                flag.postValue(Flag.SUCCESS)
                Bus.post(BusEvent.ADD_USER)
            }
        }
    }
}