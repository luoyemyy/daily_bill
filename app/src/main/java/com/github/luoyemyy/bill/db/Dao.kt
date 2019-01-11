package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface Dao {

    /********************************************************
     ******************** user ******************************
     ********************************************************
     */
    @Insert
    fun addUser(user: User): Long

    @Update
    fun updateUser(user: User): Int

    @Query("select * from user")
    fun getAllUser(): List<User>

    @Query("select * from user where isDefault = 1 limit 1")
    fun getDefaultUser(): User?

    /********************************************************
     ******************** bill ******************************
     ********************************************************
     */
    @Insert
    fun addBill(bill: Bill): Long

    @Insert
    fun addLabelRelation(labelRelation: List<LabelRelation>)

    @Query("select id from bill where rowId = :rowId limit 1")
    fun getBillIdByRowId(rowId: Long): Long

    @Query("select * from bill where date >= :start and date < :end")
    fun getBillByDate(start: Long, end: Long): List<Bill>

    @Query("select sum(money) from bill where date >= :start and date < :end")
    fun sumMoneyByDate(start: Long, end: Long): Double

}
