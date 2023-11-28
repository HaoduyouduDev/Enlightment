package com.michaelzhan.enlightenment.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.michaelzhan.enlightenment.logic.model.ErrorBookIcon

@Dao
interface ErrorBookIconDao {

    @Insert
    fun insertIcon(icon: ErrorBookIcon): Long

    @Update
    fun updateIcon(newIcon: ErrorBookIcon)

    @Query("select * from ErrorBookIcon")
    fun loadAllIcons(): LiveData<List<ErrorBookIcon>>

    @Query("select * from ErrorBookIcon where id = (:mid)")
    fun loadIconsFromId(mid: Long): ErrorBookIcon?

    @Query("select count(*) from ErrorBookIcon")
    fun getSize(): Int

    @Delete
    fun deleteIcon(icon: ErrorBookIcon)
}