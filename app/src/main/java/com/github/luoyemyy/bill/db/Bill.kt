package com.github.luoyemyy.bill.db

import androidx.room.*

@Entity
data class Bill(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var money: Double = 0.0,
        var description: String? = null,
        var detail: String? = null,
        var date: Long = 0
)


@Dao
interface BillDao {

    @Insert
    fun add(bill: Bill)

    @Update
    fun edit(bill: Bill)

    @Delete
    fun delete(bill: Bill)

    @Query("select * from bill order by id desc limit 1")
    fun getLastAddBill(): Bill

    @Query("select * from bill where date >= :start and date < :end")
    fun getBillByDate(start: Long, end: Long): List<Bill>

    @Query("select b.* from billLabel bl left join bill b on bl.billId = b.id and bl.state = 1 " +
            " where bl.labelId = :labelId" +
            " order by b.date desc ")
    fun getBillByLabel(labelId: Long): List<Bill>

    @Query("select sum(money) from bill where date >= :start and date < :end")
    fun sumMoneyByDate(start: Long, end: Long): Double
}


