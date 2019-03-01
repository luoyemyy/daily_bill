package com.github.luoyemyy.bill.activity.label

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentLabelAddBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.*
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.runOnWorker

class LabelAddFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLabelAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLabelAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer { findNavController().navigateUp() })

        mBinding.apply {
            layoutName.editText?.apply {
                setKeyAction(requireActivity())
                enableSubmit(btnAdd)
            }
            btnAdd.setOnClickListener {
                mPresenter.add(layoutName.editText?.text?.toString())
            }
        }
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Boolean>(app) {

        fun add(name: String?) {
            if (name == null) return
            runOnWorker {
                val label = Label(0, UserInfo.getUserId(app), name, 1, 0)
                Bus.post(BusEvent.ADD_LABEL, longValue = getLabelDao(app).add(label))
                flag.postValue(1)
            }
        }
    }
}