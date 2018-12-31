package com.github.luoyemyy.bill.activity.main

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.luoyemyy.bill.R
import com.github.luoyemyy.mvp.AbstractPresenter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    class Presenter(app: Application) : AbstractPresenter<Any>(app) {


    }
}
