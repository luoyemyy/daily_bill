package com.github.luoyemyy.bill.activity.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
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
import com.github.luoyemyy.ext.clearTime
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.ext.show
import com.github.luoyemyy.ext.toast
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*
import com.github.luoyemyy.mvp.runOnWorker
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
            setHasFixedSize(true)
            addItemDecoration(LinearDecoration.middle(requireContext(), 1, true))
        }
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.labelLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutAdd.layoutChips.chips(it)
        })
        mPresenter.countLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutCount.entity = it
        })
        mPresenter.setFlagObserver(this, androidx.lifecycle.Observer {
            if (it == 1) mPresenter.resetAdd() else if (it == 2) {
                if (!UserInfo.getFavorTip(requireContext())) {
                    mBinding.layoutFavorTip.root.show()
                    mBinding.layoutFavorTip.btnOk.setOnClickListener {
                        UserInfo.hideFavorTip(requireContext())
                        mBinding.layoutFavorTip.root.hide()
                    }
                }
            }
        })

        mPresenter.addLiveData.observe(this, androidx.lifecycle.Observer {
            mBinding.layoutAdd.layoutMoney.editText?.setText(it.money)
            mBinding.layoutAdd.layoutDesc.editText?.setText(it.desc)
        })

        //count
        mBinding.layoutCount.viewDay.setOnClickListener {
            findNavController().navigate(R.id.bill)
        }
        mBinding.layoutCount.viewMonth.setOnClickListener {
            findNavController().navigate(R.id.bill)
        }

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
            mPresenter.add(
                mBinding.layoutAdd.layoutMoney.editText?.text?.toString(),
                mBinding.layoutAdd.layoutDesc.editText?.text?.toString()
            )
        }
        mBinding.layoutAdd.btnReset.setOnClickListener {
            mPresenter.resetAdd()
        }

        if (UserInfo.getUserId(requireContext()) == 0L) {
            //还没有用户，进入创建用户页，或者选择用户页
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        } else {
            mPresenter.updateCount()
            mPresenter.updateAdd()
            mPresenter.loadInit(arguments)
        }

        Bus.addCallback(lifecycle, this, BusEvent.UPDATE_SHOW_LABEL, BusEvent.UPDATE_SHOW_FAVOR, BusEvent.CHANGE_USER)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.UPDATE_SHOW_LABEL -> mPresenter.updateAdd(true, mPresenter.getCheckedLabelIds())
            BusEvent.UPDATE_SHOW_FAVOR -> mPresenter.loadRefresh()
            BusEvent.CHANGE_USER -> {
                mPresenter.updateCount(true)
                mPresenter.updateAdd(true, null)
                mPresenter.loadRefresh()
            }
        }
    }

    inner class Adapter : MvpSingleAdapter<Favor, FragmentMainRecyclerFavorBinding>(mBinding.recyclerView) {

        override fun enableLoadMore(): Boolean = false

        override fun enableEmpty(): Boolean = false

        override fun getLayoutId(): Int {
            return R.layout.fragment_main_recycler_favor
        }

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
    }


    class Presenter(var app: Application) : MvpRecyclerPresenter<Favor>(app) {

        private val mBillDao = getBillDao(app)
        private val mFavorDao = getFavorDao(app)
        private val mLabelDao = getLabelDao(app)

        private var mAdd = Add()
        private var mShowLabels: List<Label>? = null

        val labelLiveData = MutableLiveData<List<Label>>()
        val countLiveData = MutableLiveData<Count>()
        val addLiveData = MutableLiveData<Add>()

        override fun delayInitTime(): Long = 0L

        fun updateMoney(money: String?) {
            mAdd.money = money
        }

        fun updateDesc(desc: String?) {
            mAdd.desc = desc
        }

        fun updateCount(reload: Boolean = false) {
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

        fun updateAdd(reload: Boolean = false, checkedLabelIds: List<Long>? = null) {
            if (isInitialized() && !reload) {
                addLiveData.postValue(mAdd)
                labelLiveData.postValue(mShowLabels)
            } else {
                runOnWorker {
                    mAdd.reset()
                    addLiveData.postValue(mAdd)

                    mShowLabels = mLabelDao.getShow(UserInfo.getUserId(app)).apply {
                        if (isNotEmpty() && checkedLabelIds != null && checkedLabelIds.isNotEmpty()) {
                            forEach {
                                if (checkedLabelIds.contains(it.id)) {
                                    it.selected = true
                                }
                            }
                        }
                    }
                    labelLiveData.postValue(mShowLabels)
                    setInitialized()
                }
            }
        }

        fun resetAdd() {
            mAdd.reset()
            addLiveData.postValue(mAdd)

            mShowLabels?.forEach {
                it.selected = false
            }
            labelLiveData.postValue(mShowLabels)
        }

        fun getCheckedLabelIds(): List<Long>? = getCheckedLabels()?.map { it.id }

        private fun getCheckedLabels(): List<Label>? = mShowLabels?.filter { it.selected }


        fun add(money: String?, desc: String?) {
            if (money == null || money.isEmpty()) {
                app.toast(R.string.main_money_tip)
                return
            }
            runOnWorker {
                val bill = Bill(0, UserInfo.getUserId(app), money.toDouble(), desc, null, System.currentTimeMillis())
                val addBill = mBillDao.getByRowId(mBillDao.add(bill)) ?: return@runOnWorker
                getCheckedLabels()?.apply {
                    addBill.description = summary(app, money.toDouble(), this, desc)
                    mBillDao.update(addBill)
                    val relations = this.map { LabelRelation(type = 1, relationId = addBill.id, labelId = it.id) }
                    mBillDao.addLabelRelation(relations)
                }
                flag.postValue(1)
                updateCount(true)
            }
        }

        fun selectFavorToAdd(position: Int): Boolean {
            val favor = getDataSet().item(position) ?: return false
            mAdd.money = formatMoney2(favor.money)
            mAdd.desc = favor.description
            runOnWorker {
                val favorLabelIds = mFavorDao.getLabels(favor.id).map { it.id }
                val copyFavorLabelIds = favorLabelIds.toMutableList()
                val selectLabelIds = labelLiveData.value?.mapTo(mutableSetOf()) { it.id } ?: mutableSetOf<Long>()
                copyFavorLabelIds.apply {
                    this.removeAll(selectLabelIds)
                    if (this.isNotEmpty()) {
                        mLabelDao.updateShowLabel(this)
                    }
                }
                updateAdd(true, favorLabelIds)
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
                val addBill = mBillDao.getByRowId(mBillDao.add(bill)) ?: return@runOnWorker
                mFavorDao.getLabels(favor.id).apply {
                    addBill.description = summary(app, favor.money, this, favor.description)
                    val relations = this.map { LabelRelation(type = 1, relationId = addBill.id, labelId = it.id) }
                    mBillDao.update(addBill)
                    mBillDao.addLabelRelation(relations)
                }
                updateCount(true)
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Favor>? {
            return mFavorDao.getAll(UserInfo.getUserId(app))
        }

        override fun afterLoadInit(ok: Boolean, list: List<Favor>?) {
            super.afterLoadInit(ok, list)
            if (ok && list != null && list.isNotEmpty()) {
                flag.postValue(2)
            }
        }

        override fun afterLoadRefresh(ok: Boolean, list: List<Favor>?) {
            super.afterLoadRefresh(ok, list)
            if (ok && list != null && list.isNotEmpty()) {
                flag.postValue(2)
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