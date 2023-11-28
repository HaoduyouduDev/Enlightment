package com.michaelzhan.enlightenment.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.michaelzhan.enlightenment.logic.model.ErrorBook
import kotlinx.coroutines.selects.select

@Dao
interface ErrorBookDao {
    @Insert
    fun insertErrorBook(book: ErrorBook): Long

    @Update
    fun updateErrorBook(newBook: ErrorBook)

    @Query("select * from ErrorBook")
    fun loadAllErrorBooks(): LiveData<List<ErrorBook>>

    @Query("select * from ErrorBook where collect = 1")
    fun loadCollectErrorBook(): LiveData<List<ErrorBook>>

    @Query("select * from ErrorBook where collect = 0")
    fun loadNoCollectErrorBook(): LiveData<List<ErrorBook>>

    @Query("select * from ErrorBook where id = (:mid)")
    fun loadErrorBookFromId(mid: Long): ErrorBook?

    @Query("select count(*) from ErrorBook")
    fun getSize(): Int

    @Delete
    fun deleteErrorBook(book: ErrorBook)
}