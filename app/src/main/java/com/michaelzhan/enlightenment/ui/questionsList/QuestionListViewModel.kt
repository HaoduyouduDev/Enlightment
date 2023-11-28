package com.michaelzhan.enlightenment.ui.questionsList

import android.util.Log
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question

class QuestionListViewModel: ViewModel() {
    private val tag = "QuestionListViewModel"
    private val allQuestionLiveData = Repository.getAllQuestions()
    private val mQuestionIds = ArrayList<Long>()
    private var firstLoad = true
    var subjectId = -1L
    var delPos = -1
    var addPos = -1

    val questionsList = ArrayList<Question>()

    val questionsLiveData = Transformations.map(allQuestionLiveData) { questions ->
        questions.filter {que ->
            mQuestionIds.contains(que.id)
        }
    }

    fun firstRequestQuestions(data: ArrayList<Long>) {
        if (firstLoad) {
            for (i in data) {
                mQuestionIds.add(i)
            }
        }
        for (i in data) {
            Log.d(tag, "add question id is $i")
        }
        firstLoad = false
    }

    fun addQuestion(data: Question, pos: Int = -1) {
        addPos = pos
        delPos = -1
        mQuestionIds.add(Repository.addQuestion(data))
    }

    fun deleteQuestion(data: Question, pos: Int = -1) {
        mQuestionIds.remove(data.id)
        delPos = pos
        addPos = -1
        Repository.deleteQuestion(data)
    }
}