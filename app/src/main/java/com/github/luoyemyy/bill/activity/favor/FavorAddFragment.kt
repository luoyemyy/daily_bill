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

class FavorAddFragment : BaseFragment() {

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
        mPresenter.setListObserver(this, Observer {
            mBinding.layoutChips.chips(it)
        })

        mBinding.apply {
            layoutMoney.editText?.apply {
                limitMoney()
                submitEnable(btnAdd)
            }
            layoutDesc.editText?.apply {
                setKeyAction(requireActivity())
            }
            btnAdd.setOnClickListener {
                mPresenter.add(layoutMoney.editText?.text?.toString(), layoutDesc.editText?.text?.toString())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.getLabels()
    }

    class Presenter(var app: Application) : AbstractPresenter<Label>(app) {

        private val mFavorDao = getFavorDao(app)
        private val mLabelDap = getLabelDao(app)

        private fun getCheckedLabels(): List<Label>? = list.value?.filter { it.selected }

        fun getLabels() {
            runOnWorker {
                list.postValue(mLabelDap.getAll(userId = UserInfo.getUserId(app)))
            }
        }

        fun add(money: String?, desc: String?) {
            if (money.isNullOrEmpty()) {
                app.toast(R.string.main_money_tip)
                return
            }
            runOnWorker {
                val rowId = mFavorDao.add(Favor(0, UserInfo.getUserId(app), money.toDouble(), desc))
                val addFavor = mFavorDao.getByRowId(rowId) ?: return@runOnWorker
                getCheckedLabels()?.apply {
                    addFavor.summary = summary(money.toDouble(), this, desc)
                    mFavorDao.update(listOf(addFavor))
                    this.map { LabelRelation(type = 2, relationId = addFavor.id, labelId = it.id) }.apply {
                        if (isNotEmpty()) {
                            mFavorDao.addLabelRelation(this)
                        }
                    }
                }
                flag.postValue(1)
                Bus.post(BusEvent.ADD_FAVOR, longValue = addFavor.id)
            }
        }
    }
}