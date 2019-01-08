package com.github.luoyemyy.bill.util

import android.view.LayoutInflater
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
}