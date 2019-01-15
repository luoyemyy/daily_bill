package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LabelDao {

    @Insert
    fun add(label: Label): Long

    @Query("select * from label where rowId = :rowId limit 1")
    fun getByRowId(rowId: Long): Label?

    @Query("select * from label where userId = :userId and show = 1 order by sort desc")
    fun getShow(userId: Long): List<Label>

    @Query("select * from label where userId = :userId order by sort desc")
    fun getAll(userId: Long): List<Label>

}