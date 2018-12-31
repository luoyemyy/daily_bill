package com.github.luoyemyy.bill.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey


@Entity
data class Label(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String? = null
)

@Dao
interface LabelDao {
    @Insert
    fun add(label: Label)
}