package com.github.luoyemyy.bill.activity.about

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bill.databinding.FragmentAboutBinding
import com.github.luoyemyy.bill.databinding.FragmentAboutRecyclerBinding
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class AboutFragment : Fragment() {

    private lateinit var mBinding: FragmentAboutBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstance() = AboutFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentAboutBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(LinearDecoration.middle(requireContext(), 1, true))
        }
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<String, FragmentAboutRecyclerBinding>(mBinding.recyclerView) {
        override fun bindContentViewHolder(binding: FragmentAboutRecyclerBinding, content: String, position: Int) {
            binding.entity = content
            binding.executePendingBindings()
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): FragmentAboutRecyclerBinding? {
            return FragmentAboutRecyclerBinding.inflate(inflater, parent, false)
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<String>(app) {
        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<String>? {
            return null
        }
    }
}
