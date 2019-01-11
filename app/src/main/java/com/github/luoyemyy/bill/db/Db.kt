package com.github.luoyemyy.bill.db

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [Bill::class, Favor::class, Label::class, LabelRelation::class, User::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class Db : RoomDatabase() {

    abstract fun getDao(): Dao

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

fun getDao(context: Context): Dao = Db.getInstance(context.applicationContext).getDao()

class DateConverters {
    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toLong(date: Date?): Long? = date?.time
}