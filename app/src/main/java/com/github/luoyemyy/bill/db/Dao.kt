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

    @Query("select * from user where id = :id")
    fun getUser(id: Long): User?

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

    @Query("select * from bill where userId = :userId and  date >= :start and date < :end")
    fun getBillByDate(userId: Long, start: Long, end: Long): List<Bill>

    @Query("select sum(money) from bill where userId = :userId and date >= :start and date < :end")
    fun sumMoneyByDate(userId: Long, start: Long, end: Long): Double

    /********************************************************
     ******************** label *****************************
     ********************************************************
     */
    @Query("select * from label where userId = :userId and show = 1 order by sort asc")
    fun getShowLabel(userId: Long): List<Label>

    /********************************************************
     ******************** favor *****************************
     ********************************************************
     */
    @Query("select * from favor where userId = :userId order by sort asc")
    fun getFavor(userId: Long): List<Favor>
}
