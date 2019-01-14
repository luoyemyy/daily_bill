package com.github.luoyemyy.bill.util

import android.content.Context
import androidx.annotation.WorkerThread
import com.github.luoyemyy.bill.db.User
import com.github.luoyemyy.bill.db.getDao
import com.github.luoyemyy.config.editor
import com.github.luoyemyy.config.spfLong
import com.github.luoyemyy.config.spfString

object UserInfo {

    fun getUserId(context: Context): Long = context.spfLong("userId")
    fun getUsername(context: Context): String? = context.spfString("username")

    fun saveUser(context: Context, user: User) {
        context.editor().putLong("userId", user.id).putString("username", user.nickname).apply()
    }

    @WorkerThread
    fun setDefaultUser(context: Context, name: String, ok: () -> Unit) {
        val dao = getDao(context)
        val clearOld = dao.getDefaultUser()?.let {
            it.isDefault = 0
            dao.updateUser(it) > 0
        } ?: true
        if (clearOld && dao.addUser(User(nickname = name, isDefault = 1)) > 0) {
            dao.getDefaultUser()?.apply {
                UserInfo.saveUser(context, this)
                ok()
            }
        }
    }

    @WorkerThread
    fun setDefaultUser(context: Context, userId: Long, ok: () -> Unit) {
        val dao = getDao(context)
        val newUser = dao.getUser(userId)?.apply {
            isDefault = 1
        } ?: return

        val clearOld = dao.getDefaultUser()?.let {
            it.isDefault = 0
            dao.updateUser(it) > 0
        } ?: true

        if (clearOld && dao.updateUser(newUser) > 0) {
            dao.getDefaultUser()?.apply {
                UserInfo.saveUser(context, this)
                ok()
            }
        }
    }
}