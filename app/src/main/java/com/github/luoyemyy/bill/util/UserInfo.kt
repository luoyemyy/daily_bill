package com.github.luoyemyy.bill.util

import android.content.Context
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.config.ext.editor
import com.github.luoyemyy.config.ext.spfLong
import com.github.luoyemyy.config.ext.spfString

object UserInfo {

    fun getUserId(context: Context): Long = context.spfLong("userId")
    fun getUsername(context: Context): String? = context.spfString("username")
    fun getIntro(context: Context): String? = context.spfString("intro")

    fun saveUser(context: Context, user: User) {
        context.editor().putLong("userId", user.id)
            .putString("username", user.nickname)
            .putString("intro", user.intro).apply()
    }

}