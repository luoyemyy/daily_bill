package com.github.luoyemyy.bill.activity.main

data class Count(var countToday: String? = null, var countMonth: String? = null)

data class Add(var money: String? = null, var desc: String? = null) {
    fun reset() {
        money = null
        desc = null
    }
}