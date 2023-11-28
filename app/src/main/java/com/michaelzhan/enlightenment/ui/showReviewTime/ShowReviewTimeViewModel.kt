package com.michaelzhan.enlightenment.ui.showReviewTime

import android.content.Context
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.EnlightenmentApplication
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.ReviewTime

class ShowReviewTimeViewModel: ViewModel() {
    private val tag = "ShowReviewTimeViewModel"
    val reviewTimeLiveData = Repository.getAllReviewTime()
    val reviewTimeList = ArrayList<ReviewTimeAdapter.QuestionWithReviewTime>()

    var deletePos = -1

    fun deleteReviewTime(reviewTime: ReviewTimeAdapter.QuestionWithReviewTime, pos: Int = -1) {
        deletePos = pos
        Repository.changeMemoryFromQuestion(reviewTime.question, false)
        Repository.deleteReviewTime(reviewTime.reviewTime)
    }

    fun getQuestionFromId(id: Long) = Repository.getQuestionFromId(id)

    fun getSubjectNameFromId(id: Long) = Repository.getErrorBookFromId(id)?.name
}