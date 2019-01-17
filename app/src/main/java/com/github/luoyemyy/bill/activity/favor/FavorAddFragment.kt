package com.github.luoyemyy.bill.activity.favor

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
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
        mPresenter.data.observe(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.labelLiveData.observe(this, Observer {
            chips(mBinding.layoutChips, it)
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
            btnAdd.setOnClickListener {
                mPresenter.add(layoutMoney.editText?.text?.toString(), layoutDesc.editText?.text?.toString())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.getLabels()
    }

    class Presenter(var app: Application) : AbstractPresenter<Boolean>(app) {

        private val mFavorDao = getFavorDao(app)
        private val mLabelDap = getLabelDao(app)

        val labelLiveData = MutableLiveData<List<Label>>()

        private fun getCheckedLabels(): List<Label>? = labelLiveData.value?.filter { it.selected }

        fun getLabels() {
            runOnWorker {
                labelLiveData.postValue(mLabelDap.getAll(userId = UserInfo.getUserId(app)))
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
                    val relations = this.map { LabelRelation(type = 2, relationId = addFavor.id, labelId = it.id) }
                    mFavorDao.addLabelRelation(relations)
                    mFavorDao.update(listOf(addFavor))
                }
                data.postValue(true)
                Bus.post(BusEvent.ADD_FAVOR, longValue = addFavor.id)
            }
        }
    }
}