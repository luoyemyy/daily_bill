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
import com.github.luoyemyy.bill.util.TextChangeAdapter
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.result
import com.github.luoyemyy.mvp.single

class LabelAddFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLabelAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLabelAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
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
                mPresenter.add(name)
            }
        }
    }

    class Presenter(var app: Application) : AbstractPresenter<Boolean>(app) {

        fun add(name: String) {
            single {
                val label = Label(0, UserInfo.getUserId(app), name, 1, 0)
                getLabelDao(app).add(label)
            }.result { _, value ->
                Bus.post(BusEvent.ADD_LABBEL, longValue = value ?: 0)
                data.postValue(true)
            }
        }
    }
}