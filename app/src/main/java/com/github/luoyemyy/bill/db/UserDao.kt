package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    fun add(user: User): Long

    @Update
    fun update(user: User): Int

    @Query("select * from user where id = :id")
    fun get(id: Long): User?

    @Query("select * from user")
    fun getAll(): List<User>

    @Query("select * from user where isDefault = 1 limit 1")
    fun getDefault(): User?
}