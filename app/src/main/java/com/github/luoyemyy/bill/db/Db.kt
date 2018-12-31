package com.github.luoyemyy.bill.db

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [User::class, Label::class, Bill::class, BillLabel::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class Db : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getLabelDao(): LabelDao
    abstract fun getBillDao(): BillDao
    abstract fun getBillLabelDao(): BillLabelDao

    companion object {

        private var instance: Db? = null

        private const val DB_NAME = "daily_bill"

        private fun createDb(appContext: Context): Db {
            return Room.databaseBuilder(appContext, Db::class.java, DB_NAME).build().apply { instance = this }
        }

        fun getInstance(appContext: Context): Db {
            return instance ?: createDb(appContext)
        }
    }
}

fun getLabelDao(appContext: Context): LabelDao = Db.getInstance(appContext).getLabelDao()
fun getUserDao(appContext: Context): UserDao = Db.getInstance(appContext).getUserDao()
fun getBillDao(appContext: Context): BillDao = Db.getInstance(appContext).getBillDao()
fun getBillLabelDao(appContext: Context): BillLabelDao = Db.getInstance(appContext).getBillLabelDao()

class DateConverters {
    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toLong(date: Date?): Long? = date?.time
}