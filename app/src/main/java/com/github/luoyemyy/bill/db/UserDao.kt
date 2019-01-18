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
    fun update(user: User)

    @Query("update user set isDefault = 0 where isDefault = 1")
    fun deleteDefault()

    @Query("update user set isDefault = 1 where id = :id")
    fun updateDefault(id: Long)

    @Query("select * from user where id = :id")
    fun get(id: Long): User?

    @Query("select * from user where rowId = :rowId limit 1")
    fun getByRowId(rowId: Long): User?

    @Query("select * from user")
    fun getAll(): List<User>

    @Query("select * from user where isDefault = 1 limit 1")
    fun getDefault(): User?

}