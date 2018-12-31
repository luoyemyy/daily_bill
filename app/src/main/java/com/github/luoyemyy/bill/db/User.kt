package com.github.luoyemyy.bill.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

fun getUserDao(appContext: Context): UserDao = Db.getInstance(appContext).getUserDao()

@Entity
data class User(@PrimaryKey(autoGenerate = true) var id: Int = 0,
                var nickname: String? = null,
                var defaultUser: Int = 0
)

@Dao
interface UserDao {

    @Insert
    fun add(user: User)

    @Query("select * from user where defaultUser = 1 limit 1")
    fun getDefault(): LiveData<User>
}
