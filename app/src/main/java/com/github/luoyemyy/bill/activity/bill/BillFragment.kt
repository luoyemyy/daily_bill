package com.github.luoyemyy.bill.activity.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentBillBinding
import com.github.luoyemyy.bill.view.ChartView

class BillFragment : BaseFragment() {

    private lateinit var mBinding: FragmentBillBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBillBinding.inflate(inflater, container, false).apply { mBinding = this }.root
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

}