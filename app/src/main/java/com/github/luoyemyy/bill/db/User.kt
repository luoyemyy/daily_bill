package com.github.luoyemyy.bill.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var nickname: String? = null,
        var isDefault: Int = 0
)
