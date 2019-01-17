package com.github.luoyemyy.bill.activity.label

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.base.BaseActivity
import com.github.luoyemyy.bill.activity.base.BaseFragment
import com.github.luoyemyy.bill.databinding.FragmentLabelBinding
import com.github.luoyemyy.bill.databinding.FragmentLabelRecyclerBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bill.util.showAnchor
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.bus.BusMsg
import com.github.luoyemyy.bus.BusResult
import com.github.luoyemyy.config.runOnWorker
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
        mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return mPresenter.move(viewHolder.adapterPosition, target.adapterPosition)
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    mPresenter.saveNewSort()
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        mItemTouchHelper.attachToRecyclerView(mBinding.recyclerView)

        Bus.addCallback(lifecycle, this, BusEvent.ADD_LABEL, BusEvent.EDIT_LABEL)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.loadInit()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.label_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        findNavController().navigate(R.id.action_label_to_labelAddFragment)
        return true
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.ADD_LABEL -> mPresenter.addLabel(msg.longValue)
            BusEvent.EDIT_LABEL -> mPresenter.editLabel(msg.longValue)
        }
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<Label, FragmentLabelRecyclerBinding>(mBinding.recyclerView) {
        override fun bindContentViewHolder(binding: FragmentLabelRecyclerBinding, content: Label, position: Int) {
            binding.apply {
                entity = content
                executePendingBindings()
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentLabelRecyclerBinding>) {
            vh.binding?.imgSort?.setOnTouchListener { _, _ ->
                mItemTouchHelper.startDrag(vh)
                true
            }

            vh.binding?.root?.setOnLongClickListener {
                PopupMenu(requireContext(), it).apply {
                    inflate(R.menu.label_update_delete)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                val label = getItem(vh.adapterPosition) ?: return@setOnMenuItemClickListener false
                                findNavController().navigate(R.id.action_label_to_labelEditFragment, Bundle().apply {
                                    putLong("id", label.id)
                                })
                            }
                            R.id.delete -> mPresenter.delete(vh.adapterPosition)
                        }
                        true
                    }
                    showAnchor(it, (requireActivity() as BaseActivity).touchX, (requireActivity() as BaseActivity).touchY)
                }
                true
            }

            vh.binding?.switchView?.setOnCheckedChangeListener { _, isChecked ->
                mPresenter.showHideLabel(vh.adapterPosition, isChecked)
            }
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): FragmentLabelRecyclerBinding {
            return FragmentLabelRecyclerBinding.inflate(inflater, parent, false)
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Label>(app) {

        private val dao = getLabelDao(app)
        private var mSort = false

        fun move(source: Int, target: Int): Boolean {
            if (source == target) return false
            getAdapterSupport()?.apply {
                getDataSet().move(source, target, getAdapter())
                mSort = true
                return true
            }
            return false
        }

        fun saveNewSort() {
            if (mSort) {
                runOnWorker {
                    dao.updateAll(getDataSet().dataList().mapIndexed { index, label ->
                        label.sort = index + 1
                        label
                    })
                    Bus.post(BusEvent.UPDATE_SHOW_LABEL)
                }
            }
        }

        fun delete(position: Int) {
            val label = getDataSet().item(position) ?: return
            getAdapterSupport()?.apply {
                getDataSet().remove(listOf(label), getAdapter())
                runOnWorker {
                    dao.delete(label)
                    Bus.post(BusEvent.UPDATE_SHOW_LABEL)
                }
            }
        }

        fun addLabel(rowId: Long) {
            single {
                dao.getByRowId(rowId)
            }.result { ok, value ->
                if (ok && value != null) {
                    getAdapterSupport()?.apply {
                        getDataSet().addDataAfter(null, listOf(value), getAdapter())
                        mSort = true
                        saveNewSort()
//                        Bus.post(BusEvent.UPDATE_SHOW_LABEL)
                    }
                }
            }
        }

        fun editLabel(labelId: Long) {
            single {
                dao.get(labelId)
            }.result { ok, value ->
                if (ok && value != null) {
                    val label = getDataSet().dataList().find { it.id == labelId } ?: return@result
                    label.name = value.name
                    getAdapterSupport()?.apply {
                        getDataSet().change(label, getAdapter())
                        Bus.post(BusEvent.UPDATE_SHOW_LABEL)
                    }
                }
            }
        }

        fun showHideLabel(position: Int, show: Boolean) {
            val label = getDataSet().item(position) ?: return
            val s = if (show) 1 else 0
            if (label.show != s) {
                label.show = s
                runOnWorker {
                    dao.updateAll(listOf(label))
                    Bus.post(BusEvent.UPDATE_SHOW_LABEL)
                }
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Label>? {
            return dao.getAll(UserInfo.getUserId(app))
        }
    }
}