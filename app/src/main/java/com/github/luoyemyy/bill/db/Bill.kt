package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey

@Entity
data class Bill(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                var money: Double = 0.0,
                var description: String? = null,
                var date: Long = 0
)

@Dao
interface BillDao{
    @Insert
    fun add(bill: Bill)



}

