package com.github.luoyemyy.bill.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Entity
data class User(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var nickname: String? = null,
    var intro: String? = null,
    var isDefault: Int = 0
)

@Dao
interface UserDao {

    @Insert
    fun add(user: User): Long

    @Update
    fun update(users: List<User>): Int

    @Query("select * from user")
    fun getAll(): LiveData<List<User>>

    @Query("select * from user where isDefault = 1 limit 1")
    fun getDefault(): LiveData<User>
}
