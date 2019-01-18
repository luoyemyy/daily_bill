package com.github.luoyemyy.bill.activity.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.activity.login.LoginActivity
import com.github.luoyemyy.bill.databinding.FragmentMainBinding
import com.github.luoyemyy.bill.databinding.FragmentMainRecyclerFavorBinding
import com.github.luoyemyy.bill.db.*
import com.github.luoyemyy.bill.util.*
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.ext.clearTime
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.ext.show
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*
import java.util.*

class MainFragment : BaseFragment(), BusResult {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(RecyclerDecoration.middle(requireContext(), spaceUnit = true))
        }
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.labelLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutAdd.layoutChips.chips(it)
        })
        mPresenter.countLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutCount.entity = it
        })
        mPresenter.addLiveData.observe(this, androidx.lifecycle.Observer {
            if (it) resetInput()
        })
        mPresenter.moneyLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutAdd.layoutMoney.editText?.setText(it)
        })
        mPresenter.descLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutAdd.layoutDesc.editText?.setText(it)
        })
        mPresenter.tipLiveData.observe(this, androidx.lifecycle.Observer {
            //favor tip
            if (!UserInfo.getFavorTip(requireContext())) {
                mBinding.layoutFavorTip.root.show()
                mBinding.layoutFavorTip.btnOk.setOnClickListener {
                    UserInfo.hideFavorTip(requireContext())
                    mBinding.layoutFavorTip.root.hide()
                }
            }
        })

        //add
        mBinding.layoutAdd.layoutMoney.editText?.apply {
            limitMoney()
            addTextChangedListener(object : TextChangeAdapter() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    mPresenter.updateMoney(s.toString())
                }
            })
        }
        mBinding.layoutAdd.layoutDesc.editText?.apply {
            addTextChangedListener(object : TextChangeAdapter() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    mPresenter.updateDesc(s.toString())
                }
            })
        }

        mBinding.layoutAdd.btnAdd.setOnClickListener {
            mPresenter.add(mBinding.layoutAdd.layoutMoney.editText?.text?.toString(), mBinding.layoutAdd.layoutDesc.editText?.text?.toString())
        }
        mBinding.layoutAdd.btnReset.setOnClickListener {
            resetInput()
        }

        if (UserInfo.getUserId(requireContext()) == 0L) {
            //还没有用户，进入创建用户页，或者选择用户页
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        } else {
            mPresenter.getInput()
            mPresenter.getCount()
            mPresenter.getLabel()
            mPresenter.loadInit(arguments)
        }

        Bus.addCallback(lifecycle, this, BusEvent.UPDATE_SHOW_LABEL, BusEvent.UPDATE_SHOW_FAVOR)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.UPDATE_SHOW_LABEL -> mPresenter.getLabel(true, mPresenter.getCheckedLabelIds())
            BusEvent.UPDATE_SHOW_FAVOR -> mPresenter.loadRefresh()
        }
    }

    private fun resetInput() {
        mBinding.layoutAdd.layoutMoney.editText?.setText("")
        mBinding.layoutAdd.layoutDesc.editText?.setText("")
        mPresenter.resetLabel()
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<Favor, FragmentMainRecyclerFavorBinding>(mBinding.recyclerView) {

        override fun enableLoadMore(): Boolean = false

        override fun enableEmpty(): Boolean = false

        override fun bindContentViewHolder(binding: FragmentMainRecyclerFavorBinding, content: Favor, position: Int) {
            binding.entity = content
            binding.executePendingBindings()
        }

        override fun bindItemEvents(vh: VH<FragmentMainRecyclerFavorBinding>) {
            vh.binding?.root?.setOnLongClickListener {
                mPresenter.selectFavorToAdd(vh.adapterPosition)
            }
        }

        override fun onItemClickListener(vh: VH<FragmentMainRecyclerFavorBinding>, view: View?) {
            mPresenter.add(getItem(vh.adapterPosition))
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): FragmentMainRecyclerFavorBinding? {
            return FragmentMainRecyclerFavorBinding.inflate(inflater, parent, false)
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Favor>(app) {

        private val mBillDao = getBillDao(app)
        private val mFavorDao = getFavorDao(app)
        private val mLabelDao = getLabelDao(app)
        private var mMoney: String? = null
        private var mDesc: String? = null

        val labelLiveData = MutableLiveData<List<Label>>()
        val countLiveData = MutableLiveData<Count>()
        val addLiveData = MutableLiveData<Boolean>()
        val tipLiveData = MutableLiveData<Boolean>()
        val moneyLiveData = MutableLiveData<String>()
        val descLiveData = MutableLiveData<String>()

        fun updateMoney(money: String?) {
            mMoney = money
        }

        fun updateDesc(desc: String?) {
            mDesc = desc
        }

        fun getInput() {
            moneyLiveData.postValue(mMoney)
            descLiveData.postValue(mDesc)
        }

        fun getCount(reload: Boolean = false) {
            if (isInitialized() && !reload) {
                countLiveData.postValue(countLiveData.value)
            } else {
                runOnWorker {
                    val userId = UserInfo.getUserId(app)
                    val startTime = getTime(0, 0)
                    val endTimeToday = getTime(1, 0)
                    val endTImeMonth = getTime(0, 1)
                    val countToday = mBillDao.sumMoneyByDate(userId, startTime, endTimeToday)
                    val countMonth = mBillDao.sumMoneyByDate(userId, startTime, endTImeMonth)
                    countLiveData.postValue(Count(formatMoney(countToday), formatMoney(countMonth)))
                    setInitialized()
                }
            }
        }

        fun getLabel(reload: Boolean = false, checkedLabelIds: List<Long>? = null) {
            if (isInitialized() && !reload) {
                labelLiveData.postValue(labelLiveData.value)
            } else {
                runOnWorker {
                    val showLabels = mLabelDao.getShow(UserInfo.getUserId(app))
                    if (showLabels.isNotEmpty() && checkedLabelIds != null && checkedLabelIds.isNotEmpty()) {
                        showLabels.forEach {
                            if (checkedLabelIds.contains(it.id)) {
                                it.selected = true
                            }
                        }
                    }
                    labelLiveData.postValue(showLabels)
                    setInitialized()
                }
            }
        }

        fun resetLabel() {
            labelLiveData.value = labelLiveData.value?.map {
                it.selected = false
                it
            }
        }

        fun getCheckedLabelIds(): List<Long>? = getCheckedLabels()?.map { it.id }

        private fun getCheckedLabels(): List<Label>? {
            return labelLiveData.value?.filter { it.selected }
        }

        fun add(money: String?, desc: String?) {
            if (money == null || money.isEmpty()) {
                app.toast(R.string.main_money_tip)
                return
            }
            runOnWorker {
                val bill = Bill(userId = UserInfo.getUserId(app), money = money.toDouble(), description = desc, date = System.currentTimeMillis())
                val rowId = mBillDao.add(bill)
                val addBill = mBillDao.getByRowId(rowId) ?: return@runOnWorker
                getCheckedLabels()?.apply {
                    addBill.description = summary(money.toDouble(), this, desc)
                    val relations = this.map { LabelRelation(type = 1, relationId = addBill.id, labelId = it.id) }
                    mBillDao.addLabelRelation(relations)
                    mBillDao.update(addBill)
                }
                addLiveData.postValue(true)
                getCount(true)
            }
        }

        fun selectFavorToAdd(position: Int): Boolean {
            val favor = getDataSet().item(position) ?: return false
            mMoney = formatMoney2(favor.money)
            mDesc = favor.description
            getInput()
            runOnWorker {
                val favorLabelIds = mFavorDao.getLabels(favor.id).map { it.id }
                val copyFavorLabelIds = favorLabelIds.toMutableList()
                val selectLabelIds = labelLiveData.value?.mapTo(mutableSetOf()) { it.id } ?: mutableSetOf<Long>()
                copyFavorLabelIds.apply {
                    this.removeAll(selectLabelIds)
                    if (this.isNotEmpty()) {
                        mLabelDao.getByIds(this).map {
                            it.show = 1
                            it
                        }.apply {
                            mLabelDao.updateAll(this)
                        }
                    }
                }
                getLabel(true, favorLabelIds)
            }
            return true
        }

        fun add(favor: Favor?) {
            if (favor == null) {
                app.toast(R.string.main_invalidate_favor)
                return
            }
            runOnWorker {
                val bill = Bill(userId = UserInfo.getUserId(app), money = favor.money, description = favor.description, date = System.currentTimeMillis())
                val rowId = mBillDao.add(bill)
                val addBill = mBillDao.getByRowId(rowId) ?: return@runOnWorker
                mFavorDao.getLabels(favor.id).apply {
                    addBill.description = summary(favor.money, this, favor.description)
                    val relations = this.map { LabelRelation(type = 1, relationId = addBill.id, labelId = it.id) }
                    mBillDao.addLabelRelation(relations)
                    mBillDao.update(addBill)
                }
                getCount(true)
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Favor>? {
            return mFavorDao.getAll(UserInfo.getUserId(app))
        }

        override fun afterLoadInit(ok: Boolean, list: List<Favor>?) {
            super.afterLoadInit(ok, list)
            if (ok && list != null && list.isNotEmpty()) {
                tipLiveData.postValue(true)
            }
        }

        override fun afterLoadRefresh(ok: Boolean, list: List<Favor>?) {
            super.afterLoadRefresh(ok, list)
            if (ok && list != null && list.isNotEmpty()) {
                tipLiveData.postValue(true)
            }
        }

        private fun getTime(addDay: Int, addMonth: Int): Long {
            val calender = Date().clearTime() ?: return 0L
            if (addDay > 0) {
                calender.add(Calendar.DATE, addDay)
            }
            if (addMonth > 0) {
                calender.add(Calendar.MONTH, addMonth)
            }
            return calender.timeInMillis
        }

    }
}