package com.github.luoyemyy.bill.activity.main

import com.github.luoyemyy.bill.db.Label

open class MainData(var type: Int)

data class Count(var countToday: String? = null, var countMonth: String? = null) : MainData(1)

data class Add(var labels: List<Label>? = null, var chips: String? = null) : MainData(2)

data class FavorHeader(var tip: String? = null) : MainData(3)

data class Favor(var favorId: Long = 0, var detail: String? = null) : MainData(4)