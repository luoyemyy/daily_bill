package com.github.luoyemyy.bill.util

import android.app.Application
import android.os.Handler
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.recycler.AbstractMultiRecyclerAdapter
import com.github.luoyemyy.mvp.recycler.AbstractRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.AbstractSingleRecyclerAdapter

abstract class MvpSimplePresenter<T>(app: Application) : AbstractPresenter<T>(app)

abstract class MvpRecyclerPresenter<T>(app: Application) : AbstractRecyclerPresenter<T>(app)

abstract class MvpSingleAdapter<T, BIND : ViewDataBinding>(recyclerView: RecyclerView) : AbstractSingleRecyclerAdapter<T, BIND>(recyclerView)

abstract class MvpMultiAdapter(recyclerView: RecyclerView) : AbstractMultiRecyclerAdapter(recyclerView)