package com.github.luoyemyy.bill.activity.favor

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentFavorAddBinding
import com.github.luoyemyy.bill.db.*
import com.github.luoyemyy.bill.util.*
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mvp.getPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.runOnWorker

class FavorEditFragment : BaseFragment() {
    private lateinit var mBinding: FragmentFavorAddBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentFavorAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.labelLiveData.observe(this, Observer {
            mBinding.layoutChips.chips(it)
        })
        mPresenter.setDataObserver(this, Observer {
            mBinding.entity = it
            if (it != null) {
                mBinding.money = formatMoney2(it.money)
            }
        })

        mBinding.apply {
            layoutMoney.editText?.apply {
                limitMoney()
                enableSubmit(btnAdd)
            }
            layoutDesc.editText?.apply {
                setKeyAction(requireActivity())
            }
            btnAdd.isEnabled = true
            btnAdd.setOnClickListener {
                mPresenter.add(layoutMoney.editText?.text?.toString(), layoutDesc.editText?.text?.toString())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.loadInit(arguments)
    }

    class Presenter(var app: Application) : MvpSimplePresenter<Favor>(app) {

        private val mFavorDao = getFavorDao(app)
        private val mLabelDao = getLabelDao(app)

        private var mId = 0L
        private var mFavor: Favor? = null
        private var mFavorLabels: List<Label>? = null

        val labelLiveData = MutableLiveData<List<Label>>()

        override fun loadData(loadType: LoadType, bundle: Bundle?) {
            val id = bundle?.getLong("id")?.apply { mId = this } ?: return
            mFavor = mFavorDao.get(id) ?: return
            data.postValue(mFavor)
            val labelIds = mFavorDao.getLabels(mId).apply { mFavorLabels = this }.map { it.id }
            val labels = mLabelDao.getAll(UserInfo.getUserId(app))
            labels.forEach {
                if (labelIds.contains(it.id)) {
                    it.selected = true
                }
            }
            labelLiveData.postValue(labels)
        }

        private fun getCheckedLabels(): List<Label>? = labelLiveData.value?.filter { it.selected }

        fun add(money: String?, desc: String?) {
            if (money.isNullOrEmpty()) {
                app.toast(R.string.main_money_tip)
                return
            }
            runOnWorker {
                val favor = mFavor ?: return@runOnWorker
                favor.money = money.toDouble()
                favor.description = desc

                val oldLabelIds = mFavorLabels?.mapTo(mutableListOf()) { it.id } ?: mutableListOf<Long>()
                var deleteLabelIds: List<Long>? = null
                var addLabelIds: List<Long>? = null
                getCheckedLabels()?.apply labels@{
                    favor.summary = summary(app, favor.money, this, desc)
                    addLabelIds = this.mapTo(mutableListOf()) { it.id }.apply { removeAll(oldLabelIds) }
                    deleteLabelIds = oldLabelIds.apply { removeAll(this@labels.map { it.id }) }
                } ?: let {
                    favor.summary = summary(app, favor.money, null, desc)
                    deleteLabelIds = oldLabelIds
                }

                deleteLabelIds?.apply {
                    mFavorDao.deleteLabelRelation(favor.id, this)
                }
                addLabelIds?.apply {
                    if (isNotEmpty()) {
                        mFavorDao.addLabelRelation(map { LabelRelation(0, 2, favor.id, it) })
                    }
                }

                mFavorDao.update(listOf(favor))
                flag.postValue(1)
                Bus.post(BusEvent.EDIT_FAVOR, longValue = favor.id)
            }
        }
    }
}