package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentUserChangeBinding
import com.github.luoyemyy.bill.databinding.FragmentUserChangeRecyclerBinding
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.bill.db.getDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*

class UserChangeFragment : BaseFragment() {

    private lateinit var mBinding: FragmentUserChangeBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstance() = UserChangeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserChangeBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.recyclerView.setHasFixedSize(true)
        mBinding.recyclerView.setLinearManager()
        mBinding.recyclerView.addItemDecoration(RecyclerDecoration.middle(requireContext(), spaceUnit = true))
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            mPresenter.loadRefresh()
        }
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.changeUserLiveData.observe(this, Observer {
            findNavController().navigateUp()
        })
        mPresenter.loadInit()
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<User, FragmentUserChangeRecyclerBinding>(mBinding.recyclerView) {

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun bindContentViewHolder(binding: FragmentUserChangeRecyclerBinding, content: User, position: Int) {
            binding.entity = content
            binding.executePendingBindings()
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): FragmentUserChangeRecyclerBinding {
            return FragmentUserChangeRecyclerBinding.inflate(inflater, parent, false)
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemClickListener(vh: VH<FragmentUserChangeRecyclerBinding>, view: View?) {
            mPresenter.selectDefault(getItem(vh.adapterPosition))
        }

        override fun bindItemEvents(vh: VH<FragmentUserChangeRecyclerBinding>) {
            vh.binding?.root?.apply {
                setOnLongClickListener {
                    PopupMenu(requireContext(), it, Gravity.CENTER).apply {
                        inflate(R.menu.user)
                        setOnMenuItemClickListener {

                            return@setOnMenuItemClickListener true
                        }
                    }.show()
                    return@setOnLongClickListener true
                }
            }
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<User>(app) {

        private val dao = getDao(app)
        val changeUserLiveData = MutableLiveData<Boolean>()

        fun selectDefault(user: User?) {
            if (user == null) return
            runOnWorker {
                UserInfo.setDefaultUser(app, user.id) {
                    Bus.post(BusEvent.UPDATE_USER)
                    changeUserLiveData.postValue(true)
                }
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<User>? {
            return dao.getAllUser()
        }
    }
}