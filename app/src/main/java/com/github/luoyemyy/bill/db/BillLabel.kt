package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey

@Entity
data class BillLabel(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var billId: Long = 0,
    var labelId: Long = 0,
    var state: Int = 0
)

@Dao
interface BillLabelDao {
    @Insert
    fun add(list: List<BillLabel>)
}
