package com.github.luoyemyy.bill.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LabelRelation(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var type: Int = 0,              // 1 账单和标签的关系 2 收藏和标签的关系
        var relationId: Long = 0,       // 账单id 或 收藏id
        var labelId: Long = 0
)