package com.github.luoyemyy.bill.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bill(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var userId: Long = 0,
        var money: Double = 0.0,
        var description: String? = null,
        var summary: String? = null,
        var date: Long = 0
)

