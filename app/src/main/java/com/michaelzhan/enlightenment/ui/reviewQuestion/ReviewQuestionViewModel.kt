package com.michaelzhan.enlightenment.ui.reviewQuestion

import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question

class ReviewQuestionViewModel: ViewModel() {
    var questionsIdList: LongArray? = null
    var showAnswerButtonIsGone = HashMap<Int, Boolean>()

    fun getQuestionFromId(id: Long) = Repository.getQuestionFromId(id)
    fun changeProficiency(mQuestion: Question, f: Float) = Repository.changeQuestionProficiency(mQuestion, f)
}