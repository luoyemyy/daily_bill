package com.github.luoyemyy.bill.activity.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.activity.login.LoginActivity
import com.github.luoyemyy.bill.databinding.*
import com.github.luoyemyy.bill.db.getBillDao
import com.github.luoyemyy.bill.db.getFavorDao
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.ext.clearTime
import com.github.luoyemyy.ext.dp2px
import com.github.luoyemyy.ext.toJsonString
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*
import java.util.*

class MainFragment : BaseFragment() {

    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mPresenter: Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(Decoration(requireContext()))
        }
        mPresenter = getRecyclerPresenter(this, Adapter())

        if (UserInfo.getUserId(requireContext()) == 0L) {
            //还没有用户，进入创建用户页，或者选择用户页
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        } else {
            mPresenter.loadInit(arguments)
        }
    }

    inner class Adapter : AbstractMultiRecyclerAdapter(mBinding.recyclerView) {

        override fun enableLoadMore(): Boolean = false

        override fun bindContentViewHolder(binding: ViewDataBinding, content: Any, position: Int) {
            when {
                content is Count && binding is FragmentMainRecyclerCountBinding -> {
                    binding.entity = content
                    binding.executePendingBindings()
                }
                content is Add && binding is FragmentMainRecyclerAddBinding -> {
                    binding.entity = content
                    binding.executePendingBindings()
                }
                content is FavorHeader && binding is FragmentMainRecyclerFavorHeaderBinding -> {
                    binding.text = content.tip
                    binding.executePendingBindings()
                }
                content is Favor && binding is FragmentMainRecyclerFavorBinding -> {
                    binding.text = content.detail
                    binding.executePendingBindings()
                }
            }
        }

        override fun bindContentViewHolder(
            binding: ViewDataBinding,
            content: Any,
            position: Int,
            payloads: MutableList<Any>
        ): Boolean {
            val viewType = getItemViewType(position)
            return when {
                viewType == 1 && content is Count && binding is FragmentMainRecyclerCountBinding -> {
                    binding.entity = content
                    binding.executePendingBindings()
                    true
                }
                viewType == 1 && content is Add && binding is FragmentMainRecyclerAddBinding -> {
                    binding.entity = content
                    binding.executePendingBindings()
                    true
                }
                else -> false
            }
        }

        override fun getItemClickViews(binding: ViewDataBinding): Array<View> {
            return when (binding) {
                is FragmentMainRecyclerCountBinding -> {
                    arrayOf(binding.txtToday, binding.txtMonth)
                }
                is FragmentMainRecyclerAddBinding -> {
                    arrayOf(binding.btnReset, binding.btnAdd)
                }
                is FragmentMainRecyclerFavorHeaderBinding -> {
                    arrayOf(binding.btnOk)
                }
                is FragmentMainRecyclerFavorBinding -> {
                    arrayOf(binding.root)
                }
                else -> arrayOf()
            }
        }

        override fun onItemClickListener(vh: VH<ViewDataBinding>, view: View?) {
            val binding = vh.binding ?: return
            when (binding) {
                is FragmentMainRecyclerCountBinding -> {
                    if (view == binding.txtToday) {
                        view.findNavController().navigate(R.id.bill)
                    } else if (view == binding.txtMonth) {
                        view.findNavController().navigate(R.id.bill)
                    }
                }
                is FragmentMainRecyclerAddBinding -> {
                    if (view == binding.btnReset) {
                        //todo
                    } else if (view == binding.btnAdd) {
                        //todo
                    }
                }
                is FragmentMainRecyclerFavorHeaderBinding -> {
                    val header = getItem(2) ?: return
                    mPresenter.getDataSet().remove(listOf(header), this)
                }
                is FragmentMainRecyclerFavorBinding -> {
                    //todo
                }
            }
        }

        override fun bindItemEvents(vh: VH<ViewDataBinding>) {
            val binding = vh.binding as? FragmentMainRecyclerFavorBinding ?: return
            binding.root.setOnLongClickListener {
                //todo
                return@setOnLongClickListener true
            }
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding? {
            return when (viewType) {
                1 -> FragmentMainRecyclerCountBinding.inflate(inflater, parent, false)
                2 -> FragmentMainRecyclerAddBinding.inflate(inflater, parent, false)
                3 -> FragmentMainRecyclerFavorHeaderBinding.inflate(inflater, parent, false)
                4 -> FragmentMainRecyclerFavorBinding.inflate(inflater, parent, false)
                else -> null
            }
        }

        override fun getContentType(position: Int, item: Any?): Int {
            return (item as? MainData)?.type ?: 0
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Any>(app) {

        private val billDao = getBillDao(app)
        private val favorDao = getFavorDao(app)
        private val labelDao = getLabelDao(app)

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Any>? {

            val userId = UserInfo.getUserId(app)
            val startTime = getTime(0, 0)
            val endTimeToday = getTime(1, 0)
            val endTImeMonth = getTime(0, 1)
            val countToday = billDao.sumMoneyByDate(userId, startTime, endTimeToday).toString()
            val countMonth = billDao.sumMoneyByDate(userId, startTime, endTImeMonth).toString()
            val count = Count(countToday, countMonth)
            val add = labelDao.getShow(userId).let { Add(it, it.toJsonString()) }
            val favors = favorDao.getAll(userId).map { Favor(it.id, it.summary) }

            return mutableListOf<Any>().apply {
                add(count)
                add(add)
                add(FavorHeader(app.getString(R.string.main_shortcut_tip)))
                if (favors.isNotEmpty()) {
                    addAll(favors)
                }
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

    inner class Decoration(context: Context) : RecyclerView.ItemDecoration() {
        private val space = context.dp2px(8)
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            when (parent.getChildAdapterPosition(view)) {
                0 -> outRect.set(0, space, 0, 0)
                1 -> outRect.set(0, space, 0, space)
                else -> outRect.bottom = 1
            }
        }
    }
}