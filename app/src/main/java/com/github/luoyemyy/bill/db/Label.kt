package com.github.luoyemyy.bill.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var userId: Long = 0,
        var name: String? = null,
        var sort: Int = 0
)



