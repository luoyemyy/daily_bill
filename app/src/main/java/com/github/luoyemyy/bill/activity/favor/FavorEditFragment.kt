package com.github.luoyemyy.bill.activity.favor

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentFavorAddBinding
import com.github.luoyemyy.bill.databinding.FragmentLabelAddBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.TextChangeAdapter
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.result
import com.github.luoyemyy.mvp.single

class FavorEditFragment : BaseFragment() {
    private lateinit var mBinding: FragmentFavorAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentFavorAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.resultLiveData.observe(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.data.observe(this, Observer {

        })


        mPresenter.load(arguments)
    }

    class Presenter(var app: Application) : AbstractPresenter<Label>(app) {

        val resultLiveData = MutableLiveData<Boolean>()

        override fun load(bundle: Bundle?) {
            val id = bundle?.getLong("id") ?: return
            runOnWorker {
                data.postValue(getLabelDao(app).get(id))
            }
        }

        fun add(name: String) {
            val label = data.value ?: return
            single {
                label.name = name
                getLabelDao(app).updateAll(listOf(label))
            }.result { _, _ ->
                Bus.post(BusEvent.EDIT_LABEL, longValue = label.id)
                resultLiveData.postValue(true)
            }
        }
    }
}