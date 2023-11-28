package com.michaelzhan.enlightenment.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.michaelzhan.enlightenment.logic.model.ReviewTime

@Dao
interface ReviewTimeDao {
    @Insert
    fun insertReviewTime(data: ReviewTime): Long

    @Update
    fun updateReviewTime(data: ReviewTime)

    @Query("select * from ReviewTime where questionId = (:data)")
    fun loadReviewTimeFromQuestionId(data: Long): ReviewTime?

    @Query("select * from ReviewTime")
    fun loadAllReviewTime(): LiveData<List<ReviewTime>>

    @Delete
    fun deleteReviewTime(data: ReviewTime)
}