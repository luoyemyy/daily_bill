package com.github.luoyemyy.bill.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Entity
data class User(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                var nickname: String? = null,
                var isDefault: Int = 0
)

@Dao
interface UserDao {

    @Insert
    fun add(user: User)

    @Query("select * from user where isDefault = 1 limit 1")
    fun getDefault(): LiveData<User>
}
