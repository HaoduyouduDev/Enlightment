package com.michaelzhan.enlightenment.ui.editQuestions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.michaelzhan.enlightenment.logic.Repository
import com.michaelzhan.enlightenment.logic.model.Question

class EditQuestionsViewModel: ViewModel() {
    private val tag = "EditQuestionsViewModel"
    private var firstLoad = true
    private val mQuestionsIdList = ArrayList<Long>()
    private val allQuestionsLiveData = Repository.getAllQuestions()
    var subjectId = -1L
    var deletePos = -1
    var addPos = -1
    val questionsLiveData = Transformations.map(allQuestionsLiveData) { questions ->
        Log.d(tag, "ob")
        questions.filter { que ->
            mQuestionsIdList.contains(que.id)
        }
    }
    val questionsList = ArrayList<Question>()

    fun getQuestionFromId(id: Long) = Repository.getQuestionFromId(id)

    fun firstRequestQuestions(data: LongArray) {
        if (firstLoad) {
            for (i in data) {
                mQuestionsIdList.add(i)
            }
        }
        for (i in data) {
            Log.d(tag, "add question id is $i")
        }
        firstLoad = false
    }

    fun addQuestion(data: Question, pos: Int = -1) {
        addPos = pos
        deletePos = -1
        mQuestionsIdList.add(Repository.addQuestion(data))
    }

    fun deleteQuestion(data: Question, pos: Int = -1) {
        mQuestionsIdList.remove(data.id)
        deletePos = pos
        addPos = -1
        Repository.deleteQuestion(data)
    }
}