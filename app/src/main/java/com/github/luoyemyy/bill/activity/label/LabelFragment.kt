package com.github.luoyemyy.bill.activity.label

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentLabelBinding
import com.github.luoyemyy.bill.databinding.FragmentLabelRecyclerBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult
import com.github.luoyemyy.mvp.getRecyclerPresenter
import com.github.luoyemyy.mvp.recycler.*
import com.github.luoyemyy.mvp.result
import com.github.luoyemyy.mvp.single

class LabelFragment : BaseFragment(), BusResult {
    private lateinit var mBinding: FragmentLabelBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mItemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentLabelBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getRecyclerPresenter(this, Adapter())
        mBinding.recyclerView.apply {
            setLinearManager()
            addItemDecoration(RecyclerDecoration.middle(requireContext(), spaceUnit = true))
        }
        mItemTouchHelper =
                ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return mPresenter.move(viewHolder.adapterPosition, target.adapterPosition)
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

                })
        mItemTouchHelper.attachToRecyclerView(mBinding.recyclerView)

        Bus.addCallback(lifecycle, this, BusEvent.ADD_LABBEL)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.loadInit()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.label, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        findNavController().navigate(R.id.action_label_to_labelAddFragment)
        return true
    }

    override fun busResult(event: String, msg: BusMsg) {
        mPresenter.addLabel(msg.longValue)
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<Label, FragmentLabelRecyclerBinding>(mBinding.recyclerView) {
        override fun bindContentViewHolder(binding: FragmentLabelRecyclerBinding, content: Label, position: Int) {
            binding.apply {
                entity = content
                executePendingBindings()
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun createContentView(
            inflater: LayoutInflater,
            parent: ViewGroup,
            viewType: Int
        ): FragmentLabelRecyclerBinding {
            return FragmentLabelRecyclerBinding.inflate(inflater, parent, false)
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Label>(app) {

        private val dao = getLabelDao(app)

        fun move(source: Int, target: Int): Boolean {
            val s = getDataSet().item(source) ?: return false
            val t = getDataSet().item(target) ?: return false
            getAdapterSupport()?.apply {
                (getDataSet().dataList() as MutableList).remove(s)
                getDataSet().addDataAfter(t, listOf(s), getAdapter())
                return true
            }
            return false
        }

        fun addLabel(rowId: Long) {
            single {
                dao.getByRowId(rowId)
            }.result { ok, value ->
                if (ok && value != null) {
                    getAdapterSupport()?.apply {
                        getDataSet().addData(listOf(value), getAdapter())
                    }
                }
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Label>? {
            return dao.getAll(UserInfo.getUserId(app))
        }
    }
}