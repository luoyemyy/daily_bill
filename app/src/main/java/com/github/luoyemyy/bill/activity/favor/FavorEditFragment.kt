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
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter

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
                addTextChangedListener(object : TextChangeAdapter() {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAdd.isEnabled = s?.length ?: 0 > 0
                    }
                })
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
        mPresenter.load(arguments)
        mPresenter.getLabels()
    }

    class Presenter(var app: Application) : AbstractPresenter<Favor>(app) {

        private val mFavorDao = getFavorDao(app)
        private val mLabelDap = getLabelDao(app)

        private var mId = 0L
        private var mLabelRelations: List<LabelRelation>? = null

        val labelLiveData = MutableLiveData<List<Label>>()
        val resultLiveData = MutableLiveData<Boolean>()

        override fun load(bundle: Bundle?) {
            val id = bundle?.getLong("id")?.apply {
                mId = this
            } ?: return
            runOnWorker {
                data.postValue(getFavorDao(app).get(id))
            }
        }

        fun getLabels() {
            runOnWorker {
                val labelIds = mFavorDao.getLabelRelation(mId).apply {
                    mLabelRelations = this
                }.map { it.labelId }
                val labels = mLabelDap.getAll(userId = UserInfo.getUserId(app))
                labels.forEach {
                    if (labelIds.contains(it.id)) {
                        it.selected = true
                    }
                }
                labelLiveData.postValue(labels)
            }
        }

        private fun getCheckedLabels(): List<Label>? = labelLiveData.value?.filter { it.selected }

        fun add(money: String?, desc: String?) {
            if (money.isNullOrEmpty()) {
                app.toast(R.string.main_money_tip)
                return
            }
            runOnWorker {
                val favor = mFavorDao.get(mId) ?: return@runOnWorker
                favor.money = money.toDouble()
                favor.description = desc

                val labelIds = mLabelRelations?.mapTo(mutableSetOf()) { it.labelId } ?: mutableSetOf<Long>()
                var deleteRelations: Set<Long>? = null
                var addRelations: List<LabelRelation>? = null
                getCheckedLabels()?.apply {
                    favor.summary = summary(favor.money, this, desc)
                    val selectLabelIds = this.mapTo(mutableSetOf()) { it.id }
                    deleteRelations = labelIds.let {
                        it.removeAll(selectLabelIds)
                        it
                    }
                    addRelations = selectLabelIds.let {
                        it.removeAll(labelIds)
                        it.map { id -> LabelRelation(0, 2, mId, id) }
                    }
                } ?: let {
                    favor.summary = summary(favor.money, null, desc)
                    deleteRelations = labelIds
                }
                deleteRelations?.apply {
                    mLabelRelations?.filter { it.labelId in this }?.apply {
                        mFavorDao.deleteLabelRelation(this)
                    }
                }
                addRelations?.apply {
                    mFavorDao.addLabelRelation(this)
                }

                mFavorDao.update(listOf(favor))
                resultLiveData.postValue(true)
                Bus.post(BusEvent.EDIT_FAVOR, longValue = favor.id)
            }
        }
    }
}