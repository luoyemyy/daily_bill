package com.github.luoyemyy.bill.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.bill.R
import kotlin.math.max

class ChartView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : FrameLayout(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    //    private fun sp2px(sp: Int): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)
    private fun dp2px(dp: Int): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)

    private var mRecyclerView: RecyclerView
    private var mLineXAxis: View
    private var mAdapter: ChartAdapter
    private var mMaxValue = 0.0f
    private var mHeight = dp2px(130)

    //    private var mXAxisTextSize: Float = sp2px(14)
    //    private var mXAxisTextColor: Int = 0x33000000
    //    private var mXAxisHeight: Int = dp2px(20).toInt()

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_chart_view, this)
        mRecyclerView = findViewById(R.id.recyclerView)
        mLineXAxis = findViewById(R.id.lineXAxis)

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mAdapter = ChartAdapter()
        mRecyclerView.adapter = mAdapter
    }

    data class ChartData(var label: String?, var value: Float)

    fun setChartData(list: List<ChartData>?) {
        mAdapter.list = list
        list?.forEach {
            mMaxValue = max(mMaxValue, it.value)
        }
        mAdapter.notifyDataSetChanged()
    }

    inner class ChartAdapter(var list: List<ChartData>? = null) : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(context).inflate(R.layout.layout_chart_view_item, parent, false))
        }

        override fun getItemCount(): Int {
            return list?.size ?: 0
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val data = list?.get(position) ?: return
            holder.setData(data)
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view), OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        private var mTxtValue: TextView = view.findViewById(R.id.txtValue)
        private var mRect: View = view.findViewById(R.id.rect)
        private var mTxtLabel: TextView = view.findViewById(R.id.txtLabel)
        fun setData(chartData: ChartData) {
            mTxtValue.text = chartData.value.toString()
            mTxtLabel.text = chartData.label
            mRect.layoutParams.height = (mHeight / mMaxValue * chartData.value).toInt()
        }

        override fun onClick(v: View?) {

        }
    }
}