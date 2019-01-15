package com.github.luoyemyy.bill.db

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [Bill::class, Favor::class, Label::class, LabelRelation::class, User::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class Db : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getBillDao(): BillDao
    abstract fun getLabelDao(): LabelDao
    abstract fun getFavorDao(): FavorDao

    companion object {

        @Volatile
        private var instance: Db? = null

        private const val DB_NAME = "daily_bill"

        private fun createDb(appContext: Context): Db {
            return Room.databaseBuilder(appContext, Db::class.java, DB_NAME).build()
        }
        
        fun getInstance(context: Context): Db {
            return instance ?: synchronized(this) {
                instance ?: createDb(context).also { instance = it }
            }
        }
    }
}

fun getUserDao(context: Context): UserDao = Db.getInstance(context.applicationContext).getUserDao()
fun getBillDao(context: Context): BillDao = Db.getInstance(context.applicationContext).getBillDao()
fun getLabelDao(context: Context): LabelDao = Db.getInstance(context.applicationContext).getLabelDao()
fun getFavorDao(context: Context): FavorDao = Db.getInstance(context.applicationContext).getFavorDao()

class DateConverters {
    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun toLong(date: Date?): Long? = date?.time
}