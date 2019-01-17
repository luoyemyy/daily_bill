package com.github.luoyemyy.bill.activity.favor

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
import com.github.luoyemyy.bill.databinding.FragmentFavorBinding
import com.github.luoyemyy.bill.databinding.FragmentFavorRecyclerBinding
import com.github.luoyemyy.bill.db.Favor
import com.github.luoyemyy.bill.db.getFavorDao
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

class FavorFragment : BaseFragment(), BusResult {

    private lateinit var mBinding: FragmentFavorBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mItemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentFavorBinding.inflate(inflater, container, false).apply { mBinding = this }.root
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

        Bus.addCallback(lifecycle, this, BusEvent.ADD_FAVOR, BusEvent.EDIT_FAVOR)
    }

    override fun busResult(event: String, msg: BusMsg) {
        when (event) {
            BusEvent.ADD_FAVOR -> mPresenter.addFavor(msg.longValue)
            BusEvent.EDIT_LABEL -> mPresenter.editFavor(msg.longValue)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.loadInit()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.favor_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        findNavController().navigate(R.id.action_favor_to_favorAddFragment)
        return true
    }

    inner class Adapter : AbstractSingleRecyclerAdapter<Favor, FragmentFavorRecyclerBinding>(mBinding.recyclerView) {
        override fun bindContentViewHolder(binding: FragmentFavorRecyclerBinding, content: Favor, position: Int) {
            binding.entity = content
            binding.executePendingBindings()
        }

        override fun createContentView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): FragmentFavorRecyclerBinding {
            return FragmentFavorRecyclerBinding.inflate(inflater, parent, false)
        }

        override fun enableLoadMore(): Boolean {
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun bindItemEvents(vh: VH<FragmentFavorRecyclerBinding>) {
            vh.binding?.imgSort?.setOnTouchListener { _, _ ->
                mItemTouchHelper.startDrag(vh)
                true
            }

            vh.binding?.root?.setOnLongClickListener {
                PopupMenu(requireContext(), it).apply {
                    inflate(R.menu.favor_update_delete)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.edit -> {
                                val favor = getItem(vh.adapterPosition) ?: return@setOnMenuItemClickListener false
                                findNavController().navigate(R.id.action_favor_to_favorEditFragment, Bundle().apply {
                                    putLong("id", favor.id)
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
        }
    }

    class Presenter(var app: Application) : AbstractRecyclerPresenter<Favor>(app) {
        private val mFavorDao = getFavorDao(app)
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
                    mFavorDao.update(getDataSet().dataList().mapIndexed { index, label ->
                        label.sort = index + 1
                        label
                    })
                    Bus.post(BusEvent.UPDATE_SHOW_FAVOR)
                }
            }
        }

        fun addFavor(id: Long) {
            single {
                mFavorDao.get(id)
            }.result { ok, value ->
                if (ok && value != null) {
                    getAdapterSupport()?.apply {
                        getDataSet().addDataAfter(null, listOf(value), getAdapter())
                        mSort = true
                        saveNewSort()
//                        Bus.post(BusEvent.UPDATE_SHOW_FAVOR)
                    }
                }
            }

        }

        fun editFavor(id: Long) {
            single {
                mFavorDao.get(id)
            }.result { ok, value ->
                if (ok && value != null) {
                    val favor = getDataSet().dataList().find { it.id == id } ?: return@result
                    getAdapterSupport()?.apply {
                        getDataSet().change(favor, getAdapter())
                        Bus.post(BusEvent.UPDATE_SHOW_FAVOR)
                    }
                }
            }
        }

        fun delete(position: Int) {
            val label = getDataSet().item(position) ?: return
            getAdapterSupport()?.apply {
                getDataSet().remove(listOf(label), getAdapter())
                runOnWorker {
                    mFavorDao.delete(label)
                    Bus.post(BusEvent.UPDATE_SHOW_FAVOR)
                }
            }
        }

        override fun loadData(loadType: LoadType, paging: Paging, bundle: Bundle?, search: String?): List<Favor>? {
            return mFavorDao.getAll(UserInfo.getUserId(app))
        }
    }
}