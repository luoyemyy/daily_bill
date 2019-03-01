package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentUserChangeBinding
import com.github.luoyemyy.bill.databinding.FragmentUserChangeRecyclerBinding
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.bill.db.getUserDao
import com.github.luoyemyy.bill.util.*
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.LoadType
import com.github.luoyemyy.mvp.recycler.Paging
import com.github.luoyemyy.mvp.recycler.VH
import com.github.luoyemyy.mvp.runOnWorker

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
        mPresenter = getRecyclerPresenter(this, Adapter())
        mPresenter.setFlagObserver(this, Observer {
            findNavController().navigateUp()
        })
        mBinding.apply {
            recyclerView.setup(hasFixed = true)
            swipeRefreshLayout.setup(mPresenter)
        }
        mPresenter.loadInit()
    }

    inner class Adapter : MvpSingleAdapter<User, FragmentUserChangeRecyclerBinding>(mBinding.recyclerView) {

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun getLayoutId(): Int {
            return R.layout.fragment_user_change_recycler
        }

        override fun bindContentViewHolder(binding: FragmentUserChangeRecyclerBinding, content: User, position: Int) {
            binding.entity = content
            binding.executePendingBindings()
        }

        override fun setRefreshState(refreshing: Boolean) {
            mBinding.swipeRefreshLayout.isRefreshing = refreshing
        }

        override fun onItemClickListener(vh: VH<FragmentUserChangeRecyclerBinding>, view: View?) {
            mPresenter.selectDefault(getItem(vh.adapterPosition))
        }

        override fun bindItemEvents(vh: VH<FragmentUserChangeRecyclerBinding>) {
            vh.binding?.root?.apply {
                setOnLongClickListener { itemMenu(it, getItem(vh.adapterPosition)) }
            }
        }

        private fun itemMenu(view: View, user: User?): Boolean {
            if (user == null) return false
            PopupMenu(requireContext(), view, Gravity.CENTER).apply {
                inflate(R.menu.edit_delete)
                setOnMenuItemClickListener {
                    if (it.itemId == R.id.edit) {
                        findNavController().navigate(R.id.userEdit, bundleOf(Pair("id", user.id)))
                    } else if (it.itemId == R.id.delete) {
                        if (mPresenter.isLoginUser(user)) {
                            refusedDelete()
                        } else {
                            confirmDelete(user)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
            }.showAnchor(view, getTouchX(), getTouchY())
            return true
        }

        private fun confirmDelete(user: User) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.user_delete_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sure) { _, _ ->
                    mPresenter.deleteUser(user)
                }.show()
        }

        private fun refusedDelete() {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.user_delete_refused)
                .setPositiveButton(R.string.know, null).show()
        }
    }

    class Presenter(var app: Application) : MvpRecyclerPresenter<User>(app) {

        private val dao = getUserDao(app)

        fun selectDefault(user: User?) {
            if (user == null || user.id == UserInfo.getUserId(app)) return
            runOnWorker {
                UserInfo.setDefaultUser(app, user.id)
                flag.postValue(1)
                Bus.post(BusEvent.CHANGE_USER)
            }
        }

        fun isLoginUser(user: User): Boolean = user.id == UserInfo.getUserId(app)


        fun deleteUser(user: User) {
            if (user.id == UserInfo.getUserId(app)) {
                return
            }
            getAdapterSupport()?.apply {
                getDataSet().remove(listOf(user), getAdapter())
            }
            runOnWorker {
                dao.delete(user)
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<User>? {
            return dao.getAll()
        }
    }
}