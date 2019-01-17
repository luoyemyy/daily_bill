package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BillDao {

    @Insert
    fun add(bill: Bill): Long

    @Update
    fun update(bill: Bill)

    @Insert
    fun addLabelRelation(labelRelation: List<LabelRelation>)

    @Query("select * from bill where rowId = :rowId limit 1")
    fun getByRowId(rowId: Long): Bill?

    @Query("select * from bill where userId = :userId and  date >= :start and date < :end")
    fun getByDate(userId: Long, start: Long, end: Long): List<Bill>

    @Query("select sum(money) from bill where userId = :userId and date >= :start and date < :end")
    fun sumMoneyByDate(userId: Long, start: Long, end: Long): Double
}