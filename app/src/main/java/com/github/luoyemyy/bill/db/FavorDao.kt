package com.github.luoyemyy.bill.db

import androidx.room.*

@Dao
interface FavorDao {

    @Insert
    fun add(favor: Favor): Long

    @Insert
    fun addLabelRelation(labelRelation: List<LabelRelation>)

    @Update
    fun update(favors: List<Favor>)

    @Delete
    fun delete(favor: Favor)

    @Query("select * from favor where id = :id")
    fun get(id: Long): Favor?

    @Query("select * from favor where rowId = :rowId")
    fun getByRowId(rowId: Long): Favor?

    @Query("select * from favor where userId = :userId order by sort asc")
    fun getAll(userId: Long): List<Favor>

}