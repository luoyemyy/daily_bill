package com.github.luoyemyy.bill.db

import androidx.room.*

@Dao
interface LabelDao {

    @Insert
    fun add(label: Label): Long

    @Insert
    fun addAll(labels: List<Label>)

    @Update
    fun updateAll(labels: List<Label>)

    @Delete
    fun delete(label: Label)

    @Query("select * from label where id = :id")
    fun get(id: Long): Label?

    @Query("select * from label where rowId = :rowId limit 1")
    fun getByRowId(rowId: Long): Label?

    @Query("select * from label where userId = :userId and show = 1 order by sort asc")
    fun getShow(userId: Long): List<Label>

    @Query("select * from label where userId = :userId order by sort asc")
    fun getAll(userId: Long): List<Label>

    @Query("select * from label where id in (:ids)")
    fun getByIds(ids: List<Long>): List<Label>

}