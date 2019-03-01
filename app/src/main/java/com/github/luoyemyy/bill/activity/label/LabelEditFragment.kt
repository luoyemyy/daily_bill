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
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.MvpSimplePresenter
import com.github.luoyemyy.bill.util.enableSubmit
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.runOnWorker

class LabelEditFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLabelAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLabelAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer { findNavController().navigateUp() })
        mPresenter.setDataObserver(this, Observer { mBinding.layoutName.editText?.setText(it.name) })

        mBinding.apply {
            layoutName.editText?.apply {
                setKeyAction(requireActivity())
                enableSubmit(btnAdd)
            }
            btnAdd.setOnClickListener {
                mPresenter.add(layoutName.editText?.text?.toString())
            }
        }

        mPresenter.loadInit(arguments)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Label>(app) {

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            val id = bundle?.getLong("id") ?: return
            data.postValue(getLabelDao(app).get(id))
        }

        fun add(name: String?) {
            if (name == null) return
            val label = data.value ?: return
            runOnWorker {
                label.name = name
                getLabelDao(app).updateAll(listOf(label))
                Bus.post(BusEvent.EDIT_LABEL, longValue = label.id)
                flag.postValue(1)
            }
        }
    }
}