package com.github.luoyemyy.bill.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import kotlin.math.roundToInt

class ChartBarView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    private val mLabelPaint = Paint().apply {
        color = 0xffcccccc.toInt()
        textSize = 12f
    }
    private val mXAxisLabelPaint = Paint().apply {
        color = 0xff00ff00.toInt()
        textSize = 15f
    }
    private val mBarPaint = Paint().apply {
        color = 0xff000000.toInt()
    }

    private fun toPx(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp).roundToInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specSize = View.MeasureSpec.getSize(widthMeasureSpec) / 7
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.EXACTLY), heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        val barWidth = min(width / 5, toPx(10))

    }
}