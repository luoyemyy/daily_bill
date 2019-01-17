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
    fun setDefaultUser(context: Context, name: String, ok: () -> Unit) {
        val dao = getUserDao(context)
        val clearOld = dao.getDefault()?.let {
            it.isDefault = 0
            dao.update(it) > 0
        } ?: true
        if (clearOld && dao.add(User(nickname = name, isDefault = 1)) > 0) {
            dao.getDefault()?.apply {
                UserInfo.saveUser(context, this)
                ok()
            }
        }
    }

    @WorkerThread
    fun setDefaultUser(context: Context, userId: Long, ok: () -> Unit) {
        val dao = getUserDao(context)
        val newUser = dao.get(userId)?.apply {
            isDefault = 1
        } ?: return

        val clearOld = dao.getDefault()?.let {
            it.isDefault = 0
            dao.update(it) > 0
        } ?: true

        if (clearOld && dao.update(newUser) > 0) {
            dao.getDefault()?.apply {
                UserInfo.saveUser(context, this)
                ok()
            }
        }
    }
}