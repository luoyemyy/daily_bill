package com.github.luoyemyy.bill.util

import android.view.LayoutInflater
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.ext.toObject
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter("chips")
    fun chips(chipGroup: ChipGroup, chips: String?) {
        val array = chips?.toObject<List<String>>() ?: return
        val inflater = LayoutInflater.from(chipGroup.context)
        (0 until array.size).forEach {
            val chip = inflater.inflate(R.layout.layout_chip_label, chipGroup, false) as Chip
            chip.text = array[it]
            chipGroup.addView(chip)
        }
    }

    @JvmStatic
    @BindingAdapter("user_selected")
    fun userSelected(textView: TextView, isDefault: Int) {
        if (isDefault == 1) {
            val right = textView.context.getDrawable(R.drawable.ic_ok)
            right?.setBounds(0, 0, right.intrinsicWidth, right.intrinsicHeight)
            textView.setCompoundDrawables(null, null, right, null)
        }
    }
}