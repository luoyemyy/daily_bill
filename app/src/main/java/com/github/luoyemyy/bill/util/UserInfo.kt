package com.github.luoyemyy.bill.util

import android.content.Context
import androidx.annotation.WorkerThread
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.bill.db.getUserDao
import com.github.luoyemyy.config.editor
import com.github.luoyemyy.config.spfBool
import com.github.luoyemyy.config.spfLong
import com.github.luoyemyy.config.spfString

object UserInfo {

    fun getUserId(context: Context): Long = context.spfLong("userId")
    fun getUsername(context: Context): String? = context.spfString("username")
    fun getFavorTip(context: Context): Boolean = context.spfBool("favor_tip")

    fun hideFavorTip(context: Context) {
        context.editor().putBoolean("favor_tip", true).apply()
    }

    fun saveUser(context: Context, user: User) {
        context.editor().putLong("userId", user.id).putString("username", user.nickname).apply()
    }

    @WorkerThread
    fun setDefaultUser(context: Context, userId: Long) {
        val dao = getUserDao(context)
        dao.deleteDefault()
        dao.updateDefault(userId)
        dao.get(userId)?.apply {
            saveUser(context, this)
        }
    }
}