package com.github.luoyemyy.bill.activity.user

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.bill.activity.main.MainActivity
import com.github.luoyemyy.bill.databinding.FragmentUserAddBinding
import com.github.luoyemyy.bill.db.Label
import com.github.luoyemyy.bill.db.getLabelDao
import com.github.luoyemyy.bill.util.BusEvent
import com.github.luoyemyy.bill.util.TextChangeAdapter
import com.github.luoyemyy.bill.util.UserInfo
import com.github.luoyemyy.bill.util.setKeyAction
import com.github.luoyemyy.bus.Bus
import com.github.luoyemyy.config.runOnWorker
import com.github.luoyemyy.ext.hide
import com.github.luoyemyy.mvp.AbstractPresenter
import com.github.luoyemyy.mvp.getPresenter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class UserAddFragment : Fragment() {

    private lateinit var mBinding: FragmentUserAddBinding
    private lateinit var mPresenter: Presenter

    companion object {
        fun newInstanceFromLogin() = UserAddFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isLogin", true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentUserAddBinding.inflate(inflater, container, false).apply { mBinding = this }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = getPresenter()
        mPresenter.data.observe(this, Observer {
            if (mPresenter.isLogin(arguments)) {
                startActivity(Intent(context, MainActivity::class.java))
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        })

        mBinding.apply {
            layoutName.editText?.apply {
                setKeyAction(requireActivity())
                addTextChangedListener(object : TextChangeAdapter() {
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        btnAdd.isEnabled = count > 0
                    }
                })
            }
            btnAdd.setOnClickListener {
                val name = layoutName.editText?.text?.toString() ?: return@setOnClickListener
                mPresenter.add(name)
            }
        }

        if (mPresenter.isLogin(arguments)) {
            mBinding.switchView.hide()
            chips(mBinding.layoutChips, mPresenter.suggestLabels())
        }
    }

    private fun chips(chipGroup: ChipGroup, chips: List<String>?) {
        if (chips == null || chips.isEmpty()) return
        val inflater = LayoutInflater.from(chipGroup.context)
        (0 until chips.size).forEach {
            val chip = inflater.inflate(R.layout.layout_chip_label, chipGroup, false) as Chip
            chip.text = chips[it]
            chipGroup.addView(chip)
            chip.setOnCheckedChangeListener { c, isChecked ->
                mPresenter.checkToggle(mBinding.layoutChips.indexOfChild(c), isChecked)
            }
        }
    }

    class Presenter(var app: Application) : AbstractPresenter<Boolean>(app) {

        private var mSuggestLabels: List<String>? = null
        private var mIsLogin = false
        private var mCheckLabelSet = mutableSetOf<Int>()

        fun isLogin(bundle: Bundle?): Boolean {
            mIsLogin = bundle?.getBoolean("isLogin") == true
            return mIsLogin
        }

        fun checkToggle(index: Int, isChecked: Boolean) {
            if (isChecked) {
                mCheckLabelSet.add(index)
            } else {
                mCheckLabelSet.remove(index)
            }
        }

        fun suggestLabels(): List<String>? {
            return app.resources.getStringArray(R.array.suggest_label).toList().apply {
                mSuggestLabels = this
            }
        }

        fun add(name: String) {
            runOnWorker {
                UserInfo.setDefaultUser(app, name) {
                    if (mIsLogin) {
                        val userId = UserInfo.getUserId(app)
                        val labels = mSuggestLabels?.filterIndexed { index, _ -> mCheckLabelSet.contains(index) }?.mapIndexed { index, s -> Label(0, userId, s, 1, index + 1) }
                        if (labels != null && labels.isNotEmpty()) {
                            getLabelDao(app).addAll(labels)
                        }
                    }
                    Bus.post(BusEvent.UPDATE_USER)
                    data.postValue(true)
                }
            }
        }
    }
}