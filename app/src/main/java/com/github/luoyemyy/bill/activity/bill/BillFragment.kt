package com.github.luoyemyy.bill.activity.bill

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentBillBinding
import com.github.luoyemyy.bill.databinding.FragmentBillRecyclerBinding
import com.github.luoyemyy.bill.db.Bill
import com.github.luoyemyy.bill.util.MvpRecyclerPresenter
import com.github.luoyemyy.bill.util.MvpSingleAdapter
import com.github.luoyemyy.bill.view.ChartView
import com.github.luoyemyy.mvp.getRecyclerPresenter

class BillFragment : BaseFragment() {

    companion object {
        const val TYPE_DAY = 1
        const val TYPE_WEEK = 2
        const val TYPE_MONTH = 3
        const val TYPE_YEAR = 4

        val day: Bundle = bundleOf(Pair("type", TYPE_DAY))
        val week: Bundle = bundleOf(Pair("type", TYPE_WEEK))
        val month: Bundle = bundleOf(Pair("type", TYPE_MONTH))
        val year: Bundle = bundleOf(Pair("type", TYPE_YEAR))
    }

    private lateinit var mBinding: FragmentBillBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.bill, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        findNavController().navigate(R.id.action_label_to_labelAddFragment)
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBillBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mBinding.chartView.setChartData(listOf(
            ChartView.ChartData("12-11", 12.44f),
            ChartView.ChartData("12-12", 112.44f),
            ChartView.ChartData("12-13", 102.44f),
            ChartView.ChartData("12-14", 2.44f),
            ChartView.ChartData("12-15", 22.44f),
            ChartView.ChartData("12-16", 32.44f),
            ChartView.ChartData("12-17", 42.44f),
            ChartView.ChartData("12-13", 22.44f),
            ChartView.ChartData("12-14", 52.44f),
            ChartView.ChartData("12-15", 72.44f),
            ChartView.ChartData("12-16", 82.44f),
            ChartView.ChartData("12-17", 122.44f),
            ChartView.ChartData("12-13", 12.44f),
            ChartView.ChartData("12-14", 122.44f),
            ChartView.ChartData("12-15", 82.44f),
            ChartView.ChartData("12-16", 92.44f),
            ChartView.ChartData("12-17", 21.44f),
            ChartView.ChartData("12-18", 98.44f)
        ))
    }


    inner class Adapter() : MvpSingleAdapter<Bill, FragmentBillRecyclerBinding>(mBinding.recyclerView) {

        override fun getLayoutId(): Int = R.layout.fragment_bill_recycler

        override fun bindContentViewHolder(binding: FragmentBillRecyclerBinding, content: Bill, position: Int) {}

        private var type: Int = TYPE_DAY

        fun initType(bundle: Bundle?) = type == bundle?.getInt("type") ?: TYPE_DAY

    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<Bill>(app) {

    }

}