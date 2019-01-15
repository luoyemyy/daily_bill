package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FavorDao {

    @Query("select * from favor where userId = :userId order by sort desc")
    fun getAll(userId: Long): List<Favor>
}