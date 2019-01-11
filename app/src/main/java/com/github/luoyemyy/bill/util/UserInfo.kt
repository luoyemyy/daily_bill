package com.github.luoyemyy.bill.util

import android.content.Context
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.config.editor
import com.github.luoyemyy.config.spfLong
import com.github.luoyemyy.config.spfString

object UserInfo {

    fun getUserId(context: Context): Long = context.spfLong("userId")
    fun getUsername(context: Context): String? = context.spfString("username")

    fun saveUser(context: Context, user: User) {
        context.editor().putLong("userId", user.id).putString("username", user.nickname).apply()
    }

}