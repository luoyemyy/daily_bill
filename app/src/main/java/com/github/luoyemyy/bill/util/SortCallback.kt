package com.github.luoyemyy.bill.util

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SortCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,0) {

    private var mSwipeWidth = 0f
    private var s = 0f

    abstract fun move(source: Int, target: Int): Boolean

    abstract fun moveEnd()

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return move(viewHolder.adapterPosition, target.adapterPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            moveEnd()
        }
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 1f
    }

//    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
//        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
//            val content = viewHolder.itemView.findViewById<View>(R.id.layoutContent) ?: return
//            val menu = viewHolder.itemView.findViewById<View>(R.id.layoutMenu) ?: return
//            val maxSwipe = menu.width
//            if (isCurrentlyActive) {
//                mSwipeWidth = abs(dX)
//                if (abs(dX) < maxSwipe) {
//                    content.translationX = dX
//                    menu.translationX = dX
//                    s = (maxSwipe - mSwipeWidth) / mSwipeWidth
//                } else {
//                    mSwipeWidth = maxSwipe.toFloat()
//                    s = 0f
//                }
//            } else {
//                if (mSwipeWidth > maxSwipe / 2) {
//                    val x = -maxSwipe - s * dX
//                    content.translationX = x
//                    menu.translationX = x
//                } else {
//                    content.translationX = dX
//                    menu.translationX = dX
//                }
//            }
//        } else {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        }
//    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}