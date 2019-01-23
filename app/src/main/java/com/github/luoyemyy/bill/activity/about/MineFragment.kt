package com.github.luoyemyy.bill.activity.about

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bill.databinding.FragmentAboutBinding
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class MineFragment : Fragment() {

    private lateinit var mBinding: FragmentAboutBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstance() = MineFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAboutBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(RecyclerDecoration.middle(requireContext(), 1, true))
        }
    }

    inner class Adapter : AbstractMultiRecyclerAdapter(mBinding.recyclerView) {
        override fun getContentType(position: Int, item: Any?): Int {
            return 0
        }

        override fun getLayoutId(viewType: Int): Int {
            return 0
        }

        override fun bindContentViewHolder(binding: ViewDataBinding, content: Any, position: Int) {
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Any>(app) {
        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Any>? {
            return null
        }
    }
}
