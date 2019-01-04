package com.github.luoyemyy.bill.activity.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.luoyemyy.bill.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).root
    }
}