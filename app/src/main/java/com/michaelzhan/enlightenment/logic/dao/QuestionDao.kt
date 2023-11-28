package com.michaelzhan.enlightenment.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.michaelzhan.enlightenment.logic.model.Question

@Dao
interface QuestionDao {
    @Insert
    fun insertQuestion(question: Question): Long

    @Query("select * from Question")
    fun loadAllQuestions(): LiveData<List<Question>>

    @Query("select * from Question where id = (:mId)")
    fun loadQuestionFromId(mId: Long): Question?

    @Update
    fun updateQuestion(newQuestion: Question)

    @Delete
    fun deleteQuestion(data: Question)
}